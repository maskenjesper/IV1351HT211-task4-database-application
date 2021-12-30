package integration;

import model.InstrumentDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolDAO {
    private Connection connection;
    private PreparedStatement listAvailableInstrumentsByKindStmt;
    private PreparedStatement rentInstrumentByIdWithUserIDStmt;
    private PreparedStatement getPriceOfInstrumentByIDStmt;
    private PreparedStatement markInstrumentAsRentedByIDStmt;

    public SchoolDAO() throws SchoolDBException {
        try {
            connectToSchoolDB();
            prepareStatements();
        } catch (SQLException exception) {
            throw new SchoolDBException("Could not connect to datasource.", exception);
        }
    }

    public void rentInstrumentByIDWithUserID(String userID, String instrumentID) throws SchoolDBException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        try {
            // TODO: Check 2 rentals per student limit.
            getPriceOfInstrumentByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            result = getPriceOfInstrumentByIDStmt.executeQuery();
            result.next();
            int price = result.getInt("price");
            rentInstrumentByIdWithUserIDStmt.setInt(1, Integer.parseInt(userID));
            rentInstrumentByIdWithUserIDStmt.setInt(2, Integer.parseInt(instrumentID));
            rentInstrumentByIdWithUserIDStmt.setInt(3, price);
            rentInstrumentByIdWithUserIDStmt.executeUpdate();
            markInstrumentAsRentedByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            markInstrumentAsRentedByIDStmt.executeUpdate();
            connection.commit();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    public List<InstrumentDTO> listAvailableInstrumentsByKind(String kind) throws SchoolDBException {
        String failureMsg = "Could not list instruments of specified kind";
        ResultSet result = null;
        List<InstrumentDTO> instruments = new ArrayList<>();
        try {
            listAvailableInstrumentsByKindStmt.setString(1, kind);
            result = listAvailableInstrumentsByKindStmt.executeQuery();
            while (result.next()) {
                instruments.add(new InstrumentDTO(  result.getString("instrument_id"),
                                                    result.getString("type"),
                                                    result.getString("brand"),
                                                    result.getString("sn"),
                                                    result.getString("price")));
            }
            connection.commit();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
        }
        return instruments;
    }

    private void prepareStatements() throws SQLException {
        listAvailableInstrumentsByKindStmt = connection.prepareStatement(
                "SELECT * " +
                        "FROM instrument " +
                        "WHERE type = ? AND is_rented = false");
        rentInstrumentByIdWithUserIDStmt = connection.prepareStatement(
                "INSERT INTO rental " +
                        "(student_id, instrument_id, start_time, end_time, with_delivery, price) " +
                        "values (?, ?, now(), now() + interval '1 month', true, ?)");
        markInstrumentAsRentedByIDStmt = connection.prepareStatement(
                "UPDATE instrument " +
                        "SET is_rented = true " +
                        "WHERE instrument_id = ?");
        getPriceOfInstrumentByIDStmt = connection.prepareStatement(
                "SELECT * " +
                        "FROM instrument " +
                        "WHERE instrument_id = ?");
    }

    private void connectToSchoolDB() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Soundgood", "postgres",
                                                    "60d5m4ck");
        connection.setAutoCommit(false);
    }

    private void closeResultSet(String failureMsg, ResultSet result) throws SchoolDBException {
        try {
            if (result != null)
                result.close();
        } catch (Exception e) {
            throw new SchoolDBException(failureMsg + " Could not close result set.", e);
        }
    }
}
