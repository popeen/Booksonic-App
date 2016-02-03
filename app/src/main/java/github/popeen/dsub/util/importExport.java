package github.popeen.dsub.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import github.popeen.dsub.R;

/**
 * Created by P on 29-Jan-16.
 */
public class importExport {

    public static void importData(Context context, String path){
        importData(context, path, true);
    }
    public static void importData(Context context, String path, Boolean overWriteServers){
        SQLiteHandler sqlh = SQLiteHandler.getHandler(context);
        SongDBHandler sdbh = SongDBHandler.getHandler(context);

        String jsonString = "{}";
        try{ jsonString = KakaduaUtil.getStringFromFile(path); }catch (Exception e){}


        try{ //import Booksonic.db
            sqlh.importData(new JSONArray(new JSONObject(jsonString).get("Booksonic").toString()));
        }catch (Exception e){}


        try{ //import SongDB.db
            sdbh.importData(new JSONArray(new JSONObject(jsonString).get("SongDB").toString()));
        }catch(Exception e){}


        try{ //import Shared_Preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            JSONArray array = new JSONArray(new JSONObject(jsonString).get("Prefs").toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);
                String num;
                if(overWriteServers){num = Integer.toString( i + 1);}
                else{num = Integer.toString(prefs.getInt("serverCount", 0) + i + 1);}
                editor.putString("serverName" + num, row.getString("serverName"));
                editor.putString("username" + num, row.getString("username"));
                editor.putString("password" + num, KakaduaUtil.base64Decode(row.getString("password").substring(1)));
                editor.putString("serverUrl" + num, row.getString("serverUrl"));
                editor.putString("serverInternalUrl" + num, row.getString("serverInternalUrl"));
                editor.putInt("mostRecentCount" + num, row.getInt("mostRecentCount"));
                editor.putInt("serverCount", Integer.parseInt(num));
            }
            editor.commit();
        }catch(Exception e){}

        KakaduaUtil.toastLong(context, context.getString(R.string.imported));
    }

    public static void exportData(Context context){
        final Context con = context;

        final EditText txtUrl = new EditText(con);
        txtUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if(Constants.EXPORT_REQUIRES_PASS) {
            new AlertDialog.Builder(con)
                    .setTitle(context.getString(R.string.export_title))
                    .setMessage(context.getString(R.string.export_server_text))
                    .setView(txtUrl)
                    .setPositiveButton(context.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (txtUrl.getText().toString().equals(prefs.getString("password1", "")) && txtUrl.getText().toString() != "") {
                                importExport.doExport(con, true);
                            } else {
                                KakaduaUtil.toastLong(con, con.getString(R.string.export_wrong_password));
                                importExport.doExport(con, false);
                            }
                        }
                    })
                    .setNegativeButton(context.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            importExport.doExport(con, false);
                        }
                    })
                    .show();
        }else{
            importExport.doExport(con, true);
        }
    }

    public static void doExport(Context context, boolean shouldExportServers){
        SQLiteHandler sqlh = SQLiteHandler.getHandler(context);
        SongDBHandler sdbh = SongDBHandler.getHandler(context);

        JSONObject json = new JSONObject();


        try{ //Backup Booksonic.db
            json.put("Booksonic", sqlh.exportData());
        }catch(Exception e){}


        try{ //Backup SongDB.db
            json.put("SongDB", sdbh.exportData());
        }catch(Exception e){}


        try{ //write to export file
            File file = new File(Environment.getExternalStorageDirectory(), "booksonic_backup.json");
            FileOutputStream fileos = new FileOutputStream(file.getPath());
            fileos.write(json.toString().getBytes());
            fileos.close();
        }catch(Exception e){ }

        if(shouldExportServers) {
            try { //Backup Shared_Preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                JSONArray jsonPrefs = new JSONArray();
                for (int i = 0; i < prefs.getInt("serverCount", 0); i++) {
                    JSONObject tempJson = new JSONObject();
                    String num = Integer.toString(i + 1);
                    tempJson.put("serverName", prefs.getString("serverName" + num, ""));
                    tempJson.put("username", prefs.getString("username" + num, ""));
                    //OBS, Base 64 IS NOT ENCRYPTION, it is only used so the password will not be in clear text readable to humans but a potential attacker would get the password from it in less then a second.
                    tempJson.put("password", KakaduaUtil.randomChar() + KakaduaUtil.base64Encode(prefs.getString("password" + num, "")).replace("=", ""));
                    tempJson.put("serverUrl", prefs.getString("serverUrl" + num, ""));
                    tempJson.put("serverInternalUrl", prefs.getString("serverInternalUrl" + num, ""));
                    tempJson.put("mostRecentCount", prefs.getInt("mostRecentCount" + num, 0));
                    jsonPrefs.put(tempJson);
                }
                json.put("Prefs", jsonPrefs);
            } catch (Exception e) {}
        }

        try { // Write to file
            File file = new File(Environment.getExternalStorageDirectory(), "booksonic_backup.json");
            FileOutputStream fileos = new FileOutputStream(file.getPath());
            fileos.write(json.toString().getBytes());
            fileos.close();
        }catch (Exception e){}

        KakaduaUtil.toastLong(context, context.getString(R.string.exported) + " " + Environment.getExternalStorageDirectory() + "/booksonic_backup.json");
    }
}
