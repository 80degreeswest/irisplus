package com.eightydegreeswest.irisplus.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.tasks.AutomationTask;

/**
 * Created by ybelenitsky on 5/30/2015.
 */
public class VoiceCommandInterpreter {

    private SharedPreferences mSharedPrefs;
    private IrisPlusLogger logger = new IrisPlusLogger();

    public void processVoiceCommand(Context context, String spokenText) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        logger.log(IrisPlusConstants.LOG_INFO, "Text input: " + spokenText);
        try {
            String keywords = "set device toggle turn to degrees hours mode scene ";
            spokenText = spokenText.replaceAll(" degrees", "");
            spokenText = spokenText.replaceAll(" hours", "");
            spokenText = spokenText.replaceAll("rain delay", "raindelay");
            spokenText = spokenText.replaceAll("mode", "");
            spokenText = spokenText.replaceAll("tonight", "night");
            spokenText = spokenText.replaceAll("partial", "night");
            spokenText = spokenText.replaceAll("disarm alarm", "alarm home");
            spokenText = spokenText.replaceAll("arm alarm", "alarm away");
            logger.log(IrisPlusConstants.LOG_INFO, "Processed text input: " + spokenText);

            //Parse spoken text into device name and the command
            String[] speechResult = spokenText.split(" ");
            if(speechResult.length > 1) {
                String deviceName = "";
                String newStatus = "";
                for(int i = 0; i < speechResult.length - 1; i++) {
                    if(!keywords.contains(speechResult[i].toLowerCase())) {
                        deviceName = deviceName + speechResult[i] + " ";
                    }
                }
                deviceName = deviceName.trim();
                newStatus = speechResult[speechResult.length - 1];
                //Toast.makeText(context, "Set " + deviceName + " to " + newStatus, Toast.LENGTH_SHORT).show();
                TaskHelper.execute(new AutomationTask(context, deviceName, newStatus, true));
            } else {
                Toast.makeText(context, "Could not recognize speech command from \"" + spokenText + ". \" Please try again!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could not recognize speech command from \"" + spokenText + ". \" Please try again!", Toast.LENGTH_LONG).show();
        }
    }
}
