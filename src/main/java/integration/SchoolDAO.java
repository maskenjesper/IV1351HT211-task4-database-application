package integration;

import model.InstrumentDTO;
import model.RentalException;
import model.RentalDTO;
import model.StudentException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access class that handles communication with the music school database.
 */
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
    private PreparedStatement rentalIsTerminatedStmt;
    private PreparedStatement instrumentIsRentedStmt;
    private PreparedStatement getNrOfActiveStudentRentalsByIDStmt;

    /**
     * Constructor that connects to data source and creates prepared statements.
     * @throws SchoolDBException
     */
    public SchoolDAO() throws SchoolDBException {
        try {
            connectToSchoolDB();
            prepareStatements();
        } catch (SQLException exception) {
            throw new SchoolDBException("Could not connect to datasource.", exception);
        }
    }

    /**
     * Queries the database for a list of available instruments of a specified kind.
     * @param kind Specified kind of instruments.
     * @return List of instruments.
     * @throws SchoolDBException
     */
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
            handleRollback(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return instruments;
    }

    /**
     * Queries the database to create a new rental of specified instrument for specified student.
     * @param studentID Specified student.
     * @param instrumentID Specified instrument.
     * @throws SchoolDBException
     */
    public void rentInstrumentByIDWithStudentID(String studentID, String instrumentID) throws SchoolDBException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        try {
            if(instrumentIsRented(instrumentID)){
                handleRollback(failureMsg, new RentalException(studentID,instrumentID," Instrument is not available for rental"));
            }
            int nrOfRentals = nrOfActiveStudentRentals(studentID);
            if(nrOfRentals >= 2){
                handleRollback(failureMsg, new RentalException(studentID,instrumentID, " Student has too many active rentals"));
            }

            getPriceOfInstrumentByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            result = getPriceOfInstrumentByIDStmt.executeQuery();
            if(!result.next()){
                handleRollback(failureMsg, new RentalException(studentID,instrumentID," Requested instrument was not found"));
            }

            int price = result.getInt("price");
            rentInstrumentByIdWithStudentIDStmt.setInt(1, Integer.parseInt(studentID));
            rentInstrumentByIdWithStudentIDStmt.setInt(2, Integer.parseInt(instrumentID));
            rentInstrumentByIdWithStudentIDStmt.setInt(3, price);
            rentInstrumentByIdWithStudentIDStmt.executeUpdate();
            markInstrumentAsRentedByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            markInstrumentAsRentedByIDStmt.executeUpdate();

            connection.commit();
        } catch (SQLException sqle) {
            handleRollback(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    /**
     * Queries the database for rentals for specified student.
     * @param studentID Specified student.
     * @return List of rentals.
     * @throws SchoolDBException
     */
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
            handleRollback(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return rentals;
    }

    /**
     * Queries the database to terminate a rental.
     * @param rentalID Specified rental.
     * @throws SchoolDBException
     */
    public void terminateRentalByID(String rentalID) throws SchoolDBException {
        String failureMsg = "Could not terminate rental.";
        ResultSet result = null;
        try {
            getRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
            result = getRentalByIDStmt.executeQuery();
            result.next();
            int instrumentID = result.getInt("instrument_id");
            if(!rentalIsTerminated(rentalID)) {
                terminateRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
                terminateRentalByIDStmt.executeUpdate();
                unmarkInstrumentAsRentedByIDStmt.setInt(1, instrumentID);
                unmarkInstrumentAsRentedByIDStmt.executeUpdate();
                connection.commit();
            }
            else {
                handleRollback(failureMsg, new RentalException("Rental was already terminated."));
            }
        } catch (SQLException sqle) {
            handleRollback(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    private boolean rentalIsTerminated(String rentalID) throws SchoolDBException, SQLException {
        String failureMsg = "Could not check if rentals is terminated.";
        ResultSet result = null;
        boolean isTerminated = false;
        try {
            rentalIsTerminatedStmt.setInt(1,Integer.parseInt(rentalID));
            result = rentalIsTerminatedStmt.executeQuery();
            if(result.next()){
                isTerminated = result.getBoolean(1);
            }
            return isTerminated;
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    private boolean instrumentIsRented(String instrumentID) throws SchoolDBException, SQLException {
        String failureMsg = "Could not check if instrument is rented.";
        ResultSet result = null;
        boolean isRented = false;
        try {
            instrumentIsRentedStmt.setInt(1,Integer.parseInt(instrumentID));
            result = instrumentIsRentedStmt.executeQuery();
            if(result.next()){
                isRented = result.getBoolean(1);
            }
            return isRented;
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    private int nrOfActiveStudentRentals(String studentId) throws SchoolDBException, SQLException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        int activeRentals;
        try {
            getNrOfActiveStudentRentalsByIDStmt.setInt(1,Integer.parseInt(studentId));
            result = getNrOfActiveStudentRentalsByIDStmt.executeQuery();
            if(result.next()){
                activeRentals = result.getInt(1);
            }
            else {
                throw new SQLException(failureMsg, new StudentException("Could not find student."));
            }
            return activeRentals;
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    private void prepareStatements() throws SQLException {
        getNrOfActiveStudentRentalsByIDStmt = connection.prepareStatement(
                "select count(*) from rental WHERE end_time > now() AND student_id = ?"
        );

        rentalIsTerminatedStmt = connection.prepareStatement(
                "select count(*) > 0 from rental where rental_id = ? AND end_time < now()"
        );

        instrumentIsRentedStmt = connection.prepareStatement(
                "select count(instrument_id) > 0 from instrument where instrument_id = ? AND is_rented = true;"
        );

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
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/music_school", "postgres",
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

    private void handleRollback(String failureMsg, Exception cause) throws SchoolDBException {
        try {
            connection.rollback();
        } catch (SQLException exc) {
            failureMsg = failureMsg + ". Rollback failed: " + exc.getMessage();
        }

        if (cause != null)
            throw new SchoolDBException(failureMsg, cause);
        else
            throw new SchoolDBException(failureMsg);
    }
}