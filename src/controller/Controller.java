package controller;

import integration.SchoolDAO;
import integration.SchoolDBException;
import model.InstrumentDTO;
import model.InstrumentException;
import model.RentException;

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

    public void rentInstrument(String userID, String instrumentID) throws RentException {
        try {
            schoolDB.rentInstrumentByIDWithUserID(userID, instrumentID);
        } catch (Exception e) {
            throw new RentException("Rental could not be performed.", e);
        }
    }
}
