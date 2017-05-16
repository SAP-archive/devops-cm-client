package sap.ai.st.cm.plugins.ciintegration;

import hudson.Extension;
import hudson.util.FormValidation;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.QueryParameter;

@Extension
public class CIIntegrationGlobalConfiguration extends GlobalConfiguration {

    private String serviceURL;
    private String serviceUser;
    private String servicePassword;

    public String getServiceURL() {
        return serviceURL;
    }

    public String getServiceUser() {
        return serviceUser;
    }

    public String getServicePassword() {
        return servicePassword;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
        this.save();
    }

    public void setServiceUser(String serviceUser) {
        this.serviceUser = serviceUser;
        this.save();
    }

    public void setServicePassword(String servicePassword) {
        this.servicePassword = servicePassword;
        this.save();
    }

    public CIIntegrationGlobalConfiguration() {
        this.load();
    }

    public FormValidation doCheckServiceURL(@QueryParameter String value) throws IOException, ServletException {
        if (value.length() == 0) {
            return FormValidation.error("Please set a URL");
        }
        if (value.length() < 4) {
            return FormValidation.warning("Isn't the URL too short?");
        }
        return FormValidation.ok();
    }

}
