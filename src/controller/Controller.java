package controller;

import integration.SchoolDAO;
import integration.SchoolDBException;
import model.InstrumentDTO;
import model.InstrumentException;

import java.util.ArrayList;
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
            throw new InstrumentException("Could not search for instruments", e);
        }
    }
}
