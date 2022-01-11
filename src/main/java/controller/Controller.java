package controller;

import integration.SchoolDAO;
import integration.SchoolDBException;
import model.InstrumentDTO;
import model.InstrumentException;
import model.RentalException;
import model.RentalDTO;

import java.util.List;

/**
 * Controller class that handles communication with the SchoolDAO.
 */
public class Controller {
    private final SchoolDAO schoolDB;

    /**
     * Constructor that instantiates a new SchoolDAO to open up communication with the database.
     * @throws SchoolDBException
     */
    public Controller() throws SchoolDBException {
        schoolDB = new SchoolDAO();
    }

    /**
     * Queries the DAO for a list of instruments that are available for rental.
     * @param kind Determines what kind of instruments that are searched for.
     * @return List of DTOs from the result of the query.
     * @throws InstrumentException
     */
    public List<InstrumentDTO> listAvailableInstrumentsByKind(String kind) throws InstrumentException {
        try {
            return schoolDB.listAvailableInstrumentsByKind(kind);
        } catch (Exception e) {
            throw new InstrumentException("Could not search for instruments.", e);
        }
    }

    /**
     * Queries the DAO to create a new rental.
     * @param studentID Student in the rental.
     * @param instrumentID Instrument being rented.
     * @throws RentalException
     */
    public void rentInstrument(String studentID, String instrumentID) throws RentalException {
        try {
            schoolDB.rentInstrumentByIDWithStudentID(studentID, instrumentID);
        } catch (Exception e) {
            throw new RentalException("Rental could not be performed.", e);
        }
    }

    /**
     * Queries the DAO for a list of rentals for a specific student.
     * @param studentID Specified student
     * @return List of rentals
     * @throws RentalException
     */
    public List<RentalDTO> getRentalsForStudent(String studentID) throws RentalException {
        try {
            return schoolDB.getRentalsByStudentID(studentID);
        } catch (Exception e) {
            throw new RentalException("Rentals could not be acquired.", e);
        }
    }

    /**
     * Queries the DAO to delete a rental.
     * @param rentalID Specified rental.
     * @throws RentalException
     */
    public void terminateRentalByID(String rentalID) throws RentalException {
        try {
            schoolDB.terminateRentalByID(rentalID);
        } catch (Exception e) {
            throw new RentalException("Rental could not be terminated.", e);
        }
    }
}
