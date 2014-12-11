package com.example.myrecording;

import java.io.IOException;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton.OnValueChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	MediaRecorder mAudioRecorder;
	MediaPlayer mPlayer;
	private String outputFile;

	ProgressBar bar;

	Button start;
	Button stop;

	RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.root , new FragRecorder());
		transaction.commit();
		
	}

	/*

	public void start(View v) {

		mAudioRecorder = new MediaRecorder();
		mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mAudioRecorder.setOutputFile(outputFile);

		try {
			mAudioRecorder.prepare();
			mAudioRecorder.start();
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		start.setEnabled(false);
		stop.setEnabled(true);

		// new Mytask().execute();

	}

	public void stop(View v) {
		try {
			mAudioRecorder.stop();
			mAudioRecorder.release();
			mAudioRecorder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		stop.setEnabled(false);
		start.setEnabled(true);

	}

	public void play(View v) {
		try {
			mPlayer = new MediaPlayer();
			mPlayer.setDataSource(outputFile);
			mPlayer.prepare();
			mPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void go(View v) {
		startActivity(new Intent(this, AudioRecord_Player.class));

	}

	class Mytask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				for (int i = 0; i < 100; i++) {
					Thread.sleep(100);
					publishProgress(i);
				}
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			bar.setProgress(values[0]);
		}
	}

	public void test(View v) {
		startActivity(new Intent(this, TestActivity.class));
	}
*/
}
