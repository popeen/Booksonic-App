package github.popeen.dsub.updates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import github.popeen.dsub.BuildConfig;

public class UpdateApp extends AsyncTask<String,Void,Void> {
    private Context context;
    private String UPDATEURL = "https://popeen.com/files/mobile/MY_APPS/PopeensDsub/update.php?v=";
    private String PATH = "/mnt/sdcard/Download/";
    private String APKNAME = "ljudbok-update.apk";

    public void setContext(Context contextf){
        context = contextf;
    }
    public String readJson(String url) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    @Override
    protected Void doInBackground(String... arg0) {
        Boolean shouldUpdate = false;
        try {
            Log.w("UpdateChecker", UPDATEURL+BuildConfig.VERSION_CODE);
            String input = readJson(UPDATEURL+BuildConfig.VERSION_CODE);
            JSONObject json = new JSONObject(input);

            Log.w("UpdateChecker", "Should I download an update: " + json.get("shouldUpdate").toString());
            if(json.get("shouldUpdate").toString().equals("yes")){
                shouldUpdate = true;
            }

            if(shouldUpdate){
                URL url = new URL(json.get("link").toString());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, APKNAME);
                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(PATH + APKNAME)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);
            }
        }catch (Exception e) {
                Log.e("UpdateChecker", "Update error! " + e.getMessage());
        }
        return null;
    }
}