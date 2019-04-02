package com.eightydegreeswest.irisplus.model;

import java.io.Serializable;

public class RuleItem implements Serializable {

	private static final long serialVersionUID = 5663806579755915134L;
	
	private String ruleName;
    private String ruleId;
    private String enabled;
    private String ruleDescription;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }
}