package startup;

import controller.Controller;
import integration.SchoolDBException;
import view.BlockingInterpreter;
import view.Welcome;

public class Main {
    public static void main(String[] args) {
        Welcome.startText();
        try {
            BlockingInterpreter interpreter = new BlockingInterpreter(new Controller());
            interpreter.handleCmds();
        } catch (SchoolDBException e) {
            e.printStackTrace();
        }
    }
}
