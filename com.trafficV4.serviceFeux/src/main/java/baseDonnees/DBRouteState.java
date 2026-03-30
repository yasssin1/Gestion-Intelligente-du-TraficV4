package baseDonnees;

import java.sql.*;

public class DBRouteState {

    public static void initRoute(String routeName) {
        String sql = "INSERT IGNORE INTO route_state (route_name) VALUES (?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, routeName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getVehicules(String routeName) {
        String sql = "SELECT nb_vehicules FROM route_state WHERE route_name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, routeName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("nb_vehicules");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static void updateVehicules(String routeName, int nbVehicules) {
        String sql = "UPDATE route_state SET nb_vehicules = ? WHERE route_name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nbVehicules);
            stmt.setString(2, routeName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePollution(String routeName, int pollution) {
        String sql = "UPDATE route_state SET pollution_niveau = ? WHERE route_name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pollution);
            stmt.setString(2, routeName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRoute(String routeName) {
        String sql = "DELETE FROM route_state WHERE route_name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, routeName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}