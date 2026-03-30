package baseDonnees;

import java.sql.*;
import java.util.*;

public class DBIntersections {

    public static void insert(String intersection, String route1, String route2) {
        String sql = "INSERT IGNORE INTO intersections (name, route1_name, route2_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, intersection);
            stmt.setString(2, route1);
            stmt.setString(3, route2);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String intersection) {
        String sql = "DELETE FROM intersections WHERE name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, intersection);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getAll() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT name, route1_name, route2_name FROM intersections";
        try (Connection conn = DBConnection.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("name"),
                    rs.getString("route1_name"),
                    rs.getString("route2_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}