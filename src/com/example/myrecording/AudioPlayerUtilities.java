package com.example.myrecording;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;




	interface MediaSeekBarUpdate
	{
		public void onSeekBarUpdate(String currentDurationTimer, 
				String totalDurationsTimer, int progressPercentage);
	}
	
	
	

public class AudioPlayerUtilities extends WaveUtillities {

	MediaPlayer mediaPLayer;
	MediaSeekBarUpdate mediaSeekBarUpdate;
	
	ImageButton playPauseButton;

	public AudioPlayerUtilities(FragRecorder frag) {

		super(frag);
		mediaSeekBarUpdate = (MediaSeekBarUpdate) frag;
		mediaPLayer = new MediaPlayer();
		mediaPLayer.setOnCompletionListener(new OnCompletionListener() {			
			@Override
			public void onCompletion(MediaPlayer mp) {


				
				
				Log.w("AudioPlayerUtilities", "completed playing ..");
				playPauseButton.setImageResource(R.drawable.btn_play);
				mediaPLayer.seekTo(0);
				//if(mHandler != null) mHandler.removeCallbacks(updateHandler);

				//TODO : code for reset the initial time and progress of seek bar to zero
				// currently only setting the button image to play and stopping the handler .. bad .. 
				
				
			}
		});
			
		
		
		
		try {
			mediaPLayer.reset();
			mediaPLayer.setDataSource(getFilename());
			mediaPLayer.prepare();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void startPlaying(View v) {

		try {
			
			if(mediaPLayer == null ) return;
			
			if( !mediaPLayer.isPlaying())
			{
			playPauseButton = (ImageButton) v;	
			playPauseButton.setImageResource(R.drawable.btn_pause);
			mediaPLayer.start();
			mHandler.postDelayed(updateHandler, 100);
			
			}// else if(mediaPLayer != null && !mediaPLayer.isPlaying()){}
			
			else
			{
				pausePlaying(v);
				if(mHandler != null) mHandler.removeCallbacks(updateHandler);
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stopPlaying() {
		if(mediaPLayer != null) 
			{
			//mediaPLayer.stop();
			mediaPLayer.release();
			}

	}

	public void pausePlaying(View v) {
		playPauseButton.setImageResource(R.drawable.btn_play);
		if (mediaPLayer.isPlaying()) mediaPLayer.pause();
	}

	Runnable updateHandler = new Runnable() {

		@Override
		public void run() {

			long totalDuration = mediaPLayer.getDuration();
			long currentDuration = mediaPLayer.getCurrentPosition();
			Log.i("Recorder", "handler running. Currnt " + currentDuration
					+ "  total " + totalDuration);
			
			String totalDurationsTimer = milliSecondsToTimer(totalDuration);
			String currentDurationTimer = milliSecondsToTimer(currentDuration);
			int progressPercentage = getProgressPercentage(currentDuration, totalDuration);
			
			mediaSeekBarUpdate.onSeekBarUpdate(currentDurationTimer,
					totalDurationsTimer, progressPercentage);
			mHandler.postDelayed(this, 100);
			/*
			if (currentDuration <= totalDuration) {
				mHandler.postDelayed(this, 100);

			} else {
				mHandler.removeCallbacks(this);
				Log.i("Recorder", "elseeeeee");

			}
			*/

		}
	};
	
	public void removeUpdateHandlerCallBacks()
	{
		if(updateHandler != null) mHandler.removeCallbacks(updateHandler);
	}
	public MediaPlayer getMyMediaPlayer() { return mediaPLayer; }

}
