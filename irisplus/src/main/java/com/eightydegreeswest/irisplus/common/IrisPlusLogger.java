package com.eightydegreeswest.irisplus.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.eightydegreeswest.irisplus.BuildConfig;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.HistoryItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class IrisPlusLogger {

	private String filename = Environment.getExternalStorageDirectory().toString() + "/irisplus.txt";
	private boolean debug = false;
	private File logFile;

	public void log(String level, String text) {
		if(debug) {
            try {
				logFile = new File(filename);
				if (!logFile.exists()) {
					try {
						logFile.createNewFile();
						BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
						buf.append("IRIS+ " + level.toUpperCase() + " (" + new Date() + "): " + "Created a new log file: " + filename);
						buf.newLine();
						buf.close();
					} catch (IOException e) {
						//e.printStackTrace();
					}
				}
				if(debug) {
					// BufferedWriter for performance, true to set append to file flag
					BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
					buf.append("IRIS+ " + level.toUpperCase() + " (" + new Date() + "): " + text);
					buf.newLine();
					buf.close();
				}
			} catch (IOException e) { }
		} else if(BuildConfig.DEBUG) {
			Log.i("IRIS+", level.toUpperCase() + " (" + new Date() + "): " + text);
		}
	}

	public List<HistoryItem> getIrisPlusHistoryItems() {
		List<HistoryItem> historyItemList = new ArrayList<>();
		try {
			Context mContext = IrisPlus.getContext();
			SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			int numberOfItems = 5;//Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_HISTORY_ITEMS, "5"));
			//Load cached list
			FileInputStream fileInputStream = mContext.openFileInput("irisplus-app-history-list.dat");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			List<HistoryItem> historyItemListFromCache = (ArrayList<HistoryItem>) objectInputStream.readObject();
			objectInputStream.close();
			if(historyItemListFromCache != null && historyItemListFromCache.size() > 0) {
				Collections.sort(historyItemListFromCache, new Comparator<HistoryItem>() {
					@Override
					public int compare(final HistoryItem object1, final HistoryItem object2) {
						return object2.getDate().compareTo(object1.getDate());
					}
				} );
				for(HistoryItem historyItem : historyItemListFromCache) {
					if(numberOfItems > 0) {
						historyItemList.add(historyItem);
						numberOfItems--;
					} else {
						break;
					}
				}
			}
		} catch(Exception e) {
			log(IrisPlusConstants.LOG_ERROR, "Could not get Iris+ history items." + e);
		}
		return historyItemList;
	}

	public void addIrisPlusHistoryItem(String str) {
		List<HistoryItem> historyItemList = new ArrayList<>();
		try {
			Context mContext = IrisPlus.getContext();
			try {
				FileInputStream fileInputStream = mContext.openFileInput("irisplus-app-history-list.dat");
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				historyItemList = (ArrayList<HistoryItem>) objectInputStream.readObject();
				objectInputStream.close();
			} catch (Exception e) {
				//File doesnt exist?
			}

			if(historyItemList == null) {
				historyItemList = new ArrayList<>();
			}

			HistoryItem historyItem = new HistoryItem();
			historyItem.setDate(Long.toString(new Date().getTime()));
			historyItem.setDescription(str);
			historyItemList.add(historyItem);

			Collections.sort(historyItemList, new Comparator<HistoryItem>() {
				@Override
				public int compare(final HistoryItem object1, final HistoryItem object2) {
					return object1.getDate().compareTo(object2.getDate());
				}
			} );

			if(historyItemList.size() > 100) {
				historyItemList.remove(0);
			}

			if(historyItemList.size() > 100) {
				historyItemList.remove(0);
			}

			if(historyItemList.size() > 100) {
				historyItemList.remove(0);
			}

			FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-app-history-list.dat", Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(historyItemList);
			objectOutputStream.close();
		} catch(Exception e) {
			log(IrisPlusConstants.LOG_ERROR, "Could not set Iris+ history item." + e);
		}
	}
	
	public void emailLog(Context context) {   
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("vnd.android.cursor.dir/email");
		String to[] = {"support@80degreeswest.com"};
		sharingIntent.putExtra(Intent.EXTRA_EMAIL, to);
		sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:" + filename));
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT,"Iris+ Logs");
		//sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(sharingIntent, "Send email"));
	}
	
	public void deleteLog() {
		File logFile = new File(filename);
		if (logFile.exists()) {
			try {
				logFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
