package com.example.myrecording;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FragRecorder extends Fragment implements 
		RecordProgressbarUpdate, MediaSeekBarUpdate, OnSeekBarChangeListener

{
	

	private static String TAG = "FragRecord";

	ProgressBar recordProgressbar;
	ToggleButton btnRecord;
	RadioGroup mode;
	//WaveUtillities waveUtilities;
	Animation mAnimation;
	SeekBar mediaSeekBar;

	TextView initialTime;
	TextView finalTime;
	
	ImageButton btnPlay;
	
	AudioPlayerUtilities audioPlayerUtilities;

	private AudioManager audioManager;
	
	
	public FragRecorder() {
	
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.w(TAG, "onCreateView.") ;
		audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		View v = inflater.inflate(R.layout.frag_recorder, container, false);
		//waveUtilities = new WaveUtillities(this);
		audioPlayerUtilities = new AudioPlayerUtilities(this);
		initializeViews(v);
		return v;

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.w(TAG, "onResume.");
		//waveUtilities = new WaveUtillities(this);
		//audioPlayerUtilities = new AudioPlayerUtilities(this);


	}
	@Override
	public void onDestroy() {
		audioPlayerUtilities.removeUpdateHandlerCallBacks();
		audioPlayerUtilities.getMyMediaPlayer().release();
		super.onDestroy();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.w(TAG, "onStop.");
		//btnRecord.setChecked(false);

	}

	@Override
	public void onProgressbarUpdate(int updateValue) {
		recordProgressbar.setProgress(updateValue);

	}
	@Override public void onRecordingError(){ // running on worker thread.
		Log.w(TAG, "onError Listener ..");
		muteNotificationSounds(false);
		
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getActivity(), "Cannot perform recording at the time being.", Toast.LENGTH_SHORT).show();
				btnRecord.setChecked(false);
				
			}
		});
		
	}
	

	@Override
	public void onSeekBarUpdate(String currentDurationTimer,
			String totalDurationsTimer, int progressPercentage) {
		

		initialTime.setText(currentDurationTimer);
		finalTime.setText(totalDurationsTimer);
		mediaSeekBar.setProgress(progressPercentage);
		
		
	}
	
	public void initializeViews(View v) {

		createButtonAnimationObject();
		recordProgressbar = (ProgressBar) v.findViewById(R.id.progressBar1);
		btnRecord = (ToggleButton) v.findViewById(R.id.record);
		mode = (RadioGroup) v.findViewById(R.id.radioGroup1);
		initialTime = (TextView) v.findViewById(R.id.initialTime);
		finalTime = (TextView) v.findViewById(R.id.finalTime);
		mediaSeekBar = (SeekBar) v.findViewById(R.id.songProgressBar);
		btnPlay = (ImageButton) v.findViewById(R.id.btnPlay);		
		setViewListeners();
		
		// roundProgressbar = (ProgressBar) v.findViewById(R.id.progressBar2);
		// roundProgressbar.getIndeterminateDrawable().setColorFilter(0xf39c12,
		// android.graphics.PorterDuff.Mode.MULTIPLY);

	}

	public void animateRecordButton(boolean b) {

		if (b) {
			
						
			btnRecord.clearAnimation();
			btnRecord.setAnimation(mAnimation);
			
			

		} else {

			btnRecord.clearAnimation();
		}

	}

	public void createButtonAnimationObject() {
		mAnimation = new AlphaAnimation(1, 0);
		mAnimation.setDuration(200);
		mAnimation.setInterpolator(new LinearInterpolator());
		mAnimation.setRepeatCount(Animation.INFINITE);
		mAnimation.setRepeatMode(Animation.REVERSE);
	}

	public void setViewListeners() {
		
		mediaSeekBar.setOnSeekBarChangeListener(this);

		
		btnRecord.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (buttonView.isShown() && isChecked) {

					animateRecordButton(true);
					muteNotificationSounds(true);
					audioPlayerUtilities.startRecording();

				} else {
					animateRecordButton(false);
					muteNotificationSounds(false);
					audioPlayerUtilities.stopRecording();

				}
			}
		});

		mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Toast.makeText(getActivity(), checkedId + "",
						Toast.LENGTH_SHORT).show();

			}
		});
		
		btnPlay.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {

				//if(audioPlayerUtilities.getMyMediaPlayer()FragRecorder != null && audioPlayerUtilities.getMyMediaPlayer().ispla)
				audioPlayerUtilities.startPlaying(v);
			}
		});
		
		

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

		Log.w(TAG, "start tracking touching.");

		audioPlayerUtilities.removeUpdateHandlerCallBacks();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		Log.w(TAG, "stop tracking touching.");		
		audioPlayerUtilities.removeUpdateHandlerCallBacks();
		long totalDuration = audioPlayerUtilities.getMyMediaPlayer().getDuration();
		int currentPosition =audioPlayerUtilities.progressToTimer(seekBar.getProgress(), (int) totalDuration);		
		audioPlayerUtilities.getMyMediaPlayer().seekTo(currentPosition);
		if(audioPlayerUtilities.getMyMediaPlayer().isPlaying())
		audioPlayerUtilities.mHandler.postDelayed(audioPlayerUtilities.updateHandler, 100);
		else
		{
			// set timer with out playing

			String currentTime = audioPlayerUtilities.milliSecondsToTimer(currentPosition);
			initialTime.setText(currentTime);

		}
		
	}

	public void muteNotificationSounds(boolean state) {
		if (audioManager != null) {
			audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, state);
			audioManager.setStreamMute(AudioManager.STREAM_ALARM, state);
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, state);
		}
	}

	
	

}
