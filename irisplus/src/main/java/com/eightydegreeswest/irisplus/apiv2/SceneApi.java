package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;
import com.eightydegreeswest.irisplus.model.SceneItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Scene screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class SceneApi extends IrisApi {

    public SceneApi(Context context) {
        super(context);
    }

    /**
     * Get current scenes
     * @return List of scenes
     */
    public List<SceneItem> getScenes() {
        List<SceneItem> scenes = new ArrayList<>();
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:scene:\",\"isRequest\":true},\"payload\":{\"messageType\":\"scene:ListScenes\",\"attributes\":{\"placeId\":\"@HUBID@\"}}}");
            JSONObject jsonObj = new JSONObject(retVal.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("scenes");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                SceneItem scene = new SceneItem();
                scene.setSceneName(jsonObj.getString("scene:name"));
                scene.setId(jsonObj.getString("base:id"));
                scene.setEnabled(jsonObj.getBoolean("scene:enabled"));
                scene.setFiring(jsonObj.getBoolean("scene:firing"));
                try {
                    scene.setLastFired(jsonObj.getLong("scene:lastFireTime"));
                } catch(Exception e) {
                    scene.setLastFired(0L);
                }
                scenes.add(scene);
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all scenes success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scenes;
    }

    /**
     * Run a scene
     * @param sceneID
     */
    public void runScene(String sceneID) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:scene:" + sceneID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"scene:Fire\",\"attributes\":{}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Run scene " + sceneID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run a scene by name
     * @param sceneName
     */
    public void runSceneByName(String sceneName) {
        try {
            List<SceneItem> scenes = this.getScenes();
            for(SceneItem scene : scenes) {
                if(scene.getSceneName().equalsIgnoreCase(sceneName)) {
                    this.runScene(scene.getId());
                    break;
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Run scene " + sceneName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a scene by name
     * @param sceneName
     */
    public SceneItem getSceneByName(String sceneName) {
        try {
            List<SceneItem> scenes = this.getScenes();
            for(SceneItem scene : scenes) {
                if(scene.getSceneName().equalsIgnoreCase(sceneName)) {
                    logger.log(IrisPlusConstants.LOG_DEBUG, "Found scene " + sceneName);
                    return scene;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
