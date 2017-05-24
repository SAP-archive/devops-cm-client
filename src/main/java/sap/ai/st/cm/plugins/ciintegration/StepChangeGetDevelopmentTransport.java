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

    private final String TargetEnvVariable;

    public String getTargetEnvVariable() {
        return TargetEnvVariable;
    }

    @DataBoundConstructor
    public StepChangeGetDevelopmentTransport(String ChangeID, String TargetEnvVariable) {

        super(ChangeID);

        this.TargetEnvVariable = TargetEnvVariable;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener taskListener) throws InterruptedException, IOException {

        super.perform(run, fp, lnchr, taskListener);

        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration);

            ArrayList<CMODataTransport> transportList = odataClient.getChangeTransports(this.ChangeID);

            CMODataTransport selectedTransport = null;

            for (CMODataTransport transport : transportList) {
                taskListener.getLogger().println("Transport " + transport.getTransportID() + (transport.isModifiable() ? " is modifiable" : "is not modfiable"));

                if (selectedTransport == null && transport.isModifiable()) {
                    selectedTransport = transport;
                }
            }

            if (selectedTransport != null) {

                taskListener.getLogger().println("Transport " + selectedTransport.getTransportID() + " selected");

                //run.getEnvironment(taskListener).put(this.TargetEnvVariable, selectedTransport.getTransportID());
                
                run.addAction(new VariableInjectionAction(this.TargetEnvVariable, selectedTransport.getTransportID()));

                taskListener.getLogger().println("Variable " + this.TargetEnvVariable + " set to " + run.getEnvironment(taskListener).get(this.TargetEnvVariable));
            }

        } catch (Exception e) {

            taskListener.getLogger().println(e);

            throw new IOException(e);

        }

    }

    @Extension
    public static final class ChangeCheckInDevelopmentDescriptor extends BuildStepDescriptor<Builder> {

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
