package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.HistoryItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the History screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class HistoryApi extends IrisApi {

    public HistoryApi(Context context) {
        super(context);
    }

    /**
     * Get history
     * @param limit
     * @param offset
     * @return JSON representation of history
     */
    public List<HistoryItem> getHistory(List<HistoryItem> historyItems, String limit, String offset) {
        try {
            if(offset != null && !"".equalsIgnoreCase(offset) && !"null".equalsIgnoreCase(offset)) {
                offset = ",\"token\":\"" + offset + "\"";
            } else {
                offset = "";
            }

            if(limit != null && !"".equalsIgnoreCase(limit) && !"0".equalsIgnoreCase(limit)) {
                limit = "\"limit\":\"" + limit + "\"";
            } else {
                limit = "\"limit\":\"100\"";
            }
            String historyData = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListHistoryEntries\",\"attributes\":{" + limit + offset + "}}}");
            JSONObject jsonObj = new JSONObject(historyData.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("results");
            String offsetToken = null;

            try {
                jsonObj.getString("nextToken");
                if(offset == "") {
                    historyItems = new ArrayList<HistoryItem>();
                }
            } catch (Exception e) {
                if(offset == "") {
                    historyItems = new ArrayList<HistoryItem>();
                }
            }

            for(int i = 0; i < jsonArray.length(); i++) {
                try {
                    jsonObj = new JSONObject(jsonArray.get(i).toString());
                    HistoryItem historyItem = new HistoryItem();
                    historyItem.setOffset(offsetToken);
                    historyItem.setDescription(jsonObj.getString("subjectName") + " " + jsonObj.getString("longMessage"));
                    historyItem.setDate(jsonObj.getString("timestamp"));
                    historyItem.setId(jsonObj.getString("subjectAddress"));
                    historyItems.add(historyItem);
                } catch (Exception e) {
                    //Ignore and continue
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get history success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return historyItems;
    }
}
