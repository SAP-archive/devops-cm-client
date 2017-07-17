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
import org.kohsuke.stapler.DataBoundConstructor;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataChange;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class StepChangeCheckStatus extends StepAbstract {

    private final boolean isInDevelopment;

    public boolean isInDevelopment() {
        return isInDevelopment;
    }

    @DataBoundConstructor
    public StepChangeCheckStatus(String ChangeID, boolean isInDevelopment) {

        super(ChangeID);

        this.isInDevelopment = isInDevelopment;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener taskListener) throws InterruptedException, IOException {
        
        super.perform(run, fp, lnchr, taskListener);

        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration.getServiceURL(),
                    this.globalConfiguration.getServiceUser(),
                    this.globalConfiguration.getServicePassword());

            CMODataChange change = odataClient.getChange(run.getEnvironment(taskListener).expand(this.ChangeID));

            boolean isInDevelopment = change.isInDevelopment();

            if (isInDevelopment == this.isInDevelopment ) {

                taskListener.getLogger().println("Change is in status 'development':" + isInDevelopment + ".");

            } else {

                throw new InterruptedException("Change is in unexpected development status: " + isInDevelopment + ".");

            }

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
            return "SAP Change Management: Check Change Status";
        }
    }
}
