package com.eightydegreeswest.irisplus.wear;

import com.eightydegreeswest.irisplus.common.VoiceCommandInterpreter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

public class WearService extends WearableListenerService {

    private static GoogleApiClient googleApiClient;
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private static final String WEAR_PATH = "/mobile";
    private String nodeId;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //System.out.println("FROM WEAR: " + messageEvent.getPath());
        if (messageEvent.getPath().equals(WEAR_PATH)) {
            nodeId = messageEvent.getSourceNodeId();
            String voiceText = new String(messageEvent.getData());
            //System.out.println("Voice data received from wear: " + voiceText);
            VoiceCommandInterpreter voiceCommandInterpreter = new VoiceCommandInterpreter();
            voiceCommandInterpreter.processVoiceCommand(this.getApplicationContext(), voiceText);
            //reply(WEAR_PATH);
        }
    }

    private void reply(final String path) {
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();

        if (googleApiClient != null && !(googleApiClient.isConnected() || googleApiClient.isConnecting()))
            googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        Wearable.MessageApi.sendMessage(googleApiClient, nodeId, path, null).await();
        googleApiClient.disconnect();
    }
}
