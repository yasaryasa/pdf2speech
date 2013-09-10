package com.yaser.pdf2speech;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DirectoryListActivity extends ListActivity {

	private static final String STRING = "/";
	private static final String UP = "Up";
	private static final String SONG_TITLE = "songTitle";
	private static final String SONG_PATH = "songPath";
	private static String prevPath="";

	
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	private String selectedFilePath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// no more this
		// setContentView(R.layout.list_fruit);

		songsList = getDirectoryList(null);
		
		// Adding menuItems to ListView
		ListAdapter adapter = new SimpleAdapter(this, songsList,
				R.layout.playlist_item, new String[] { SONG_TITLE },
				new int[] { R.id.songTitle });
		
		setListAdapter(adapter);

		final ListView listView = getListView();
		listView.setTextFilterEnabled(true);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				
				String songPath;
				songPath = songsList.get(position).get(SONG_PATH);
				if (songPath.equals(UP)) {
					songPath = prevPath.substring(0, prevPath.lastIndexOf(STRING));
				} 
				prevPath = songPath;
				songsList = getDirectoryList(songPath);
				listView.invalidateViews();
				
			}
		});

	}
	
	/**
	 * Function to read all mp3 files from sdcard
	 * and store the details in ArrayList
	 * */
	public ArrayList<HashMap<String, String>> getDirectoryList(String path){
		File home = null;
		if (path == null) {
			home = Environment.getExternalStorageDirectory();
		} else {
			home = new File(path);
		}
		
		if (home.isFile()) {
			selectedFilePath = path;
			// Starting new intent
			Intent in = new Intent(getApplicationContext(),
					TextPlayerActivity.class);
			// Sending songIndex to PlayerActivity
			in.putExtra("filePath", path);
			setResult(100, in);
			// Closing PlayListView
			finish();
		} else { 
			if (home.listFiles(new FileExtensionFilter()).length > 0) {
				songsList.clear();
				
				HashMap<String, String> song = new HashMap<String, String>();
				song.put(SONG_TITLE, UP);
				song.put(SONG_PATH, UP);
				songsList.add(song);
				for (File file : home.listFiles(new FileExtensionFilter())) {
					HashMap<String, String> song1 = new HashMap<String, String>();
					song1.put(SONG_TITLE, file.getName());
					song1.put(SONG_PATH, file.getPath());
					
					// Adding each song to SongList
					songsList.add(song1);
				}
			}
		}
		// return songs list array
		return songsList;
	}
	
	/**
	 * Class to filter files which are having .mp3 extension
	 * */
	class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			File sel = new File(dir, name);
			// Filters based on whether the file is hidden or not
			return (sel.isFile() || sel.isDirectory())
					&& !sel.isHidden();
		}
	}

}