package github.popeen.dsub.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by P on 2015-11-19.
 */
public class SQLiteHandler extends SQLiteOpenHelper {
    private static SQLiteHandler dbHandler;

    private static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "BooksonicDB";
    public static final String TABLE_HEARD_BOOKS = "HeardBooks";
    public static final String BOOK_NAME = "name";
    public static final String BOOK_HEARD = "heard";
    public static final String BOOK_DATE = "date";

    private static final String[] COLUMNS = {BOOK_NAME, BOOK_HEARD, BOOK_DATE};

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_HEARD_BOOKS + " ( " + BOOK_NAME + " TEXT, " + BOOK_HEARD + " TEXT, " + BOOK_DATE + " TEXT )";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEARD_BOOKS);
        this.onCreate(db);
    }

    public void markBookAsRead(String[] track) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BOOK_NAME, track[0]);
        values.put(BOOK_HEARD, track[1]);
        values.put(BOOK_DATE, track[2]);

        db.insertWithOnConflict(TABLE_HEARD_BOOKS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void markBookAsUnread(String track) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HEARD_BOOKS, BOOK_NAME + " = ?", new String[] { track });
        db.close();
    }

    public void importData(JSONArray array){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(BOOK_NAME, row.getString("BOOK_NAME"));
                values.put(BOOK_HEARD, row.getString("BOOK_HEARD"));
                values.put(BOOK_DATE, row.getString("BOOK_DATE"));
                db.insertWithOnConflict(TABLE_HEARD_BOOKS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }catch(Exception e){ }
    }

    public JSONArray exportData(){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {BOOK_NAME, BOOK_HEARD, BOOK_DATE};
        Cursor cursor = db.query(TABLE_HEARD_BOOKS, columns, null, null, null, null, null, null);


        try {
            JSONArray json = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject tempJson = new JSONObject();
                tempJson.put("BOOK_NAME", cursor.getString(0));
                tempJson.put("BOOK_HEARD", cursor.getString(1));
                tempJson.put("BOOK_DATE", cursor.getString(2));
                json.put(tempJson);
            }
            cursor.close();
            return json;
        }catch(Exception e){ }
        return new JSONArray();
    }

    public String[] getBook(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HEARD_BOOKS,
                COLUMNS, " " + BOOK_NAME + " = ?", new String[] { name }, null, null, null, null);

        String[] book = new String[3];
        try {
            cursor.moveToFirst();
            book[0] = cursor.getString(0); //id
            book[1] = cursor.getString(1); //heard
            book[2] = cursor.getString(2); //date
            return book;
        }catch(Exception e){}
        db.close();
        return book;
    }
    public void updateTrack(String[] track) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BOOK_HEARD, track[1]);
        values.put(BOOK_DATE, track[2]);

        int i = db.update(TABLE_HEARD_BOOKS, values, BOOK_NAME + " = ?", new String[] { track[0] });
        db.close();
    }

    public static SQLiteHandler getHandler(Context context) {
        if(dbHandler == null) {
            dbHandler = new SQLiteHandler(context);
        }

        return dbHandler;
    }
}
