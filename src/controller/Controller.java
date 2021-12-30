package controller;

import integration.SchoolDAO;
import integration.SchoolDBException;
import model.InstrumentDTO;
import model.InstrumentException;
import model.RentException;
import model.RentalDTO;

import java.util.List;

public class Controller {
    private final SchoolDAO schoolDB;

    public Controller() throws SchoolDBException {
        schoolDB = new SchoolDAO();
    }

    public List<InstrumentDTO> listAvailableInstrumentsByKind(String kind) throws InstrumentException {
        try {
            return schoolDB.listAvailableInstrumentsByKind(kind);
        } catch (Exception e) {
            throw new InstrumentException("Could not search for instruments.", e);
        }
    }

    public void rentInstrument(String studentID, String instrumentID) throws RentException {
        try {
            schoolDB.rentInstrumentByIDWithStudentID(studentID, instrumentID);
        } catch (Exception e) {
            throw new RentException("Rental could not be performed.", e);
        }
    }

    public List<RentalDTO> getRentalsForStudent(String studentID) throws RentException {
        try {
            return schoolDB.getRentalsByStudentID(studentID);
        } catch (Exception e) {
            throw new RentException("Rentals could not be acquired.", e);
        }
    }

    public void terminateRentalByID(String rentalID) throws RentException {
        try {
            schoolDB.terminateRentalByID(rentalID);
        } catch (Exception e) {
            throw new RentException("Rental could not be terminated.");
        }
    }
}
