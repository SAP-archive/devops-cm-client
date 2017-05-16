package sap.ai.st.cm.plugins.ciintegration;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

public class VariableInjectionAction implements EnvironmentContributingAction {

    private final String key;
    private final String value;

    public VariableInjectionAction(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        if (env != null && key != null && value != null) {
            env.put(key, value);
        }
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "VariableInjectionAction";
    }

    @Override
    public String getUrlName() {
        return null;
    }

}
