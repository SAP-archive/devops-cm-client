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
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

public class StepChangeCreateDevelopmentTransport extends StepAbstract {

    protected final String Description;
    protected final String Owner;

    @DataBoundConstructor
    public StepChangeCreateDevelopmentTransport(String ChangeID, String Description, String Owner) {

        super(ChangeID);

        this.Description = Description;
        this.Owner = Owner;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener taskListener) throws InterruptedException, IOException {

        super.perform(run, fp, lnchr, taskListener);

        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration);

            CMODataTransport selectedTransport = odataClient.createDevelopmentTransportAdvanced(run.getEnvironment(taskListener).expand(this.ChangeID), run.getEnvironment(taskListener).expand(this.Description), run.getEnvironment(taskListener).expand(this.Owner));

            taskListener.getLogger().println("Created transport request " + selectedTransport.getTransportID());

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
            return "SAP Change Management: Create Development Transport";
        }
    }
}
