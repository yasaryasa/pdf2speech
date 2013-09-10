package com.yaser.pdf2speech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yaser.pdf2speech.pdf.PDFExtractor;
import com.yaser.pdf2speech.pdf.PDFOperations;
import com.yaser.pdf2speech.tts.TextToSpeechInitializer;
import com.yaser.pdf2speech.tts.TextToSpeechStartupListener;
import com.yaser.pdf2speech.util.SpeechConstants;

public class TextPlayerActivity extends Activity implements
		OnCompletionListener, SeekBar.OnSeekBarChangeListener,
		TextToSpeechStartupListener {

	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;

	// // Media Player
	// private MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	private int currentSongIndex = 0;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

	private TextToSpeechInitializer ttsInit;
	private TextToSpeech tts;
	private static final String TAG = "TextPlayerActivity";
	private static boolean isReading = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		ttsInit = new TextToSpeechInitializer(this, Locale.getDefault(), this);

		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this); // Important
		// mp.setOnCompletionListener(this); // Important

		/**
		 * Play button click event plays a song and changes button to pause
		 * image pauses a song and changes button to play image
		 * */
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (PDFOperations.reader == null) {

					String string = "Lütfen okumak için doküman seçin.";
					Toast.makeText(getApplicationContext(), string,
							Toast.LENGTH_SHORT).show();
					
					readString(string);
					
					return;
				}

				if (btnPlay.getTag() == null
						|| btnPlay.getTag().equals(R.drawable.btn_play)) {
					startReading();
					btnPlay.setImageResource(R.drawable.btn_pause);
					btnPlay.setTag(R.drawable.btn_pause);
				} else {
					stopReading();
					btnPlay.setImageResource(R.drawable.btn_play);
					btnPlay.setTag(R.drawable.btn_play);
				}

				// if(mp.isPlaying()){
				// if(mp!=null){
				// mp.pause();
				// // Changing button image to play button
				// btnPlay.setImageResource(R.drawable.btn_play);
				// }
				// }else{
				// // Resume song
				// if(mp!=null){
				// mp.start();
				// // Changing button image to pause button
				// btnPlay.setImageResource(R.drawable.btn_pause);
				// }
				// }
			}
		});

		/**
		 * Forward button click event Forwards song specified seconds
		 * */
		btnForward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				readNextSentence();
			}
		});

		/**
		 * Backward button click event Backward song to specified seconds
		 * */
		btnBackward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				readPrevSentence();
			}
		});

		/**
		 * Next button click event Plays next song by taking currentSongIndex +
		 * 1
		 * */
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				PDFOperations.setNextPage();
				readNextSentence();
			}

		});

		/**
		 * Back button click event Plays previous song by currentSongIndex - 1
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

			}
		});

		/**
		 * Button Click event for Repeat button Enables repeat flag to true
		 * */
		btnRepeat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF",
							Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				} else {
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON",
							Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});

		/**
		 * Button Click event for Shuffle button Enables shuffle flag to true
		 * */
		btnShuffle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isShuffle) {
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF",
							Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				} else {
					// make repeat to true
					isShuffle = true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON",
							Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
			}
		});

		/**
		 * Button Click event for Play list click event Launches list activity
		 * which displays list of songs
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						DirectoryListActivity.class);
				startActivityForResult(i, 100);
			}
		});

	}

	private void startReading() {
		if (PDFOperations.currentSentence != null) {
			readString(PDFOperations.currentSentence);
		} else {
			readNextSentence();
		}
		isReading = true;
	}

	private void stopReading() {
		tts.stop();
		isReading = false;
	}

	private void readNextSentence() {
		String nextSentence = PDFOperations.nextSentence();
		if (nextSentence.equals(SpeechConstants.END_OF_PAGE)) {
			PDFOperations.setNextPage();
			nextSentence = PDFOperations.nextSentence();
		}
		readString(nextSentence);
	}

	private void readPrevSentence() {
		readString(PDFOperations.prevSentence());
	}

	private void readString(String str) {
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
				SpeechConstants.END_OF_SPEECH);
		// tts.playSilence(1000, TextToSpeech.QUEUE_ADD, null);
		tts.speak(str, TextToSpeech.QUEUE_ADD, myHashAlarm);
		// tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
	}

	/**
	 * Receiving song index from playlist view and play the song
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 100) {
			// currentSongIndex = data.getExtras().getInt("songIndex");
			// // play selected song
			// playSong(currentSongIndex);
			ProgressDialog pDialog = ProgressDialog.show(
					TextPlayerActivity.this, null, "Doküman ayrýþtýrýlýyor",
					true);
			pDialog.setCancelable(false);

			String filePath = data.getExtras().getString("filePath");

			PDFExtractor ext = new PDFExtractor(pDialog, filePath);
			ext.start();
		}

	}

	/**
	 * Function to play a song
	 * 
	 * @param songIndex
	 *            - index of song
	 * */
	public void playSong(int songIndex) {
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			// long totalDuration = mp.getDuration();
			// long currentDuration = mp.getCurrentPosition();
			//
			// // Displaying Total Duration time
			// songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			// // Displaying time completed playing
			// songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
			//
			// // Updating progress bar
			// int progress = (int)(utils.getProgressPercentage(currentDuration,
			// totalDuration));
			// //Log.d("Progress", ""+progress);
			// songProgressBar.setProgress(progress);
			//
			// // Running this thread after 100 milliseconds
			// mHandler.postDelayed(this, 100);
		}
	};

	/**
	 * 
	 * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 * */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress hanlder
	 * */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// mHandler.removeCallbacks(mUpdateTimeTask);
		// int totalDuration = mp.getDuration();
		// int currentPosition = utils.progressToTimer(seekBar.getProgress(),
		// totalDuration);
		//
		// // forward or backward to certain seconds
		// mp.seekTo(currentPosition);
		//
		// // update timer progress again
		// updateProgressBar();
	}

	/**
	 * On Song Playing completed if repeat is ON play same song again if shuffle
	 * is ON play random song
	 * */
	@Override
	public void onCompletion(MediaPlayer arg0) {

		// check for repeat is ON or OFF
		if (isRepeat) {
			// repeat is on play same song again
			playSong(currentSongIndex);
		} else if (isShuffle) {
			// shuffle is on - play a random song
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex);
		} else {
			// no repeat or shuffle ON - play next song
			if (currentSongIndex < (songsList.size() - 1)) {
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;
			} else {
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onSuccessfulInit(TextToSpeech tts) {
		this.tts = tts;
		setTtsListener();
	}

	@Override
	public void onFailedToInit() {
		DialogInterface.OnClickListener onClickOk = makeOnFailedToInitHandler();
		AlertDialog a = new AlertDialog.Builder(this).setTitle("Error")
				.setMessage("Unable to create text to speech")
				.setNeutralButton("Ok", onClickOk).create();
		a.show();
	}

	@Override
	public void onRequireLanguageData() {
		DialogInterface.OnClickListener onClickOk = makeOnClickInstallDialogListener();
		DialogInterface.OnClickListener onClickCancel = makeOnFailedToInitHandler();
		AlertDialog a = new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage(
						"Requires Language data to proceed, "
								+ "would you like to install?")
				.setPositiveButton("Ok", onClickOk)
				.setNegativeButton("Cancel", onClickCancel).create();
		a.show();
	}

	@Override
	public void onWaitingForLanguageData() {
		// either wait for install
		DialogInterface.OnClickListener onClickWait = makeOnFailedToInitHandler();
		DialogInterface.OnClickListener onClickInstall = makeOnClickInstallDialogListener();

		AlertDialog a = new AlertDialog.Builder(this)
				.setTitle("Info")
				.setMessage(
						"Please wait for the language data to finish"
								+ " installing and try again.")
				.setNegativeButton("Wait", onClickWait)
				.setPositiveButton("Retry", onClickInstall).create();
		a.show();
	}

	private DialogInterface.OnClickListener makeOnClickInstallDialogListener() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ttsInit.installLanguageData();
			}
		};
	}

	private DialogInterface.OnClickListener makeOnFailedToInitHandler() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		};
	}

	/**
	 * set the TTS listener to call {@link #onDone(String)} depending on the
	 * Build.Version.SDK_INT
	 */
	private void setTtsListener() {
		int listenerResult = tts
				.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
					@Override
					public void onUtteranceCompleted(String utteranceId) {
						TextPlayerActivity.this.onDone(utteranceId);
					}
				});
		if (listenerResult != TextToSpeech.SUCCESS) {
			Log.e(TAG, "failed to add utterance completed listener");
		}
	}

	private void onDone(final String utteranceId) {
		Log.d(TAG, "utterance completed: " + utteranceId);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (utteranceId.equals(SpeechConstants.END_OF_SPEECH)
						&& isReading) {
					readNextSentence();
				}
			}
		});
	}

}