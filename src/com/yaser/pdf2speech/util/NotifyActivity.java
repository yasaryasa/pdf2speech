package com.yaser.pdf2speech.util;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NotifyActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create textview
		TextView txt = new TextView(this);
		// Set text
		txt.setText("You have successfully opened the activity associated with the notification!");
		// Set textview on the view
		setContentView(txt);
	}
}