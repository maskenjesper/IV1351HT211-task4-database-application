package controller;

import integration.SchoolDAO;
import model.InstrumentDTO;

import java.util.List;

public class Controller {
    private final SchoolDAO schoolDB;

    public Controller() {
        schoolDB = new SchoolDAO();
    }

}
