package com.example.myrecording;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AudioRecord_Player extends Activity implements OnClickListener{
	
	private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final String AUDIO_RECORDER_FILE = "M2";
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static int POS_POSITION;
    private static final String AUDIO_RECORDER_AMR = "M2converted.amr";
    
    Handler mHandler = new Handler();
    Utilities utilities;
//    private static final int AUDIO_SAMPLE_FREQ = 44100;
//    private static final int AUDIO_BUFFER_SIZE = 200000; 

    
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    
     Button start_record,pause_rec,stop_rec,play_rec,resume_rec;
     SeekBar seekbar ;
     MediaPlayer mPlayer;
     
     TextView initialTime ;
     TextView finalTime;
     
     Button rec;
     //private int seekBarMax = 1000;
     
     private static boolean isMediaPlaying; 
    
    /** Called when the activity is first created. */
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio__record__player);
        
        mPlayer = new MediaPlayer();
        utilities = new Utilities();
        
        start_record=(Button)findViewById(R.id.record);
        pause_rec=(Button)findViewById(R.id.pause);
        resume_rec=(Button)findViewById(R.id.resume);
        stop_rec=(Button)findViewById(R.id.stop);
        play_rec=(Button)findViewById(R.id.play);
        
        seekbar = (SeekBar) findViewById(R.id.seekBar1);
        //seekbar.setMax(seekBarMax);
        
        
        initialTime = (TextView)findViewById(R.id.textView1);
        finalTime = (TextView)findViewById(R.id.textView2);
        
        pause_rec.setEnabled(false);
        stop_rec.setEnabled(false);
        //play_rec.setEnabled(false);
        resume_rec.setEnabled(false);
        
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
      
        start_record.setOnClickListener(this);
        pause_rec.setOnClickListener(this);
        stop_rec.setOnClickListener(this);
        play_rec.setOnClickListener(this);
        resume_rec.setOnClickListener(this);
        
        /*
        mPlayer.setOnTimedTextListener(new OnTimedTextListener() {
			
			@Override
			public void onTimedText(MediaPlayer mp, TimedText text) {

				
			}
		});
		*/
        
        
    }
   
    private void deleteSoundClip()
    {
    	File file = new File(getFilename());
    	file.delete();
    }
    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath+ "/" + AUDIO_RECORDER_FILE + AUDIO_RECORDER_FILE_EXT_WAV);
        try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return (file.getAbsolutePath());
   }
    
    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath+ "/" + AUDIO_RECORDER_TEMP_FILE);
        try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return (file.getAbsolutePath());
    }
    private String getAMRFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath+ "/" + AUDIO_RECORDER_AMR);
        try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return (file.getAbsolutePath());
   }
    
    private void startRecording(final boolean b){
    	recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
        
    	recorder.startRecording();
        
        isRecording = true;
        
        recordingThread = new Thread(new Runnable() {
                
                @Override
                public void run() {
                        writeAudioDataToFile(b);
                }
        },"AudioRecorder Thread");
        
        recordingThread.start();
    }
    
    private void stopRecording(boolean b){
        if(recorder != null){
                isRecording = false;
                
                recorder.stop();
                recorder.release();
                
                recorder = null;
                recordingThread = null;
        }
        
        if(b == true){
        	copyWaveFile(getTempFilename(),getFilename());
            deleteTempFile();
        }
        
        long totalDuration = getSoundDuration();
        
        Log.i("Recorder", "Duration " + totalDuration );
        
        
        
        /*
        seekbar.setMax((int)getSoundDuration());
        
        int hours =  (int) (totalDuration / 3600);
        int minutes =(int)(totalDuration % 3600)/ 60 ;
        int seconds =(int)(totalDuration % 3600) % 60;
          
        
        initialTime.setText("0 : 0 : 0");
        finalTime.setText(hours+" : "+minutes+" : "+seconds );
        
        try {
			convertWaveToAmr(getFilename());
		} catch (Exception e) {
		}
        
        
        */
        
        
    }
   
    private void deleteTempFile() {
        File file = new File(getTempFilename());
        
        file.delete();
    }
    private void writeAudioDataToFile(boolean b){
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
       
        
        
        
        
        FileOutputStream os = null;
        
        try {
                os = new FileOutputStream(filename,b);
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        }
        
        int read = 0;
        
        if(os != null){
            while(isRecording){
                    read = recorder.read(data, 0, bufferSize);
                    
                    if(AudioRecord.ERROR_INVALID_OPERATION != read){
                            try {
                                    os.write(data);
                            } catch (IOException e) {
                                    e.printStackTrace();
                            }
                    }
            }
            
            try {
                    os.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
   }  
    
    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 44;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
        
        byte[] data = new byte[bufferSize];
        
        try {
                in = new FileInputStream(inFilename);
                out = new FileOutputStream(outFilename , true);
                totalAudioLen = in.getChannel().size() + out.getChannel().size();
                totalDataLen = totalAudioLen + 44 ;
                
                Log.i("Recorder" ,"Out channel size initially "+out.getChannel().size() );

                WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                                longSampleRate, channels, byteRate, outFilename);
                Log.i("Recorder" ,"Out channel size after header write "+out.getChannel().size() );

                
                while(in.read(data) != -1){
                        out.write(data);
                }
                Log.i("Recorder" ,"Out channel size"+out.getChannel().size() );
                Log.i("Recorder" ,"in channel size"+in.getChannel().size() );
                in.close();
                out.close();
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
   }
    
    @Override
    protected void onDestroy() {

    	mPlayer.release();
    	super.onDestroy();
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(mPlayer.isPlaying()) mPlayer.pause();
    }
    
    
    
    private void WriteWaveFileHeader(
        FileOutputStream out, long totalAudioLen,
        long totalDataLen, long longSampleRate, int channels,
        long byteRate, String outFileName) throws IOException {

	    byte[] header = new byte[44];
	                                                  
	    header[0] = 'R';  // RIFF/WAVE header
	    header[1] = 'I';
	    header[2] = 'F';
	    header[3] = 'F';
	    header[4] = (byte) (totalDataLen & 0xff);
	    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
	    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
	    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
	    header[8] = 'W';
	    header[9] = 'A';
	    header[10] = 'V';
	    header[11] = 'E';
	    header[12] = 'f';  // 'fmt ' chunk
	    header[13] = 'm';
	    header[14] = 't';
	    header[15] = ' ';
	    header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
	    header[17] = 0;
	    header[18] = 0;
	    header[19] = 0;
	    header[20] = 1;  // format = 1
	    header[21] = 0;
	    header[22] = (byte) channels;
	    header[23] = 0;
	    header[24] = (byte) ( longSampleRate & 0xff);
	    header[25] = (byte) ((longSampleRate >> 8) & 0xff);
	    header[26] = (byte) ((longSampleRate >> 16) & 0xff);
	    header[27] = (byte) ((longSampleRate >> 24) & 0xff);
	    header[28] = (byte) (byteRate & 0xff);
	    header[29] = (byte) ((byteRate >> 8) & 0xff);
	    header[30] = (byte) ((byteRate >> 16) & 0xff);
	    header[31] = (byte) ((byteRate >> 24) & 0xff);
	    header[32] = (byte) (2 * 16 / 8);  // block align
	    header[33] = 0;
	    header[34] = RECORDER_BPP;  // bits per sample
	    header[35] = 0;
	    header[36] = 'd';
	    header[37] = 'a';
	    header[38] = 't';
	    header[39] = 'a';
	    header[40] = (byte) (totalAudioLen & 0xff);
	    header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
	    header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
	    header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
	
	    //out.write(header, 0, 44);
	    //out.getChannel().position(0).write(ByteBuffer.wrap(header));
	    
	    RandomAccessFile rFile = new RandomAccessFile(outFileName, "rw");
	    rFile.seek(0);
	    rFile.write(header, 0, 44);
	    rFile.close();
	    
	    
	 }
    
    public long getSoundDuration()
    {
    	File file = new File(getFilename());
    	long filesiZe = file.length();
    	int channels = 2;
    	long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
    	long duration = filesiZe / byteRate;
    	return duration;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==start_record)
		{
			
			    Animation mAnimation = new AlphaAnimation(1, 0);
	    	    mAnimation.setDuration(200);
	    	    mAnimation.setInterpolator(new LinearInterpolator());
	    	    mAnimation.setRepeatCount(Animation.INFINITE);
	    	    mAnimation.setRepeatMode(Animation.REVERSE); 
	    	    //v.clearAnimation();

	    	    
	    	    start_record.startAnimation(mAnimation);
	    	    
	    	    
		  Toast.makeText(AudioRecord_Player.this, "Now Recording...", Toast.LENGTH_LONG).show();
		  startRecording(false);
		  start_record.setEnabled(false);
		  pause_rec.setEnabled(true);
	      stop_rec.setEnabled(true) ;
	   
		}
		else if(v==stop_rec)
		{
			start_record.clearAnimation();
			
		  Toast.makeText(AudioRecord_Player.this, "Recording Stopped...", Toast.LENGTH_LONG).show();
		  stopRecording(true);
		  start_record.setEnabled(true);
		  stop_rec.setEnabled(false);
		  pause_rec.setEnabled(false);
			
		}
		else if(v==pause_rec)
		{
		  Toast.makeText(AudioRecord_Player.this, "Pausing...", Toast.LENGTH_LONG).show();
		  stopRecording(false);
		  pause_rec.setEnabled(false);
		  resume_rec.setEnabled(true);
		
		}
	
		else if(v==play_rec)
		{
			
			isMediaPlaying = true;
			
		  Toast.makeText(AudioRecord_Player.this, "Now Playing...", Toast.LENGTH_LONG).show();
		  /*
		  try {
			  
			   
				mPlayer = new MediaPlayer();
				mPlayer.setDataSource(getFilename());
				mPlayer.prepare();
				
				mPlayer.start();

				long totalDuration = getSoundDuration();
				
				
		        int hours =  (int) (totalDuration / 3600);
		        int minutes =(int)(totalDuration % 3600)/ 60 ;
		        int seconds =(int)(totalDuration % 3600) % 60;
		        
		        
		        initialTime.setText("0 : 0 : 0");
		        finalTime.setText(hours+" : "+minutes+" : "+seconds );
		       
		        
		        new AsyncTask<Void, Integer, Void>()
		        {

					@Override
					protected Void doInBackground(Void... params) {

						int seekRate = seekBarMax / (int)getSoundDuration();
						int progress =0;
						while( isMediaPlaying)
						{
							publishProgress(progress);							
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							progress += seekRate ;
							
						}
						return null;
						
					}
					@Override
		        	protected  void onProgressUpdate(Integer[] values) 
					{
						seekbar.setProgress(values[0]);
						
					}
		        	
		        	@Override
		        	protected void onPostExecute(Void result)
		        	{
		        		seekbar.setProgress(0);
		        	}
		        	
		        	
		        }.execute();
		        
		      
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
			*/
			  

			try {
				
				String fileName = getFilename();
				File file = new File(fileName);
				if(file.length() == 0 )
				{
					Toast.makeText(this, "File length zero .", Toast.LENGTH_SHORT) . show();
					return;
				}
				
	        	mPlayer.reset();
				mPlayer.setDataSource(getFilename());
				mPlayer.prepare();
				mPlayer.start();
				// Displaying Song title
				
				seekbar.setProgress(0);
				seekbar.setMax(100);
				
				// Updating progress bar

			   mHandler.postDelayed(updateHandler, 100);
			   
			   
			
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  
		}
		else if(v==resume_rec)
		{
		  Toast.makeText(AudioRecord_Player.this, "Resuming...", Toast.LENGTH_LONG).show();	
		  //recorder.startRecording();
		  startRecording(true);
		  start_record.setEnabled(false);
		  pause_rec.setEnabled(true);
	      stop_rec.setEnabled(true);
		  
		  
		}
	}   
	
	public void pauseMedia(View v)
	{
		mPlayer.pause();
		isMediaPlaying = false;
	}
	public void delete(View v)
	{
		deleteSoundClip();
	}
	public void convertWaveToAmr(String wavFilename)
	{
		
		AmrInputStream aStream = null ;
		InputStream inStream = null;
		OutputStream out = null;

		try {
			inStream = new FileInputStream(wavFilename);
			aStream= new AmrInputStream(inStream);
			File file = new File(getAMRFilename());        
			out= new FileOutputStream(file); 
			out.write(0x23);
			out.write(0x21);
			out.write(0x41);
			out.write(0x4D);
			out.write(0x52);
			out.write(0x0A);    

			byte[] x = new byte[1024];
			int len;
			while ((len=aStream.read(x)) > 0) {
			    out.write(x,0,len);
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				out.close();
				aStream.close();
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	Runnable updateHandler = new Runnable() {
		
		@Override
		public void run() {
			
			
			long totalDuration = mPlayer.getDuration();
			   long currentDuration = mPlayer.getCurrentPosition();
				Log.i("Recorder" , "handler running. Currnt "+currentDuration + "  total " +totalDuration );
				

			   // Displaying Total Duration time
			   initialTime.setText(""+utilities.milliSecondsToTimer(totalDuration));
			   // Displaying time completed playing
			   finalTime.setText(""+utilities.milliSecondsToTimer(currentDuration));
			   
			   // Updating progress bar
			   int progress = (int)(utilities.getProgressPercentage(currentDuration, totalDuration));
			   //Log.d("Progress", ""+progress);
			   seekbar.setProgress(progress);
			   
			   // Running this thread after 100 milliseconds
		       if(currentDuration <= totalDuration)
			   {
		    	   mHandler.postDelayed(this, 100);
		    	  
			   }
		       else
		       {
		    	   mHandler.removeCallbacks(this);
				   Log.i("Recorder" , "elseeeeee" );

		       }
		
		}
	};
}
