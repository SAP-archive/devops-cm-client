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

    private final String ChangeStatus;

    public String getChangeStatus() {
        return ChangeStatus;
    }

    @DataBoundConstructor
    public StepChangeCheckStatus(String ChangeID, String ChangeStatus) {

        super(ChangeID);

        this.ChangeStatus = ChangeStatus;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener taskListener) throws InterruptedException, IOException {
        
        super.perform(run, fp, lnchr, taskListener);

        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration);

            CMODataChange change = odataClient.getChange(this.ChangeID);

            String status = change.getStatus();

            if (status.equals(this.ChangeStatus)) {

                taskListener.getLogger().println("Change is in status " + this.ChangeStatus);

            } else {

                throw new InterruptedException("Change is in status " + status);

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
            return "SAP Change Management: Check Change Status";
        }
    }
}
