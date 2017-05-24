package sap.ai.st.cm.plugins.ciintegration;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.File;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public class StepUploadFileToTransport extends StepAbstract {

    private final String FilePath;
    private final String TransportID;
    private final String ApplicationID;

    public String getTransportID() {
        return TransportID;
    }

    public String getApplicationID() {
        return ApplicationID;
    }

    public String getFilePath() {
        return FilePath;
    }

    @DataBoundConstructor
    public StepUploadFileToTransport(String ChangeID, String TransportID, String FilePath, String ApplicationID) {

        super(ChangeID);

        this.FilePath = FilePath;
        this.TransportID = TransportID;
        this.ApplicationID = ApplicationID;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {

        super.perform(run, fp, launcher, taskListener);

        EnvVars environment = run.getEnvironment(taskListener);

        try {

            String transportID = environment.expand(this.TransportID);
            String filePath = fp.getRemote() + File.separator + environment.expand(this.FilePath);
            String applicationID = environment.expand(this.ApplicationID);

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration);

            taskListener.getLogger().println("Uploading " + filePath + " to transport " + transportID + " for application " + applicationID);

            odataClient.uploadFileToTransport(transportID, filePath, applicationID);

            taskListener.getLogger().println("Uploaded " + filePath + " to transport " + transportID + " for application " + applicationID);

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
            return "SAP Change Management: Upload file to transport";
        }
    }
}
