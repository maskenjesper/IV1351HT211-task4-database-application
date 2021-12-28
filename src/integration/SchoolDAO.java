package integration;

import model.InstrumentDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolDAO {
    private Connection connection;
    private PreparedStatement listAvailableInstrumentsByKindStmt;

    public SchoolDAO() throws SchoolDBException {
        try {
            connectToSchoolDB();
            prepareStatements();
        } catch (SQLException exception) {
            throw new SchoolDBException("Could not connect to datasource.", exception);
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

    private void connectToSchoolDB() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Soundgood", "postgres",
                                                    "Bokaj11!");
        connection.setAutoCommit(false);
    }

    private void prepareStatements() throws SQLException {
        listAvailableInstrumentsByKindStmt = connection.prepareStatement(
                "SELECT * " +
                "FROM instrument " +
                "WHERE type = ? AND is_rented = false");
    }

    private void closeResultSet(String failureMsg, ResultSet result) throws SchoolDBException {
        try {
            result.close();
        } catch (Exception e) {
            throw new SchoolDBException(failureMsg + " Could not close result set.", e);
        }
    }
}
