package sap.ai.st.cm.plugins.ciintegration;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import org.kohsuke.stapler.DataBoundConstructor;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class StepChangeGetDevelopmentTransport extends StepAbstract {

    protected final boolean CreateNewOnDemand;

    public boolean getCreateNewOnDemand() {
        return CreateNewOnDemand;
    }

    @DataBoundConstructor
    public StepChangeGetDevelopmentTransport(String ChangeID, Boolean CreateNewOnDemand) {

        super(ChangeID);

        this.CreateNewOnDemand = CreateNewOnDemand;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener taskListener) throws InterruptedException, IOException {

        super.perform(run, fp, lnchr, taskListener);

        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration);

            ArrayList<CMODataTransport> transportList = odataClient.getChangeTransports(run.getEnvironment(taskListener).expand(this.ChangeID));

            CMODataTransport selectedTransport = null;

            for (CMODataTransport transport : transportList) {
                taskListener.getLogger().println("Transport " + transport.getTransportID() + (transport.isModifiable() ? " is modifiable" : "is not modfiable"));

                if (selectedTransport == null && transport.isModifiable()) {
                    selectedTransport = transport;
                    taskListener.getLogger().println("Transport " + selectedTransport.getTransportID() + " selected");
                    break;
                }
            }

            if (selectedTransport == null) {

                if (this.CreateNewOnDemand) {

                    selectedTransport = odataClient.createDevelopmentTransport(run.getEnvironment(taskListener).expand(this.ChangeID));

                } else {

                    throw new InterruptedException("No modifiable tranpsort found");
                }
                
            }

            CIIntegrationProperties properties = new CIIntegrationProperties(fp);

            properties.setDevelopmentTransportID(selectedTransport.getTransportID());

            properties = new CIIntegrationProperties(fp);

            properties.setProperty("TransportID", selectedTransport.getTransportID());

            taskListener.getLogger().println("Set current development transport to " + properties.getDevelopmentTransportID());

        } catch (Exception e) {

            taskListener.getLogger().println(e);

            throw new IOException(e);

        }

    }

    @Extension
    public static final class StepDescriptor extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "SAP Change Management: Get Development Transport";
        }
    }
}
