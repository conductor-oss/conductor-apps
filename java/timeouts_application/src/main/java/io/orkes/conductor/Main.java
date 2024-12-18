package io.orkes.conductor;

import java.util.Scanner;

import io.orkes.conductor.logic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * By default, the sweeper (periodically checks the state of workflows) runs every 30 seconds,
 * task timeouts may not occur immediately.
 * Client credentials to be placed inside ClientUtil under package io.orkes.conductor.util
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args)  {
        log.info("Enter a test number :-\n   " +
                "1. Test Global Timeouts\n   " +
                "2. Test Task level timeout\n   " +
                "3. Test Task level response timeout\n   " +
                "4. Test Task level poll timeout\n   " +
                "5. Test Global timeout with retries\n   " +
                "6. Test Global Timeouts with alerts");
        Scanner reader = new Scanner(System.in);
        int userInput = reader.nextInt();
        reader.close();

        switch (userInput){
            case 1: WorkflowGlobalTimeout.run(); break;
            case 2: WorkflowTaskTimeoutAfterWorkerPickup.run(); break;
            case 3: WorkflowTaskResponseTimeout.run(); break;
            case 4: WorkflowTaskTimeoutBeforeWorkerPickup.run(); break;
            case 5: WorkflowTaskTimeoutAfterWorkerPickupWithRetries.run(); break;
            case 6: WorkflowGlobalTimeoutWithAlerts.run(); break;
            default: log.info("Wrong test selected");
        }
    }
}