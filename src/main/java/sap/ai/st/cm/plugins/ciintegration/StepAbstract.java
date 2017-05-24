package sap.ai.st.cm.plugins.ciintegration;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import java.io.IOException;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;

public abstract class StepAbstract extends Builder implements SimpleBuildStep {

    protected final String ChangeID;
    protected final CIIntegrationGlobalConfiguration globalConfiguration;

    public String getChangeID() {
        return ChangeID;
    }

    public StepAbstract(String ChangeID) {

        this.ChangeID = ChangeID;

        CIIntegrationGlobalConfiguration configuration = GlobalConfiguration.all().get(CIIntegrationGlobalConfiguration.class);

        if (configuration != null) {

            this.globalConfiguration = configuration;

        } else {

            this.globalConfiguration = new CIIntegrationGlobalConfiguration();

        }
    }

    @Override
    public void perform(Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {

        taskListener.getLogger().println(getDescriptor().getDisplayName());
        taskListener.getLogger().println("Change ID " + this.ChangeID);

        taskListener.getLogger().println("URL " + this.globalConfiguration.getServiceURL());
        taskListener.getLogger().println("User " + this.globalConfiguration.getServiceUser());

        try {

            CMODataClient odataClient = new CMODataClient(this.globalConfiguration);

            odataClient.getChange(this.ChangeID);

        } catch (Exception e) {

            taskListener.getLogger().println(e);

            throw new IOException(e);

        }
    }
}
