package server.database;

import common.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

public class HumanBeingDAO {
    public static boolean save(HumanBeing human, String ownerUsername) {
        String sql = "INSERT INTO human_beings (name, coordinate_x, coordinate_y, creation_date, " +
                "real_hero, has_toothpick, impact_speed, soundtrack_name, weapon_type, " +
                "mood, car_cool, owner_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM users WHERE username = ?))";
        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, human.getName());
            stmt.setDouble(2, human.getCoordinates().getX());
            stmt.setFloat(3, human.getCoordinates().getY());
            stmt.setTimestamp(4, Timestamp.valueOf(human.getCreationDate()));
            stmt.setBoolean(5, human.getRealHero());
            stmt.setBoolean(6, human.getHasToothpick());
            stmt.setFloat(7, human.getImpactSpeed());
            stmt.setString(8, human.getSoundtrackName());
            stmt.setString(9, human.getWeaponType().name());
            stmt.setString(10, human.getMood() != null ? human.getMood().name() : null);
            stmt.setBoolean(11, human.getCar().getCool());
            stmt.setString(12, ownerUsername);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                human.setId(rs.getLong(1));
            }
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Deque<HumanBeing> loadAll() {
        Deque<HumanBeing> collection = new ArrayDeque<>();
        String sql = "SELECT h.*, u.username as owner_name " +
                "FROM human_beings h " +
                "JOIN users u ON h.owner_id = u.id";

        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HumanBeing human = mapResultSetToHuman(rs);
                collection.add(human);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return collection;
    }

    public static boolean update(HumanBeing human, String ownerUsername) {
        String sql = "UPDATE human_beings SET " +
                "name = ?, coordinate_x = ?, coordinate_y = ?, creation_date = ?, " +
                "real_hero = ?, has_toothpick = ?, impact_speed = ?, soundtrack_name = ?, " +
                "weapon_type = ?, mood = ?, car_cool = ? " +
                "WHERE id = ? AND owner_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, human.getName());
            stmt.setDouble(2, human.getCoordinates().getX());
            stmt.setFloat(3, human.getCoordinates().getY());
            stmt.setTimestamp(4, Timestamp.valueOf(human.getCreationDate()));
            stmt.setBoolean(5, human.getRealHero());
            stmt.setBoolean(6, human.getHasToothpick());
            stmt.setFloat(7, human.getImpactSpeed());
            stmt.setString(8, human.getSoundtrackName());
            stmt.setString(9, human.getWeaponType().name());
            stmt.setString(10, human.getMood() != null ? human.getMood().name() : null);
            stmt.setBoolean(11, human.getCar().getCool());
            stmt.setLong(12, human.getId());
            stmt.setString(13, ownerUsername);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(long id, String ownerUsername) {
        String sql = "DELETE FROM human_beings WHERE id = ? AND owner_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, ownerUsername);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean existsAndOwnedBy(long id, String ownerUsername) {
        String sql = "SELECT 1 FROM human_beings h " +
                "JOIN users u ON h.owner_id = u.id " +
                "WHERE h.id = ? AND u.username = ?";

        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setString(2, ownerUsername);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static HumanBeing mapResultSetToHuman(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        Double x = rs.getDouble("coordinate_x");
        Float y = rs.getFloat("coordinate_y");
        Coordinates coordinates = new Coordinates(x, y);
        LocalDateTime creationDate = rs.getTimestamp("creation_date").toLocalDateTime();
        Boolean realHero = rs.getBoolean("real_hero");
        Boolean hasToothpick = rs.getBoolean("has_toothpick");
        float impactSpeed = rs.getFloat("impact_speed");
        String soundtrackName = rs.getString("soundtrack_name");
        WeaponType weaponType = WeaponType.valueOf(rs.getString("weapon_type"));
        String moodStr = rs.getString("mood");
        Mood mood = moodStr != null ? Mood.valueOf(moodStr) : null;
        Boolean carCool = rs.getBoolean("car_cool");
        Car car = new Car(carCool);
        String owner = rs.getString("owner_name");

        HumanBeing human = new HumanBeing(id, creationDate, name, coordinates, realHero, hasToothpick,
                impactSpeed, soundtrackName, weaponType, mood, car, owner);
        return human;
    }
}
