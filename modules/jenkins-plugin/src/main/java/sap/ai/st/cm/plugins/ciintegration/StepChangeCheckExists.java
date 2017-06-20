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

public class StepChangeCheckExists extends StepAbstract {

    @DataBoundConstructor
    public StepChangeCheckExists(String ChangeID) {
        
        super(ChangeID);
    }

    @Override
    public void perform(Run<?, ?> run, FilePath filepath, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {        
        super.perform(run, filepath, launcher, taskListener);
        
        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration.getServiceURL(),
                    this.globalConfiguration.getServiceUser(),
                    this.globalConfiguration.getServicePassword());

            odataClient.getChange(run.getEnvironment(taskListener).expand(this.ChangeID));

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
            return "SAP Change Management: Check Change exists";
        }
    }
}
