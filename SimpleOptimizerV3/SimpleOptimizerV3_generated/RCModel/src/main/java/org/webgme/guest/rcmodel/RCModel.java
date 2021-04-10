package org.webgme.guest.rcmodel;

import org.webgme.guest.rcmodel.rti.*;

import org.cpswt.config.FederateConfig;
import org.cpswt.config.FederateConfigParser;
import org.cpswt.hla.InteractionRoot;
import org.cpswt.hla.base.AdvanceTimeRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cpswt.utils.CpswtUtils;

// Define the RCModel type of federate for the federation.

public class RCModel extends RCModelBase {
    private final static Logger log = LogManager.getLogger();

    private double currentTime = 0;

    public RCModel(FederateConfig params) throws Exception {
        super(params);
    }

    // Initializing Parameters
    double currentTemp = 20.0;
    String scurrentTemp;
    String separator = ",";
    boolean receivedSimTime=false;
    String dataString="";

    private void checkReceivedSubscriptions() {
        InteractionRoot interaction = null;
        while ((interaction = getNextInteractionNoWait()) != null) {
            if (interaction instanceof SendModel) {
                handleInteractionClass((SendModel) interaction);
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
            //    ReceiveModel vReceiveModel = create_ReceiveModel();
            //    vReceiveModel.set_actualLogicalGenerationTime( < YOUR VALUE HERE > );
            //    vReceiveModel.set_dataString( < YOUR VALUE HERE > );
            //    vReceiveModel.set_federateFilter( < YOUR VALUE HERE > );
            //    vReceiveModel.set_originFed( < YOUR VALUE HERE > );
            //    vReceiveModel.set_sourceFed( < YOUR VALUE HERE > );
            //    vReceiveModel.sendInteraction(getLRC(), currentTime + getLookAhead());

            scurrentTemp = String.valueOf(currentTemp);
            
            dataString = scurrentTemp;
            
            
            ReceiveModel vReceiveModel = create_ReceiveModel();
            vReceiveModel.set_dataString(dataString);
            log.info("Sent receiveModel interaction with {}",  dataString);
            
            vReceiveModel.sendInteraction(getLRC());

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

            System.out.println(currentTime);

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

    private void handleInteractionClass(SendModel interaction) {
        ///////////////////////////////////////////////////////////////
        // TODO implement how to handle reception of the interaction //
        ///////////////////////////////////////////////////////////////
        String holder = null;
    	holder = interaction.get_dataString();
    	System.out.println("holder received as: " + holder);
    	
    	String vars[] = holder.split(separator);
    	System.out.println("vars[0]=" + vars[0]);
        receivedSimTime=true;
    }

    public static void main(String[] args) {
        try {
            FederateConfigParser federateConfigParser =
                new FederateConfigParser();
            FederateConfig federateConfig =
                federateConfigParser.parseArgs(args, FederateConfig.class);
            RCModel federate =
                new RCModel(federateConfig);
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
