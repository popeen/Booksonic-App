package github.popeen.dsub.util;

import android.app.Activity;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by P on 2015-11-19.
 */
public class SQLiteHandler extends SQLiteOpenHelper {
    private static SQLiteHandler dbHandler;

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BooksonicDB";
    public static final String TABLE_HEARD_TRACKS = "HeardTracks";
    public static final String TRACK_ID = "id";
    public static final String TRACK_HEARD = "heard";
    public static final String TRACK_DATE = "date";

    private static final String[] COLUMNS = { TRACK_ID, TRACK_HEARD, TRACK_DATE };

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_HEARD_TRACKS + " ( " + TRACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TRACK_HEARD + " TEXT, " + TRACK_DATE + " TEXT )";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEARD_TRACKS);
        this.onCreate(db);
    }

    public void addTrack(String[] track) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRACK_ID, track[0]);
        values.put(TRACK_HEARD, track[1]);
        values.put(TRACK_DATE, track[2]);

        db.insertWithOnConflict(TABLE_HEARD_TRACKS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void importData(JSONArray array){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(TRACK_ID, row.getInt("TRACK_ID"));
                values.put(TRACK_HEARD, row.getString("TRACK_HEARD"));
                values.put(TRACK_DATE, row.getString("TRACK_DATE"));
                db.insertWithOnConflict(TABLE_HEARD_TRACKS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }catch(Exception e){ }
    }

    public JSONArray exportData(){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {TRACK_ID, TRACK_HEARD, TRACK_DATE};
        Cursor cursor = db.query(TABLE_HEARD_TRACKS, columns, null, null, null, null, null, null);


        try {
            JSONArray json = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject tempJson = new JSONObject();
                tempJson.put("TRACK_ID", cursor.getInt(0));
                tempJson.put("TRACK_HEARD", cursor.getString(1));
                tempJson.put("TRACK_DATE", cursor.getString(2));
                json.put(tempJson);
            }
            cursor.close();
            return json;
        }catch(Exception e){ }
        return new JSONArray();
    }

    public String[] getTrack(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HEARD_TRACKS,
                COLUMNS, " id = ?", new String[] { id }, null, null, null, null);

        try {
            cursor.moveToFirst();
            String[] track = new String[3];
            track[0] = cursor.getString(0); //id
            track[1] = cursor.getString(1); //heard
            track[2] = cursor.getString(2); //date
            return track;
        }catch(Exception e){}
        return new String[3];
    }
    public void updateTrack(String[] track) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRACK_HEARD, track[1]);
        values.put(TRACK_DATE, track[2]);

        int i = db.update(TABLE_HEARD_TRACKS, values, TRACK_ID + " = ?", new String[] { track[0] });
        db.close();
    }

    public static SQLiteHandler getHandler(Context context) {
        if(dbHandler == null) {
            dbHandler = new SQLiteHandler(context);
        }

        return dbHandler;
    }
}
