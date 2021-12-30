package integration;

import model.InstrumentDTO;
import model.RentalDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolDAO {
    private Connection connection;
    private PreparedStatement listAvailableInstrumentsByKindStmt;
    private PreparedStatement rentInstrumentByIdWithStudentIDStmt;
    private PreparedStatement getPriceOfInstrumentByIDStmt;
    private PreparedStatement markInstrumentAsRentedByIDStmt;
    private PreparedStatement unmarkInstrumentAsRentedByIDStmt;
    private PreparedStatement terminateRentalByIDStmt;
    private PreparedStatement getRentalsByStudentIDStmt;
    private PreparedStatement getRentalByIDStmt;

    public SchoolDAO() throws SchoolDBException {
        try {
            connectToSchoolDB();
            prepareStatements();
        } catch (SQLException exception) {
            throw new SchoolDBException("Could not connect to datasource.", exception);
        }
    }

    public void terminateRentalByID(String rentalID) throws SchoolDBException {
        String failureMsg = "Could not terminate rental.";
        ResultSet result = null;
        try {
            // TODO: Check that rental isn't already terminated.
            getRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
            result = getRentalByIDStmt.executeQuery();
            result.next();
            String instrumentID = result.getString("instrument_id");

            terminateRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
            terminateRentalByIDStmt.executeUpdate();

            unmarkInstrumentAsRentedByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            unmarkInstrumentAsRentedByIDStmt.executeUpdate();

            connection.commit();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    public List<RentalDTO> getRentalsByStudentID(String studentID) throws SchoolDBException {
        String failureMsg = "Could not acquire the rentals.";
        ResultSet result = null;
        List<RentalDTO> rentals = new ArrayList<>();
        try {
            getRentalsByStudentIDStmt.setInt(1, Integer.parseInt(studentID));
            result = getRentalsByStudentIDStmt.executeQuery();
            while (result.next()) {
                rentals.add(new RentalDTO(  result.getString("rental_id"),
                                            result.getString("student_id"),
                                            result.getString("instrument_id"),
                                            result.getString("start_time"),
                                            result.getString("end_time"),
                                            result.getBoolean("with_delivery"),
                                            result.getInt("price")));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
        }
        return rentals;
    }

    public void rentInstrumentByIDWithStudentID(String studentID, String instrumentID) throws SchoolDBException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        try {
            // TODO: Check 2 rentals per student limit.
            getPriceOfInstrumentByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            result = getPriceOfInstrumentByIDStmt.executeQuery();
            result.next();
            int price = result.getInt("price");
            rentInstrumentByIdWithStudentIDStmt.setInt(1, Integer.parseInt(studentID));
            rentInstrumentByIdWithStudentIDStmt.setInt(2, Integer.parseInt(instrumentID));
            rentInstrumentByIdWithStudentIDStmt.setInt(3, price);
            rentInstrumentByIdWithStudentIDStmt.executeUpdate();
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
                    "WHERE type = ? AND is_rented = false"
        );
        rentInstrumentByIdWithStudentIDStmt = connection.prepareStatement(
                "INSERT INTO rental " +
                    "(student_id, instrument_id, start_time, end_time, with_delivery, price) " +
                    "values (?, ?, now(), now() + interval '1 month', true, ?)"
        );
        markInstrumentAsRentedByIDStmt = connection.prepareStatement(
                "UPDATE instrument " +
                    "SET is_rented = true " +
                    "WHERE instrument_id = ?"
        );
        unmarkInstrumentAsRentedByIDStmt = connection.prepareStatement(
                "UPDATE instrument " +
                    "SET is_rented = false " +
                    "WHERE instrument_id = ?"
        );
        getPriceOfInstrumentByIDStmt = connection.prepareStatement(
                "SELECT * " +
                    "FROM instrument " +
                    "WHERE instrument_id = ?"
        );
        getRentalsByStudentIDStmt = connection.prepareStatement(
                "SELECT * " +
                    "FROM rental " +
                    "WHERE student_id = ?"
        );
        terminateRentalByIDStmt = connection.prepareStatement(
                "UPDATE rental " +
                    "SET end_time = now() " +
                    "WHERE rental_id = ?"
        );
        getRentalByIDStmt = connection.prepareStatement(
                "SELECT * " +
                    "FROM rental " +
                    "WHERE rental_id = ?"
        );
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
