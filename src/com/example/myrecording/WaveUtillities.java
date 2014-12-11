package com.example.myrecording;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.Principal;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;


	interface RecordProgressbarUpdate {
		public void onProgressbarUpdate(int updateValue);
		public void onRecordingError();
	}

	

public class WaveUtillities extends Utilities{

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
	//Utilities utilities;
	

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	Context context;
	RecordProgressbarUpdate recordProgressbarUpdate;
	


	
	public WaveUtillities(FragRecorder fragRecorder) {

		super();
		recordProgressbarUpdate = (RecordProgressbarUpdate) fragRecorder;
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
	
	}

	public void deleteSoundClip() {
		File file = new File(getFilename());
		file.delete();
	}

	public String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath + "/" + AUDIO_RECORDER_FILE
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (file.getAbsolutePath());
	}

	public String getTempFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath + "/" + AUDIO_RECORDER_TEMP_FILE);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (file.getAbsolutePath());
	}

	public String getAMRFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath + "/" + AUDIO_RECORDER_AMR);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (file.getAbsolutePath());
	}

	public void startRecording() {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);
		recorder.startRecording();

		isRecording = true;

		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();
	}

	public void stopRecording() {
		if (recorder != null) {
			isRecording = false;

			recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}

			copyWaveFile(getTempFilename(), getFilename());
			deleteTempFile();

		long totalDuration = getSoundDuration();

		Log.i("Recorder", "Duration " + totalDuration);

		/*
		 * seekbar.setMax((int)getSoundDuration());
		 * 
		 * int hours = (int) (totalDuration / 3600); int minutes
		 * =(int)(totalDuration % 3600)/ 60 ; int seconds =(int)(totalDuration %
		 * 3600) % 60;
		 * 
		 * 
		 * initialTime.setText("0 : 0 : 0");
		 * finalTime.setText(hours+" : "+minutes+" : "+seconds );
		 * 
		 * try { convertWaveToAmr(getFilename()); } catch (Exception e) { }
		 */

	}

	public void deleteTempFile() {
		File file = new File(getTempFilename());
		file.delete();
	}

	public void writeAudioDataToFile() {
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();

		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int read = 0;

		if (os != null) {
			int amplitude = 0;
			int sum = 0, progress = 0;
			while (isRecording) {
				read = recorder.read(data, 0, bufferSize);

				
				
				Log.i("WaveUtilities" , "write data to file "+ read + " bytes ");
				
				/*
				for(int i=0;i<data.length;i++) sum += data[i] * data[i] ;
				
				amplitude = sum / read;
				progress = (int) Math.sqrt(amplitude);
				*/
				
				try {
					
					amplitude = (data[0] & 0xff) << 8 | data[1];
				    amplitude = Math.abs(amplitude);
				    Log.i("Waveutils", "amplitude " + amplitude);
				    progress = (amplitude / 3000 ) * 100;
				    progress /= 2;
				    recordProgressbarUpdate.onProgressbarUpdate(progress);

				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
				
				
				//if (AudioRecord.ERROR_INVALID_OPERATION != read	&& AudioRecord.ERROR != read && AudioRecord.ERROR_BAD_VALUE!= read
					//	&& AudioRecord.STATE_UNINITIALIZED != read) {
					
				
				
				if(read > 0 ){
					
					try {
							os.write(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				else
				{
					// on error
					isRecording = false;
					recordProgressbarUpdate.onRecordingError();					
					return;
					
					
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 44;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename, true);
			totalAudioLen = in.getChannel().size() + out.getChannel().size();
			totalDataLen = totalAudioLen + 44;

			Log.i("Recorder", "Out channel size initially "
					+ out.getChannel().size());

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate, outFilename);
			Log.i("Recorder", "Out channel size after header write "
					+ out.getChannel().size());

			while (in.read(data) != -1) {
				out.write(data);
			}
			Log.i("Recorder", "Out channel size" + out.getChannel().size());
			Log.i("Recorder", "in channel size" + in.getChannel().size());
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate, String outFileName) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
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
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		// out.write(header, 0, 44);
		// out.getChannel().position(0).write(ByteBuffer.wrap(header));

		RandomAccessFile rFile = new RandomAccessFile(outFileName, "rw");
		rFile.seek(0);
		rFile.write(header, 0, 44);
		rFile.close();

	}

	public long getSoundDuration() {
		File file = new File(getFilename());
		long filesiZe = file.length();
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
		long duration = filesiZe / byteRate;
		return duration;
	}

	public void convertWaveToAmr(String wavFilename) {

		AmrInputStream aStream = null;
		InputStream inStream = null;
		OutputStream out = null;

		try {
			inStream = new FileInputStream(wavFilename);
			aStream = new AmrInputStream(inStream);
			File file = new File(getAMRFilename());
			out = new FileOutputStream(file);
			out.write(0x23);
			out.write(0x21);
			out.write(0x41);
			out.write(0x4D);
			out.write(0x52);
			out.write(0x0A);

			byte[] x = new byte[1024];
			int len;
			while ((len = aStream.read(x)) > 0) {
				out.write(x, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				aStream.close();
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	

}
