/*
	This file is part of Subsonic.
	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with Subsonic. If not, see <http://www.gnu.org/licenses/>.
	Copyright 2015 (C) Scott Jackson
*/

package github.popeen.dsub.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import github.popeen.dsub.R;
import github.popeen.dsub.domain.MusicDirectory;
import github.popeen.dsub.service.DownloadFile;

public class SongDBHandler extends SQLiteOpenHelper {
	private static final String TAG = SongDBHandler.class.getSimpleName();
	private static SongDBHandler dbHandler;

	private static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "SongsDB";

	public static final String TABLE_SONGS = "RegisteredSongs";
	public static final String SONGS_ID = "id";
	public static final String SONGS_SERVER_KEY = "serverKey";
	public static final String SONGS_SERVER_ID = "serverId";
	public static final String SONGS_COMPLETE_PATH = "completePath";
	public static final String SONGS_LAST_PLAYED = "lastPlayed";
	public static final String SONGS_LAST_COMPLETED = "lastCompleted";

	private Context context;

	private SongDBHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_SONGS + " ( " +
				SONGS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SONGS_SERVER_KEY + " INTEGER NOT NULL, " +
				SONGS_SERVER_ID + " TEXT NOT NULL, " +
				SONGS_COMPLETE_PATH + " TEXT NOT NULL, " +
				SONGS_LAST_PLAYED + " INTEGER, " +
				SONGS_LAST_COMPLETED + " INTEGER, " +
				"UNIQUE(" + SONGS_SERVER_KEY + ", " + SONGS_SERVER_ID + "))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		this.onCreate(db);
	}

	public void exportHeard() {
		SQLiteDatabase db = this.getReadableDatabase();
		SQLiteHandler sqlh = new SQLiteHandler(context);

		String[] columns = {SONGS_ID, SONGS_SERVER_KEY, SONGS_SERVER_ID, SONGS_COMPLETE_PATH, SONGS_LAST_PLAYED, SONGS_LAST_COMPLETED};
		Cursor cursor = db.query(TABLE_SONGS, columns, SONGS_LAST_PLAYED + " != ''", null, null, null, null, null);

		JSONObject json = new JSONObject();

		try {
			JSONArray jsonSongDb = new JSONArray();
			while (cursor.moveToNext()) {
				JSONObject tempJson = new JSONObject();
				tempJson.put("SONGS_ID", cursor.getInt(0));
				tempJson.put("SONGS_SERVER_KEY", cursor.getInt(1));
				tempJson.put("SONGS_SERVER_ID", cursor.getString(2));
				tempJson.put("SONGS_COMPLETE_PATH", cursor.getString(3));
				tempJson.put("SONGS_LAST_PLAYED", cursor.getInt(4));
				tempJson.put("SONGS_LAST_COMPLETED", cursor.getInt(5));
				jsonSongDb.put(tempJson);
			}
			cursor.close();


			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			JSONArray jsonPrefs = new JSONArray();
			for(int i=0; i<prefs.getInt("serverCount", 0); i++){
				JSONObject tempJson = new JSONObject();
				tempJson.put("serverName", prefs.getString("serverName"+Integer.toString(i + 1), ""));
				tempJson.put("username", prefs.getString("username"+Integer.toString(i + 1), ""));
				//OBS, Base 64 IS NOT ENCRYPTION, it is only used so the password will not be in clear text readable to humans but a potential attacker would get the password from it in less then a second.
				tempJson.put("password", KakaduaUtil.randomChar()+KakaduaUtil.base64Encode(prefs.getString("password" + Integer.toString(i + 1), "")).replace("=", ""));
				tempJson.put("serverUrl", prefs.getString("serverUrl"+Integer.toString(i + 1), ""));
				tempJson.put("serverInternalUrl", prefs.getString("serverInternalUrl"+Integer.toString(i + 1), ""));
				tempJson.put("mostRecentCount", prefs.getInt("mostRecentCount"+Integer.toString(i + 1), 0));
				jsonPrefs.put(tempJson);
			}


			json.put("Prefs", jsonPrefs);
			json.put("SongDB", jsonSongDb);
			json.put("Booksonic", sqlh.export());
		}catch(Exception e){ }

		try {
			File file = new File(Environment.getExternalStorageDirectory(), "booksonic_backup.json");
			FileOutputStream fileos = new FileOutputStream(file.getPath());
			fileos.write(json.toString().getBytes());
			fileos.close();
		}catch(Exception e){ }

		Toast toast = Toast.makeText(context, context.getString(R.string.exported) + " " + Environment.getExternalStorageDirectory() + "/booksonic_backup.json", Toast.LENGTH_LONG);
		toast.show();
	}

	protected String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	protected String getStringFromFile (String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		//Make sure you close all streams.
		fin.close();
		return ret;
	}

	public void importHeard(String path) {
		SQLiteDatabase db = this.getReadableDatabase();
		SQLiteHandler sqlh = new SQLiteHandler(context);

		try {
			String jsonString = getStringFromFile(path);
			JSONArray array = new JSONArray(new JSONObject(jsonString).get("SongDB").toString());
			for (int i = 0; i < array.length(); i++) {
				JSONObject row = array.getJSONObject(i);
				ContentValues values = new ContentValues();
				values.put(SONGS_ID, row.getInt("SONGS_ID"));
				values.put(SONGS_SERVER_KEY, row.getInt("SONGS_SERVER_KEY"));
				values.put(SONGS_SERVER_ID, row.getString("SONGS_SERVER_ID"));
				values.put(SONGS_COMPLETE_PATH, row.getString("SONGS_COMPLETE_PATH"));
				values.put(SONGS_LAST_PLAYED, row.getInt("SONGS_LAST_PLAYED"));
				values.put(SONGS_LAST_COMPLETED, row.getInt("SONGS_LAST_COMPLETED"));


				db.insertWithOnConflict(TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

			}



			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			array = new JSONArray(new JSONObject(jsonString).get("Prefs").toString());
			for (int i = 0; i < array.length(); i++) {
				JSONObject row = array.getJSONObject(i);
				editor.putString("serverName" + Integer.toString(i + 1), row.getString("serverName"));
				editor.putString("username" + Integer.toString(i + 1), row.getString("username"));
				editor.putString("password" + Integer.toString(i + 1), KakaduaUtil.base64Decode(row.getString("password").substring(1)));
				editor.putString("serverUrl" + Integer.toString(i + 1), row.getString("serverUrl"));
				editor.putString("serverInternalUrl" + Integer.toString(i + 1), row.getString("serverInternalUrl"));
				editor.putInt("mostRecentCount" + Integer.toString(i + 1), row.getInt("mostRecentCount"));
				editor.putInt("serverCount", i + 1);
			}

			editor.commit();
			sqlh.importData(new JSONArray(new JSONObject(jsonString).get("Booksonic").toString()));

		}catch(Exception e){ }
	}

	public synchronized void addSong(DownloadFile downloadFile) {
		addSong(Util.getMostRecentActiveServer(context), downloadFile);
	}
	public synchronized void addSong(int instance, DownloadFile downloadFile) {
		SQLiteDatabase db = this.getWritableDatabase();
		addSong(db, instance, downloadFile);
		db.close();
	}
	protected synchronized void addSong(SQLiteDatabase db, DownloadFile downloadFile) {
		addSong(db, Util.getMostRecentActiveServer(context), downloadFile);
	}
	protected synchronized void addSong(SQLiteDatabase db, int instance, DownloadFile downloadFile) {
		addSong(db, instance, downloadFile.getSong().getId(), downloadFile.getSaveFile().getAbsolutePath());
	}

	protected synchronized void addSong(SQLiteDatabase db, String id, String absolutePath) {
		addSong(db, Util.getMostRecentActiveServer(context), id, absolutePath);
	}
	protected synchronized void addSong(SQLiteDatabase db, int instance, String id, String absolutePath) {
		addSongImpl(db, Util.getRestUrlHash(context, instance), id, absolutePath);
	}
	protected synchronized void addSongImpl(SQLiteDatabase db, int serverKey, String id, String absolutePath) {
		ContentValues values = new ContentValues();
		values.put(SONGS_SERVER_KEY, serverKey);
		values.put(SONGS_SERVER_ID, id);
		values.put(SONGS_COMPLETE_PATH, absolutePath);

		db.insertWithOnConflict(TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}

	public synchronized void addSongs(int instance, List<MusicDirectory.Entry> entries) {
		SQLiteDatabase db = this.getWritableDatabase();

		List<Pair<String, String>> pairs = new ArrayList<>();
		for(MusicDirectory.Entry entry: entries) {
			pairs.add(new Pair<>(entry.getId(), FileUtil.getSongFile(context, entry).getAbsolutePath()));
		}
		addSongs(db, instance, pairs);

		db.close();
	}
	public synchronized void addSongs(SQLiteDatabase db, int instance, List<Pair<String, String>> entries) {
		addSongsImpl(db, Util.getRestUrlHash(context, instance), entries);
	}
	protected synchronized void addSongsImpl(SQLiteDatabase db, int serverKey, List<Pair<String, String>> entries) {
		db.beginTransaction();
		try {
			for (Pair<String, String> entry : entries) {
				ContentValues values = new ContentValues();
				values.put(SONGS_SERVER_KEY, serverKey);
				values.put(SONGS_SERVER_ID, entry.getFirst());
				values.put(SONGS_COMPLETE_PATH, entry.getSecond());

				db.insertWithOnConflict(TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}

			db.setTransactionSuccessful();
		} catch(Exception e) {}

		db.endTransaction();
	}

	public synchronized void setSongPlayed(DownloadFile downloadFile, boolean submission) {
		// TODO: In case of offline want to update all matches
		Pair<Integer, String> pair = getOnlineSongId(downloadFile);
		if(pair == null) {
			return;
		}
		int serverKey = pair.getFirst();
		String id = pair.getSecond();

		// Open and make sure song is in db
		SQLiteDatabase db = this.getWritableDatabase();
		addSongImpl(db, serverKey, id, downloadFile.getSaveFile().getAbsolutePath());

		// Update song's last played
		ContentValues values = new ContentValues();
		values.put(submission ? SONGS_LAST_COMPLETED : SONGS_LAST_PLAYED, System.currentTimeMillis());
		db.update(TABLE_SONGS, values, SONGS_SERVER_KEY + " = ? AND " + SONGS_SERVER_ID + " = ?", new String[]{Integer.toString(serverKey), id});
		db.close();
	}

	public synchronized Long[] getLastPlayed(MusicDirectory.Entry entry) {
		return getLastPlayed(getOnlineSongId(entry));
	}
	protected synchronized Long[] getLastPlayed(Pair<Integer, String> pair) {
		return getLastPlayed(pair.getFirst(), pair.getSecond());
	}
	public synchronized Long[] getLastPlayed(int serverKey, String id) {
		SQLiteDatabase db = this.getReadableDatabase();

		String[] columns = {SONGS_LAST_PLAYED, SONGS_LAST_COMPLETED};
		Cursor cursor = db.query(TABLE_SONGS, columns, SONGS_SERVER_KEY + " = ? AND " + SONGS_SERVER_ID + " = ?", new String[]{Integer.toString(serverKey), id}, null, null, null, null);

		try {
			cursor.moveToFirst();

			Long[] dates = new Long[2];
			dates[0] = cursor.getLong(0);
			dates[1] = cursor.getLong(1);
			return dates;
		} catch(Exception e) {}

		return null;
	}

	public synchronized Pair<Integer, String> getOnlineSongId(MusicDirectory.Entry entry) {
		return getOnlineSongId(Util.getRestUrlHash(context), entry.getId(), FileUtil.getSongFile(context, entry).getAbsolutePath(), Util.isOffline(context) ? false : true);
	}
	public synchronized Pair<Integer, String> getOnlineSongId(DownloadFile downloadFile) {
		return getOnlineSongId(Util.getRestUrlHash(context), downloadFile.getSong().getId(), downloadFile.getSaveFile().getAbsolutePath(), Util.isOffline(context) ? false : true);
	}

	public synchronized Pair<Integer, String> getOnlineSongId(int serverKey, MusicDirectory.Entry entry) {
		return getOnlineSongId(serverKey, new DownloadFile(context, entry, true));
	}
	public synchronized Pair<Integer, String> getOnlineSongId(int serverKey, DownloadFile downloadFile) {
		return getOnlineSongId(serverKey, downloadFile.getSong().getId(), downloadFile.getSaveFile().getAbsolutePath(), true);
	}
	public synchronized Pair<Integer, String> getOnlineSongId(int serverKey, String id, String savePath, boolean requireServerKey) {
		SharedPreferences prefs = Util.getPreferences(context);
		String cacheLocn = prefs.getString(Constants.PREFERENCES_KEY_CACHE_LOCATION, null);
		if(cacheLocn != null && id.indexOf(cacheLocn) != -1) {
			if(requireServerKey) {
				return getIdFromPath(serverKey, savePath);
			} else {
				return getIdFromPath(savePath);
			}
		} else {
			return new Pair<>(serverKey, id);
		}
	}

	public synchronized Pair<Integer, String> getIdFromPath(String path) {
		SQLiteDatabase db = this.getReadableDatabase();

		String[] columns = {SONGS_SERVER_KEY, SONGS_SERVER_ID};
		Cursor cursor = db.query(TABLE_SONGS, columns, SONGS_COMPLETE_PATH + " = ?", new String[] { path }, null, null, SONGS_LAST_PLAYED + " DESC", null);

		try {
			cursor.moveToFirst();
			return new Pair(cursor.getInt(0), cursor.getString(1));
		} catch(Exception e) {}

		return null;
	}
	public synchronized Pair<Integer, String> getIdFromPath(int serverKey, String path) {
		SQLiteDatabase db = this.getReadableDatabase();

		String[] columns = {SONGS_SERVER_KEY, SONGS_SERVER_ID};
		Cursor cursor = db.query(TABLE_SONGS, columns, SONGS_SERVER_KEY + " = ? AND " + SONGS_COMPLETE_PATH + " = ?", new String[] {Integer.toString(serverKey), path }, null, null, null, null);

		try {
			cursor.moveToFirst();
			return new Pair(cursor.getInt(0), cursor.getString(1));
		} catch(Exception e) {}

		return null;
	}

	public static SongDBHandler getHandler(Context context) {
		if(dbHandler == null) {
			dbHandler = new SongDBHandler(context);
		}

		return dbHandler;
	}
}
