package integration;


import model.InstrumentDTO;
import model.RentException;
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
    private PreparedStatement getInstrumentByIDStmt;
    private PreparedStatement isInstrumentRentedStmt;
    private PreparedStatement getNrOfActiveStudentRentalsByIDStmt;
    private PreparedStatement getAllActiveRentalsStmt;
    private PreparedStatement getAllStudentIDsStmt;

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
            getRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
            result = getRentalByIDStmt.executeQuery();
            result.next();
            int instrumentID = result.getInt("instrument_id");
            if(instrumentIsRented(String.valueOf(instrumentID))){
                terminateRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
                terminateRentalByIDStmt.executeUpdate();

                unmarkInstrumentAsRentedByIDStmt.setInt(1, instrumentID);
                unmarkInstrumentAsRentedByIDStmt.executeUpdate();

                connection.commit();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    public List<Integer> getAllStudentIDs() throws SchoolDBException {
        List<Integer> lst = new ArrayList<>();
        ResultSet result = null;
        try {
            result = getAllStudentIDsStmt.executeQuery();
            while (result.next()){
                lst.add(result.getInt(1));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet("failureMsg", result);
            return lst;
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

    public List<RentalDTO> findAllActiveRentals() throws SchoolDBException {
        List<RentalDTO> rentals = new ArrayList<>();
        ResultSet result = null;

        try {
            result = getAllActiveRentalsStmt.executeQuery();
            while (result.next()){
                String rentalID = result.getString("rental_id");
                String studentID = result.getString("student_id");
                String instrumentID = result.getString("instrument_id");
                Timestamp start = result.getTimestamp("start_time");
                Timestamp end =  result.getTimestamp("end_time");
                boolean withDelivery = result.getBoolean("with_delivery");
                int price = result.getInt("price");
                RentalDTO rentalDTO = new RentalDTO(rentalID,studentID,instrumentID, start.toString(), end.toString(),withDelivery, price);
                rentals.add(rentalDTO);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet("Failed", result);
            return rentals;
        }
    }

    public void rentInstrumentByIDWithStudentID(String studentID, String instrumentID) throws SchoolDBException, RentException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        try {
            // TODO:
            //  [x] Check 2 rentals per student limit.
            //  [] throw error
            int nrOfRentals = nrOfActiveStudentRentals(studentID);
            if(nrOfRentals >= 2){
                throw new RentException(studentID,instrumentID, " Student has too many active rentals");
            }

            // TODO:
            //  [x] Check if instrument with <instrumentID> is available
            //  [] throw error
            if(instrumentIsRented(instrumentID)){
                throw new RentException(studentID,instrumentID," Instrument is not available for rental");
            }


            getPriceOfInstrumentByIDStmt.setInt(1, Integer.parseInt(instrumentID));
            result = getPriceOfInstrumentByIDStmt.executeQuery();
            if(!result.next()){
                throw new RentException(studentID,instrumentID," Requested instrument was not found");
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
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
        }
    }

    public RentalDTO getRentalById(String rentalID) throws SchoolDBException {
        ResultSet result = null;
        RentalDTO rentalDTO = null;
        try {
            getRentalByIDStmt.setInt(1, Integer.parseInt(rentalID));
            result = getRentalByIDStmt.executeQuery();
            String rental_id = result.getString("rental_id");
            String studentID = result.getString("student_id");
            String instrumentID = result.getString("instrument_id");
            Timestamp start = result.getTimestamp("start_time");
            Timestamp end = result.getTimestamp("end_time");
            boolean withDelivery = result.getBoolean("with_delivery");
            int price = result.getInt("price");
            rentalDTO = new RentalDTO(rental_id, studentID, instrumentID, start.toString(), end.toString(), withDelivery, price);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } finally {
            closeResultSet("Failed", result);
            return rentalDTO;
        }
    }

    public boolean instrumentIsRented(String instrumentID) throws SchoolDBException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        boolean isRented = false;
        try {
            isInstrumentRentedStmt.setInt(1,Integer.parseInt(instrumentID));
            result = isInstrumentRentedStmt.executeQuery();
            if(result.next()){
                isRented = result.getBoolean(1);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
            return isRented;
        }
    }

    public int nrOfActiveStudentRentals(String studentId) throws SchoolDBException {
        String failureMsg = "Could not rent the specified instrument.";
        ResultSet result = null;
        int activeRentals = 0;
        try {
            getNrOfActiveStudentRentalsByIDStmt.setInt(1,Integer.parseInt(studentId));
            result = getNrOfActiveStudentRentalsByIDStmt.executeQuery();
            if(result.next()){
                activeRentals = result.getInt(1);

            } else {
                // throw error (something went wrong, could not find student)
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            closeResultSet(failureMsg, result);
            return activeRentals;
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
        getAllStudentIDsStmt = connection.prepareStatement(
                "select student_id from student;"
        );
        getAllActiveRentalsStmt = connection.prepareStatement(
                "select rental_id,student_id,i.instrument_id,start_time,end_time,with_delivery,rental.price from rental join instrument i on i.instrument_id = rental.instrument_id where is_rented=true"

        );
        getNrOfActiveStudentRentalsByIDStmt = connection.prepareStatement(
                "select count(*) from rental join instrument i on i.instrument_id = rental.instrument_id where student_id=? and is_rented=true"
        );

        isInstrumentRentedStmt = connection.prepareStatement(
                "select count(instrument_id) > 0 from instrument where instrument_id = ? AND is_rented = true;"
        );

        getInstrumentByIDStmt = connection.prepareStatement(
                "select * from instrument where instrument_id = ?");

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
                "3ge4wujj");
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