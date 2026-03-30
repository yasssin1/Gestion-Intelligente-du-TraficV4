const { Kafka } = require('kafkajs');
const WebSocket = require('ws');
const express = require('express');
const soap = require('soap');

const app = express();
app.use(express.json());


const cors = require('cors');

app.use(cors());
/* =======================
   CONFIG
======================= */
const KAFKA_BROKERS = ['localhost:9092'];
const SOAP_FLUX = "http://localhost:6000/ws/FluxVehicules?wsdl";
const SOAP_FEUX = "http://localhost:6001/ws/FeuxSignalisation?wsdl";

/* =======================
   KAFKA SETUP
======================= */
const kafka = new Kafka({
    clientId: 'traffic-dashboard',
    brokers: KAFKA_BROKERS
});

const consumer = kafka.consumer({ groupId: 'dashboard-group' });
const producer = kafka.producer();

/* =======================
   WEBSOCKET SERVER
======================= */
const wss = new WebSocket.Server({ port: 3000 });

let routesState = {};
let intersectionsState = {};

/* =======================
   BROADCAST FUNCTION
======================= */
function broadcast() {
    const payload = {
        routes: routesState,
        intersections: intersectionsState,
        adjustments: global.adjustmentAlerts || []
    };

    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(payload));
        }
    });
}

/* =======================
   WEB CLIENT CONNECTION
======================= */
wss.on('connection', ws => {
    ws.send(JSON.stringify({
        routes: routesState,
        intersections: intersectionsState
    }));
});

/* =======================
   KAFKA CONSUMER
======================= */
async function startKafka() {
    await consumer.connect();
    await producer.connect();

    await consumer.subscribe({ topic: 'traffic-data', fromBeginning: false });
    await consumer.subscribe({ topic: 'intersection-data', fromBeginning: false });
    await consumer.subscribe({ topic: 'pollution-data', fromBeginning: false });
    await consumer.subscribe({ topic: 'bruit-data', fromBeginning: false });
    await consumer.subscribe({ topic: 'intersection-adjustment', fromBeginning: false });

    await consumer.run({
        eachMessage: async ({ topic, message }) => {
            const data = JSON.parse(message.value.toString());

            //gestion trafic
            if (topic === 'traffic-data') {
                if (!routesState[data.route]) {
                    routesState[data.route] = {
                        nbVehicules: 0,
                        pollution: null,
                        timestamp: ""
                    };
                }

                routesState[data.route].nbVehicules = data.nbVehicules;
                routesState[data.route].timestamp = new Date().toLocaleTimeString();
            }

            //gestion intersection
            if (topic === 'intersection-data') {
                intersectionsState[data.intersection] = {
                    route1: data.route1,
                    route2: data.route2,
                    route1Red: data.route1Red,
                    route2Red: data.route2Red,
                    timestamp: new Date().toLocaleTimeString()
                };
            }

            //gestion pollution
            if (topic === 'pollution-data') {
                const routeName = data.route.trim();

                if (!routesState[routeName]) {
                    routesState[routeName] = {
                        nbVehicules: 0,
                        timestamp: new Date().toLocaleTimeString(),
                        pollution: null
                    };
                }

                if (data.niveau != null) {
                    routesState[routeName].pollution = data.niveau;
                }
                routesState[routeName].timestamp = new Date().toLocaleTimeString();
            }


            //gestion bruits
            if (topic === 'bruit-data') {
                const routeName = data.route.trim();

                if (!routesState[routeName]) {
                    routesState[routeName] = {
                        nbVehicules: 0,
                        pollution: null,
                        bruit: null,
                        timestamp: new Date().toLocaleTimeString()
                    };
                }

                routesState[routeName].bruit = data.niveau;
                routesState[routeName].timestamp = new Date().toLocaleTimeString();
            }

            if (topic === 'intersection-adjustment') {
                if (!global.adjustmentAlerts) global.adjustmentAlerts = [];

                global.adjustmentAlerts.unshift({
                    ...data,
                    timestamp: new Date().toLocaleTimeString()
                });

                if (global.adjustmentAlerts.length > 20) {
                    global.adjustmentAlerts.pop();
                }
            }

            broadcast();
        }
    });
}

/* =======================
   SOAP CLIENT HELPER
======================= */
async function getSoapClient( URL ) {
    return await soap.createClientAsync(URL);
}

/* =======================
   REST API (FROM FRONTEND)
======================= */

/* ➕ ADD ROUTE */
app.post("/routes/add", async (req, res) => {
    const { name } = req.body;
    console.log("RAW NAME:", name);
console.log("TYPE:", typeof name);
console.log("LENGTH:", name?.length);

    try {
            const client = await getSoapClient( SOAP_FLUX);
            await client.addRouteAsync({ routeName: name });
                // const client = await getSoapClient();
                // await client.addRoute({ routeName: "TEST_ROUTE" });


        // ✅ ADD TO LOCAL STATE (temporary value)
        routesState[name] = {
            nbVehicules: 0,
            timestamp: new Date().toLocaleTimeString()
        };

        // ✅ FORCE UI UPDATE
        broadcast();
        console.log("Route added via SOAP:", name);

        res.sendStatus(200);
    } catch (err) {
        console.error("Add route error:", err);
        res.sendStatus(500);
    }
});

/* ❌ DELETE ROUTE */
app.delete("/routes/delete/:name", async (req, res) => {
    const name = req.params.name;

    try {
        const fluxClient = await getSoapClient(SOAP_FLUX);
        const feuxClient = await getSoapClient(SOAP_FEUX);

        //  1. Delete all related intersections FIRST
        for (const key of Object.keys(intersectionsState)) {
            const inter = intersectionsState[key];

            if (inter.route1 === name || inter.route2 === name) {
                console.log("Deleting intersection via SOAP:", key);

                await feuxClient.deleteIntersectionAsync({
                    intersection: key
                });

                delete intersectionsState[key];
            }
        }

        //  2. Delete the route
        await fluxClient.deleteRouteAsync({ routeName: name });

        delete routesState[name];

        //  3. Update UI
        broadcast();

        console.log("Route + related intersections deleted:", name);

        res.sendStatus(200);

    } catch (err) {
        console.error("Delete route error:", err);
        res.sendStatus(500);
    }
});
// add intersection
app.post("/intersections/add", async (req, res) => {
    const { route1, route2, name } = req.body;

    try {
        const client = await getSoapClient(SOAP_FEUX);

        await client.addIntersectionAsync({
            intersection: name,
            route1: route1,
            route2: route2
        });

        console.log("Intersection added via SOAP:", name);

        res.sendStatus(200);
    } catch (err) {
        console.error("Add intersection error:", err);
        res.sendStatus(500);
    }
});
// delete int
app.delete("/intersections/delete/:name", async (req, res) => {
    const name = req.params.name;

    try {
        const client = await getSoapClient( SOAP_FEUX );

        await client.deleteIntersectionAsync({
            intersection: name
        });

        delete intersectionsState[name];

        broadcast();

        console.log("Intersection deleted:", name);

        res.sendStatus(200);
    } catch (err) {
        console.error("Delete intersection error:", err);
        res.sendStatus(500);
    }
});
/* =======================
   START EVERYTHING
======================= */
async function start() {
    await startKafka();

    app.listen(8081, () => {
        console.log("REST + SOAP Gateway running on http://localhost:8081");
    });
                // const client = await getSoapClient( SOAP_FLUX );
                // await client.addRoute({ routeName: "TEST_ROUTE" });

    console.log("WebSocket running on ws://localhost:3000");
}

start().catch(console.error);