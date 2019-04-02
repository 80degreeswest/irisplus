package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.PetItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Pet screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class PetApi extends IrisApi {

    public PetApi(Context context) {
        super(context);
    }

    /**
     * Get all pets on the account
     * @return JSON representation of devices
     */
    public List<PetItem> getAllPets() {
        List<PetItem> pets = new ArrayList<PetItem>();
        try {
            String petsList=null;// = this.executeCommand(API_URL + "/users/" + "@USERNAME@" + "/widgets/petdoors", "GET", "");
            JSONObject jsonObj = new JSONObject(petsList);
            JSONArray jsonArray = new JSONArray(jsonObj.getString("petdoors"));

            for(int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                PetItem petItem = new PetItem();
                petItem.setId(jsonObj.getString("id"));
                petItem.setPetName(jsonObj.getString("name"));
                petItem.setState(jsonObj.getString("mode"));

                pets.add(petItem);
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all pets success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pets;
    }

    /**
     * Set specific pet door state
     * @param petID
     * @param state (LOCKED, UNLOCKED, AUTO)
     */
    public void setPetState(String petID, String state) {
        try {
            //this.executeCommand(API_URL + "/users/" + "@USERNAME@" + "/widgets/petdoors/" + petID + "/mode", "PUT", "mode=" + state.toUpperCase());
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set pet " + petID + " to " + state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
