package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.RuleItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Rule screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class RuleApi extends IrisApi {

    public RuleApi(Context context) {
        super(context);
    }

    /**
     * Get current rules
     * @return List of rules
     */
    public List<RuleItem> getRules() {
        List<RuleItem> rules = new ArrayList<>();
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:rule:\",\"isRequest\":true},\"payload\":{\"messageType\":\"rule:ListRules\",\"attributes\":{\"placeId\":\"@HUBID@\"}}}");
            JSONObject jsonObj = new JSONObject(retVal.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("rules");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                RuleItem rule = new RuleItem();
                rule.setRuleName(jsonObj.getString("rule:name"));
                rule.setRuleId(jsonObj.getString("base:id"));
                rule.setEnabled(jsonObj.getString("rule:state"));
                rule.setRuleDescription(jsonObj.getString("rule:description"));
                rules.add(rule);
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all rules success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rules;
    }

    /**
     * Toggle a rule
     * @param ruleID
     * @param status (ENABLED, DISABLED)
     */
    //TODO: Not Working (NullPointerException)
    public void toggleRuleStatus(String ruleID, String status) {
        try {
            if("ENABLED".equalsIgnoreCase(status)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:rule:" + ruleID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"rule:Enable\",\"attributes\":{}}}");
            } else if("DISABLED".equalsIgnoreCase(status)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:rule:" + ruleID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"rule:Disable\",\"attributes\":{}}}");
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Toggled rule to " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
