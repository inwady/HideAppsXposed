/*
 * Copyright (C) 2014 Marvin Frommhold for Hide Apps Xposed project (depressiveRobot@xda)
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.depressiverobot.android.xposed.hideappsxposed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.depressiverobot.android.xposed.hideappsxposed.R;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;

public class HideAppsXposedSettings extends ListActivity {
	
	public static final String TAG = HideAppsXposedSettings.class.getSimpleName();
	public static final String PACKAGE_NAME = HideAppsXposedSettings.class.getPackage().getName();
	public static final String PREFERENCES_NAME = HideAppsXposedSettings.class.getSimpleName();
	public static final String APPS_TO_HIDE_KEY = "HideAppsXposedSettings.APPS_TO_HIDE";
	
	public static final String LABEL_PACKAGENAME_SEPERATOR = "#$#";
	
	private PackageManager packageManager = null;
    private List<ResolveInfo> appsList = null;
    private ArrayList<Boolean> checkList = null;
    private Set<String> appsToHide = new HashSet<String>();
    private ApplicationAdapter listAdaptor = null;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        packageManager = getPackageManager();
 
        new LoadApplications().execute();
    }
    
    @SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
    public void onPause() {
    	super.onPause();
    	
    	Set<String> appsToSave = new HashSet<String>();
    	for (int i = 0; i < appsList.size(); i++) {
    		if (listAdaptor.getCheckList().get(i)) {
    			String label = appsList.get(i).loadLabel(packageManager).toString();
    			String packageName = appsList.get(i).activityInfo.packageName;
    			appsToSave.add(label + LABEL_PACKAGENAME_SEPERATOR + packageName);
    		}
    	}
    	
		SharedPreferences prefs = getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putStringSet(APPS_TO_HIDE_KEY, appsToSave);
    	editor.commit();
    }
    
    @SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
    @Override
    public void onResume() {
    	super.onResume();
    	
		SharedPreferences prefs = getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		this.appsToHide = prefs.getStringSet(APPS_TO_HIDE_KEY, new HashSet<String>());
    }
    
    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;
 
        @Override
        protected Void doInBackground(Void... params) {

        	// load all launcher activities 
        	final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            appsList = packageManager.queryIntentActivities(mainIntent, 0);
            
            // sort list of apps alphabetically
            Collections.sort(appsList, new ResolveInfo.DisplayNameComparator(packageManager));
            
            // create checklist containing information which apps should be marked as hidden
            checkList = new ArrayList<Boolean>(appsList.size());
        	for (int i = 0; i < appsList.size(); i++) {
        		String label = appsList.get(i).loadLabel(packageManager).toString();
        		String packageName = appsList.get(i).activityInfo.packageName;
        		if (appsToHide.contains(label + LABEL_PACKAGENAME_SEPERATOR + packageName)) {
        			checkList.add(true);
        		} else {
        			checkList.add(false);
        		}
            }
            
            listAdaptor = new ApplicationAdapter(HideAppsXposedSettings.this, R.layout.snippet_list_row, appsList, checkList);
 
            return null;
        }
 
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
 
        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listAdaptor);
            progress.dismiss();
            super.onPostExecute(result);
        }
 
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(HideAppsXposedSettings.this, null, "Loading application info...");
            super.onPreExecute();
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
