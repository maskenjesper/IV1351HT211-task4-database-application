package view;

import java.util.List;
import java.util.Scanner;
import controller.Controller;
import model.InstrumentDTO;
import model.InstrumentException;
import model.RentException;

/**
 * Reads and interprets user commands. This command interpreter is blocking, the user
 * interface does not react to user input while a command is being executed.
 */
public class BlockingInterpreter {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller ctrl;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all operations.
     *
     * @param ctrl The controller used by this instance.
     */
    public BlockingInterpreter(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Stops the commend interpreter.
     */
    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands. This method will not return until the
     * UI has been stopped. The UI is stopped either when the user gives the
     * "quit" command, or when the method <code>stop()</code> is called.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        System.out.println("\nWelcome to Soundgood music schools rental service!");
        System.out.println("Type help for a list of commands.");
        while (keepReceivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case HELP:
                        System.out.println("help: Shows commands and what they do.");
                        System.out.println("quit: Quits the application.");
                        System.out.println("list <kind>: Shows list of instruments of specified kind that are available to rent.");
                        System.out.println("rent <studentID> <instrumentID>: Creates a one month rental for the specified student and instrument.");
                        break;
                    case QUIT:
                        keepReceivingCmds = false;
                        break;
                    case LIST:
                        try {
                            List<InstrumentDTO> instruments = ctrl.listAvailableInstrumentsByKind(cmdLine.getParameter(0));
                            if (instruments.size() == 0)
                                System.out.println("No instruments of this kind");
                            else {
                                System.out.println("Available instruments:");
                                for (InstrumentDTO instrument : instruments)
                                    System.out.println(instrument);
                            }
                        } catch (InstrumentException ie) {
                            System.out.println("Instruments could not be listed.");
                        }
                        break;
                    case RENT:
                        try {
                            ctrl.rentInstrument(cmdLine.getParameter(0), cmdLine.getParameter(1));
                            System.out.println("A rental has been created successfully!");
                        } catch (RentException re) {
                            System.out.println("The instrument could not be rented.");
                        }
                        break;
                    default:
                        System.out.println("illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String readNextLine() {
        System.out.print(PROMPT);
        return console.nextLine();
    }
}