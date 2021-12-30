package startup;

import controller.Controller;
import integration.SchoolDBException;
import view.BlockingInterpreter;

public class Main {
    public static void main(String[] args) {
        try {
            BlockingInterpreter interpreter = new BlockingInterpreter(new Controller());
            interpreter.handleCmds();
        } catch (SchoolDBException e) {
            e.printStackTrace();
        }
    }
}
