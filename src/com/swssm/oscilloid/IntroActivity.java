package com.swssm.oscilloid;

import android.app.Activity;
import android.content.*;
import android.os.*;

public class IntroActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro_oscilloid);
		
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Intent intent = new Intent(getApplicationContext(),MainActivity.class);
				startActivity(intent);
				finish();
			}
		};
		handler.sendEmptyMessageDelayed(0,1500);
	}
}
