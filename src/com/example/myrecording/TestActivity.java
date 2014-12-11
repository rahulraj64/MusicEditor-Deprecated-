package com.example.myrecording;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;

public class TestActivity extends  Activity {

	private static final String TAG = "TestActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		try {
			filesTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	

	
	
	
	public void filesTest() throws IOException
	{
//		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//		String fileName = "myTestFile.txt";
//		path += File.separator + fileName ;
//		File file = new File(path);
//		
		
		Intent intent = 
			      new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
			startActivity(intent);
	}
	
}

