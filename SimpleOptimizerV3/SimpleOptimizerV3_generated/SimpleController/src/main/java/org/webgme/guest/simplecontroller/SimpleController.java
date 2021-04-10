package org.webgme.guest.simplecontroller;

import org.webgme.guest.simplecontroller.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import org.cpswt.utils.CpswtUtils;

// Define the SimpleController type of federate for the federation.

public class SimpleController extends SimpleControllerBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public SimpleController(FederateConfig params) throws Exception {
        super(params);
    }

    // Initializing parameters
    int num1, num2, sum =1;
    String snum1 = "1", snum2 = "1";
    String separator = ",";
    String dataString="";
    boolean receivedSimTime=false;

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof ReceiveModel) {
                handleInteractionClass((ReceiveModel) interaction);
            }
            else {
                log.debug("unhandled interaction: {}", interaction.getClassName());
            }
        }
    }

    private void execute() throws Exception {
        if(super.isLateJoiner()) {
            log.info("turning off time regulation (late joiner)");
            currentTime = super.getLBTS() - super.getLookAhead();
            super.disableTimeRegulation();
        }

        /////////////////////////////////////////////
        // TODO perform basic initialization below //
        /////////////////////////////////////////////

        AdvanceTimeRequest atr = new AdvanceTimeRequest(currentTime);
        putAdvanceTimeRequest(atr);

        if(!super.isLateJoiner()) {
            log.info("waiting on readyToPopulate...");
            readyToPopulate();
            log.info("...synchronized on readyToPopulate");
        }

        ///////////////////////////////////////////////////////////////////////
        // TODO perform initialization that depends on other federates below //
        ///////////////////////////////////////////////////////////////////////

        if(!super.isLateJoiner()) {
            log.info("waiting on readyToRun...");
            readyToRun();
            log.info("...synchronized on readyToRun");
        }

        startAdvanceTimeThread();
        log.info("started logical time progression");

        while (!exitCondition) {
            atr.requestSyncStart();
            enteredTimeGrantedState();

            ////////////////////////////////////////////////////////////
            // TODO send interactions that must be sent every logical //
            // time step below                                        //
            ////////////////////////////////////////////////////////////

            // Set the interaction's parameters.
            //
            //    SendModel vSendModel = create_SendModel();
            //    vSendModel.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vSendModel.set_dataString( < YOUR VALUE HERE > );
            //    vSendModel.set_federateFilter( < YOUR VALUE HERE > );
            //    vSendModel.set_originFed( < YOUR VALUE HERE > );
            //    vSendModel.set_sourceFed( < YOUR VALUE HERE > );
            //    vSendModel.sendInteraction(getLRC(), currentTime + getLookAhead());

            // removing time delay...
            while (!receivedSimTime){
                log.info("waiting to receive SimTime...");
                synchronized(lrc){
                    lrc.tick();
                } 
                checkReceivedSubscriptions();
                if(!receivedSimTime){
                    CpswtUtils.sleep(1000);
                }
            }
            receivedSimTime = false;
            // ...........

            String s = null;
            try {

                Process p = Runtime.getRuntime().exec("python ./energyOpt.py ";

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                System.out.println("Here is the result");
                s = stdInput.readLine();
                System.out.println(s);
                dataString = s;
                System.out.println(dataString);

              
//                while ((s = stdInput.readLine()) != null) {
//                    System.out.println(s);
//                    dataString = s;
//                    System.out.println(dataString);
//                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(currentTime);
            System.out.println(dataString);

            SendModel vSendModel = create_SendModel();
            vSendModel.set_dataString(dataString);
            log.info("Sent sendModel interaction with {}", dataString);
            
            vSendModel.sendInteraction(getLRC());

            ////////////////////////////////////////////////////////////////////
            // TODO break here if ready to resign and break out of while loop //
            ////////////////////////////////////////////////////////////////////

            if (!exitCondition) {
                currentTime += super.getStepSize();
                AdvanceTimeRequest newATR =
                    new AdvanceTimeRequest(currentTime);
                putAdvanceTimeRequest(newATR);
                atr.requestSyncEnd();
                atr = newATR;
            }
        }

        // call exitGracefully to shut down federate
        exitGracefully();

        //////////////////////////////////////////////////////////////////////
        // TODO Perform whatever cleanups are needed before exiting the app //
        //////////////////////////////////////////////////////////////////////
    }

    private void handleInteractionClass(ReceiveModel interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        String holder;
    	holder = interaction.get_dataString();
    	System.out.println("holder received as: " + holder);
    	
    	String vars[] = holder.split(separator);
    	System.out.println("vars[0]=" + vars[0]);
    	
    	// snum1 = vars[0];
    	// snum2 = vars[1];
    	// System.out.println("Received random numbers as" + snum1 + ", " + snum2);
    	// log.info("Received random numbers as {}, {}", snum1, snum2);
    	receivedSimTime=true;
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            SimpleController federate =
                new SimpleController(federateConfig);
            federate.execute();
            log.info("Done.");
            System.exit(0);
        }
        catch (Exception e) {
            log.error(e);
            System.exit(1);
        }
    }
}
