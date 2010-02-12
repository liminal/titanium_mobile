/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package org.appcelerator.titanium;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiActivitySupportHelper;
import org.appcelerator.titanium.view.TitaniumCompositeLayout;
import org.appcelerator.titanium.view.TitaniumCompositeLayout.TitaniumCompositeLayoutParams;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class TiRootActivity extends ActivityGroup
	implements TiActivitySupport
{
	private static AtomicInteger windowIdGenerator;

	protected TiContext tiContext;
	protected TiActivitySupportHelper supportHelper;
	protected TitaniumCompositeLayout rootLayout;

	public static class TiActivityRef
	{
		public String key;
		public Activity activity;
	}

	public TiRootActivity() {
		super(true); // Allow multiple activities
		windowIdGenerator = new AtomicInteger(0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.checkpoint("checkpoint, on root activity create.");

		TiApplication host = getTiApp();
		host.setRootActivity(this);
		tiContext = TiContext.createTiContext(this, null, null);

		rootLayout = new TitaniumCompositeLayout(this);
		setContentView(rootLayout);

		runOnUiThread(new Runnable(){

			public void run() {
				try {
					tiContext.evalFile("app.js");
				} catch (IOException e) {
					// TODO be more helpful
					e.printStackTrace();
					finish();
				}
			}
		});
	}

	protected TiApplication getTiApp() {
		return (TiApplication) getApplication();
	}

	public String openWindow(Intent intent)
	{
		LocalActivityManager lam = getLocalActivityManager();

		String windowId = "window$$" + windowIdGenerator.incrementAndGet();

		Window w = lam.startActivity(windowId, intent);
//		Activity activity = lam.getActivity(windowId);
//		if (activity != null) {
//			if (activity instanceof TiActivity) {
//				TitaniumCompositeLayout layout = ((TiActivity) activity).getLayout();
//				rootLayout.addView(layout, layout.getLayoutParams());
//			} else {
//				rootLayout.addView(w.getDecorView());
//			}
//		} else {
//			windowId = null;
//		}

		return windowId;
	}

	public void addWindow(String windowId, TitaniumCompositeLayoutParams params)
	{
		LocalActivityManager lam = getLocalActivityManager();
		Activity activity = lam.getActivity(windowId);
		if (activity != null) {
			View decor = activity.getWindow().getDecorView();
			rootLayout.addView(decor, params);
		}
	}

	public void closeWindow(String windowId) {
		LocalActivityManager lam = getLocalActivityManager();
		Activity activity = lam.getActivity(windowId);
		if (activity != null) {
			View decor = activity.getWindow().getDecorView();
			rootLayout.removeView(decor);
		}
		lam.destroyActivity(windowId, true);
	}

	// Activity Support
	public int getUniqueResultCode() {
		if (supportHelper == null) {
			this.supportHelper = new TiActivitySupportHelper(this);
		}
		return supportHelper.getUniqueResultCode();
	}

	public void launchActivityForResult(Intent intent, int code, TiActivityResultHandler resultHandler)
	{
		if (supportHelper == null) {
			this.supportHelper = new TiActivitySupportHelper(this);
		}
		supportHelper.launchActivityForResult(intent, code, resultHandler);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		supportHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

//	@Override
//	public void finishFromChild(Activity child) {
//		//super.finishFromChild(child);
//		finish();
//	}

	// Lifecyle
	@Override
	protected void onStart() {
		super.onStart();

		tiContext.dispatchOnStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.checkpoint("checkpoint, on root activity resume.");
		tiContext.dispatchOnResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		tiContext.dispatchOnPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		tiContext.dispatchOnStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		tiContext.dispatchOnDestroy();
	}
}
