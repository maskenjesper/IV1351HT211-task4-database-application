package startup;

import controller.Controller;
import integration.SchoolDBException;
import view.BlockingInterpreter;

/**
 * Startup class of the application.
 */
public class Main {
    /**
     * Instantiates a blocking interpreter and makes it start handling commands.
     * @param args Command line parameters
     */
    public static void main(String[] args) {
        try {
            BlockingInterpreter interpreter = new BlockingInterpreter(new Controller());
            interpreter.handleCmds();
        } catch (SchoolDBException e) {
            e.printStackTrace();
        }
    }
}
