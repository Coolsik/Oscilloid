package com.swssm.oscilloid;

import java.io.*;
import java.util.logging.*;

import android.os.*;
import android.util.*;

public class Logger {
	public static FileHandler logger = null;
	private static String filename = "ProjectName_Log";
	
	static boolean isExternalStorageAvailable = false;
	static boolean isExternalStorageWriteable = false;
	static String state = Environment.getExternalStorageState();
	
	public static void addRecordToLog(String message) { 
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			isExternalStorageAvailable = isExternalStorageWriteable = true;
		} else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			isExternalStorageAvailable = true;
			isExternalStorageWriteable = false;
		} else {
			isExternalStorageAvailable = isExternalStorageWriteable = false;
		}
		
		File dir = new File("/sdcard/Files/Project_Name");
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			if(!dir.exists()) {
				Log.d("Dir created ","Dir created");
				dir.mkdirs();
			}
			
			File logFile = new File("/sdcard/Files/Project_Name/"+filename+".txt");
			
			if(!logFile.exists()) {
				try {
					Log.d("File Created","File Created");
					logFile.createNewFile();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,true));
				buf.write(message+"\r\n");
				buf.newLine();
				buf.flush();
				buf.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
