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
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;

public class StepChangeCheckInDevelopment extends Builder implements SimpleBuildStep {

    private final String ChangeID;
    
    public String getChangeID() {
        return ChangeID;
    }
    
    @DataBoundConstructor
    public StepChangeCheckInDevelopment(String ChangeID) {
        this.ChangeID = ChangeID;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener tl) throws InterruptedException, IOException {
        
        tl.getLogger().println(getDescriptor().getDisplayName());
        tl.getLogger().println("Change ID " + this.ChangeID);

        CIIntegrationGlobalConfiguration globalConfiguration = GlobalConfiguration.all().get(CIIntegrationGlobalConfiguration.class);

        if (globalConfiguration != null) {

            tl.getLogger().println("URL " + globalConfiguration.getServiceURL());
            tl.getLogger().println("User " + globalConfiguration.getServiceUser());
           
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
            return "SAP ChaRM: Check Change is in development";
        }
    }
}
