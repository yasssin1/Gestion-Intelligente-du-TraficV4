package baseDonnees;

import java.sql.*;

public class DBRoutes {

    public static void insert(String routeName) {
        String sql = "INSERT IGNORE INTO routes (name) VALUES (?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, routeName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String routeName) {
        String sql = "DELETE FROM routes WHERE name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, routeName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static java.util.List<String> getAll() {
        java.util.List<String> routes = new java.util.ArrayList<>();
        String sql = "SELECT name FROM routes";
        try (Connection conn = DBConnection.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) routes.add(rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }
}
