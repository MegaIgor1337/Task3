package org.example;

import org.example.user.User;
import org.example.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner {
    public static void main(String[] args) {
        System.out.println(getCommonNames());
        System.out.println(getUsersWithCountOfTickets());
        updateTicketsById(53L);
        try {
            updateTicketFlightById(3L);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // самые частые имена
    private static List<String> getCommonNames() {
        String sql = """
                select name 
                from (select split_part(passenger_name, ' ', 1) name, count(*) count 
                from ticket t
                group by name
                having count(*) > 2
                order by count desc ) t
                """;
        List<String> commonNames = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            var result = statement.executeQuery();
            while (result.next()) {
                commonNames.add(result.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return commonNames;
    }

    // люди и их количество билетов
    private static List<User> getUsersWithCountOfTickets() {
        String sql = """
                select passenger_name, count(*) tickets
                from ticket
                group by passenger_name;
                """;
        List<User> users = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            var result = statement.executeQuery();
            while (result.next()) {
                users.add(new User(result.getString("passenger_name"),
                        result.getInt("tickets")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }


    // изменить билет по id
    private static void updateTicketsById(Long id) {
        String sql = """
                update ticket
                set passenger_name = 'Игорь Якубович'
                where id = ?;
                """;
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // изменить ticket и flight по id
    private static void updateTicketFlightById(Long id) throws SQLException {
        String sqlUpdateTicketById = """
                update ticket
                set cost = 400
                where flight_id = ?;
                """;
        String sqlUpdateFlightById = """
                update flight
                set flight_no = 'NK42K'
                where id = ?;
                """;

        Connection connection = null;
        PreparedStatement updateFlightStatement = null;
        PreparedStatement updateTicketStatement = null;
        try {
            connection = ConnectionManager.open();
            updateFlightStatement = connection.prepareStatement(sqlUpdateFlightById);
            updateTicketStatement = connection.prepareStatement(sqlUpdateTicketById);
            connection.setAutoCommit(false);
            updateTicketStatement.setLong(1, id);
            updateTicketStatement.executeUpdate();
            updateFlightStatement.setLong(1, id);
            updateFlightStatement.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw e;
        } finally {
            if (connection != null)
                connection.close();
            if (updateFlightStatement != null)
                updateFlightStatement.close();
            if (updateTicketStatement != null)
                updateTicketStatement.close();
        }
    }
}
