package com.eightydegreeswest.irisplus.apiv2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.BuildConfig;
import com.eightydegreeswest.irisplus.common.IrisPlusHelper;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.HubItem;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

@SuppressLint("DefaultLocale")
public class IrisApi {

	protected final String API_URL_LOGIN = "https://bc.irisbylowes.com/login";
	protected final String API_URL = "wss://bc.irisbylowes.com/";
	private HttpsURLConnection conn;
	private String username;    //primary username to login with
	private String password;
	private String irisAuthToken;
	private String hubID;
	protected static IrisPlusLogger logger = new IrisPlusLogger();
	private Context mContext;
	SharedPreferences mSharedPrefs;
	CookieManager cookieManager = new CookieManager();
	Map<String, String> httpHeaders = new HashMap<String, String>();
	int sdk = Build.VERSION.SDK_INT;
	public static String wsReturnValue = null;
	public static WebSocket ws = null;

	/**
	 * Initialize Iris API by setting cookie handler and the login credentials from the preferences
	 * @param context context
	 */
	public IrisApi(Context context) {
		try {
			mContext = context;
			mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			this.username = URLEncoder.encode(mSharedPrefs.getString(IrisPlusConstants.PREF_USERNAME, ""), "UTF-8");
			this.password = URLEncoder.encode(mSharedPrefs.getString(IrisPlusConstants.PREF_PASSWORD, ""), "UTF-8");
			CookieHandler.setDefault(cookieManager);
			logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
			logger.log(IrisPlusConstants.LOG_DEBUG, "Iris API Initialized. Sdk " + sdk);

			this.connectToWebsocket(API_URL + "websocket");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Initialize Iris API by setting cookie handler and the login credentials from the settings file
	 * @param username username
	 * @param password password
	 */
	public IrisApi(Context context, String username, String password) {
		try {
			mContext = context;
			mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			CookieHandler.setDefault(cookieManager);
			this.username = URLEncoder.encode(username, "UTF-8");
			this.password = URLEncoder.encode(password, "UTF-8");
			this.login();
			logger.log(IrisPlusConstants.LOG_DEBUG, "Iris API Initialized. Sdk " + sdk);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Login to Iris
	 * Sets session ID for subsequent commands
	 */
	private void login() throws Exception {
		try {
			URL obj = new URL(API_URL_LOGIN);
			conn = (HttpsURLConnection) obj.openConnection();
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(60000);
			conn.connect();

			BufferedReader rd;
			StringBuilder sb;
			String line;

			// Send post request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes("user=" + this.username + "&password=" + this.password);
			wr.flush();
			wr.close();

			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			sb = new StringBuilder();

			while ((line = rd.readLine()) != null) {
				sb.append(line).append('\n');
			}

			//Get full cookie
			//HttpCookie fullCookie = cookieManager.getCookieStore().getCookies().get(0);

			//Save token and place id
			SharedPreferences.Editor editor = mSharedPrefs.edit();

			//Get irisAuthToken
			irisAuthToken = conn.getHeaderField("Set-Cookie");
			//logger.log(IrisPlusConstants.LOG_INFO, "Cookie: " + irisAuthToken);

			if(irisAuthToken != null) {
				irisAuthToken = irisAuthToken.split("=")[1];
				irisAuthToken = irisAuthToken.split(";")[0];
				httpHeaders.put("Cookie", "irisAuthToken=" + irisAuthToken);
				editor.putString(IrisPlusConstants.PREF_TOKEN, irisAuthToken);
				editor.commit();
			}

			logger.log(IrisPlusConstants.LOG_DEBUG, "Login successful.");
			connectToWebsocket(API_URL + "websocket");
		} catch(IOException ioe) {
			logger.log(IrisPlusConstants.LOG_ERROR, "Timeout during Iris login.");
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
	}

	protected void connectToWebsocket(String url) throws URISyntaxException {
		try {
			hubID = mSharedPrefs.getString(IrisPlusConstants.PREF_HUB_ID, "");
			final String iftttKey = mSharedPrefs.getString(IrisPlusConstants.PREF_IFTTT_KEY, "");
			if(ws == null || !ws.isOpen()) {
				ws = new WebSocketFactory()
						.createSocket(url)
						.addHeader("Cookie", "irisAuthToken=" + mSharedPrefs.getString(IrisPlusConstants.PREF_TOKEN, ""))
						.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
						.addListener(new WebSocketAdapter() {
							// A text message arrived from the server.
							public void onTextMessage(WebSocket websocket, String message) {
								if(!message.contains("base:ValueChange") && !message.contains("sess:SetActivePlaceResponse") && !message.contains("SessionCreated")) {
									logger.log(IrisPlusConstants.LOG_DEBUG, "Message: " + message);
									wsReturnValue = message;
								} else if(message.contains("SessionCreated")) {
									setAccountPlaces(message);
								} else if(!"".equalsIgnoreCase(iftttKey) && message.contains("base:ValueChange")) {
									logger.log(IrisPlusConstants.LOG_DEBUG, "ValueChange Message: " + message);
									IrisPlusHelper.submitToIfttt(mContext, message);
								}
							}
						})
						.connect();
			}
		} catch(Exception e) {
			logger.log(IrisPlusConstants.LOG_ERROR, "Websocket exception. " + e);
		}
	}

	protected String sendToWebsocket(String url, String msg) throws Exception {
		wsReturnValue = null;
		this.setActivePlace();
		this.doSendToWebsocket(url, msg);

		for(int i = 0; i < 50; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(wsReturnValue != null) {
				logger.log(IrisPlusConstants.LOG_DEBUG, "Received from Iris: " + wsReturnValue);
				return wsReturnValue;
			}
		}

		return wsReturnValue;
	}

	protected void doSendToWebsocket(String url, String msg) throws Exception {
		msg = msg.replaceAll("@USERNAME@", this.username);
		msg = msg.replaceAll("@HUBID@", hubID);
		if(BuildConfig.DEBUG) {
			logger.log(IrisPlusConstants.LOG_DEBUG, "Sending the following query: " + msg + ".");
		} else {
			logger.log(IrisPlusConstants.LOG_DEBUG, "Sending query via web socket.");
		}
		try {
			if(ws == null) {
				this.connectToWebsocket(url);
			}
			ws.sendText(msg);
		} catch(Exception e) {
			logger.log(IrisPlusConstants.LOG_ERROR, "Websocket exception. " + e);
		}
	}

	public void setActivePlace() {
		try {
			this.doSendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:sess:\",\"isRequest\":true},\"payload\":{\"messageType\":\"sess:SetActivePlace\",\"attributes\":{\"placeId\":\"@HUBID@\"}}}");
			logger.log(IrisPlusConstants.LOG_DEBUG, "Set active placeID ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void setAccountPlaces(String placesJson) {
		logger.log(IrisPlusConstants.LOG_DEBUG, "Places: " + placesJson);
		//Get place ID
		try {
			JSONObject jsonObj = new JSONObject(placesJson);
			jsonObj = new JSONObject(jsonObj.getString("payload"));
			jsonObj = new JSONObject(jsonObj.getString("attributes"));
			JSONArray places = new JSONArray(jsonObj.getString("places"));
			List<HubItem> hubs = new ArrayList<>();
			for(int i = 0; i < places.length(); i++) {
				JSONObject place = places.getJSONObject(i);
				HubItem item = new HubItem();
				item.setId(place.getString("placeId"));
				item.setHubName(place.getString("placeName"));
				item.setRole(place.getString("role"));
				hubs.add(item);
				logger.log(IrisPlusConstants.LOG_INFO, "Found hub on the account: " + item.getHubName());
				if("OWNER".equalsIgnoreCase(item.getRole())) {
					SharedPreferences.Editor editor = mSharedPrefs.edit();
					hubID = item.getId();
					editor.putString(IrisPlusConstants.PREF_HUB_ID, item.getId()).commit();
					logger.log(IrisPlusConstants.LOG_INFO, "Using owner hub: " + item.getHubName());
				}
			}

			this.setActivePlace();

			try {
				//Cache list
				FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-hubs-list.dat", Context.MODE_PRIVATE);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(hubs);
				objectOutputStream.close();
			} catch (Exception cacheException) {
				//Ignore
				cacheException.printStackTrace();
			}
		} catch (Exception e) {
			logger.log(IrisPlusConstants.LOG_ERROR, "error: Could not parse place ID. " + e.getMessage());
		}
	}

	public String getUsername() {
		return username;
	}

	public String getIrisAuthToken() {
		return irisAuthToken;
	}

	public String getHubID() {
		return hubID;
	}


}
