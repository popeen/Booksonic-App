package github.popeen.dsub.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.util.StringUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

/**
 * Created by P on 2015-10-13.
 */


public class BookInfoAPI extends AsyncTask<BookInfoAPIParams, Void, String> {

    private Context context;
    private String author;
    private String bookName;

    public BookInfoAPI(Context context){
        this.context = context;
    }
    private Exception exception;

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
    protected String doInBackground(BookInfoAPIParams... apiParams) {
        String returnData = "noInfo";

        try {
            String input = readJson(apiParams[0].getURL());
            JSONObject json = new JSONObject(input);

            if(Util.isTagBrowsing(context)){
                returnData = json.getJSONObject("subsonic-response").getJSONObject("album").get("description").toString();
            }else {
                returnData = json.getJSONObject("subsonic-response").getJSONObject("directory").get("description").toString();
            }
        } catch (Exception e) {
            this.exception = e;
        }

        if(returnData.equals("noInfo")){
            try {
                Log.w("GoogleBooks", "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(apiParams[0].getAuthor(), "UTF-8") + "+" + URLEncoder.encode(apiParams[0].getTitle(), "UTF-8"));
                String input = readJson("https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(apiParams[0].getAuthor(), "UTF-8") + "+" + URLEncoder.encode(apiParams[0].getTitle(), "UTF-8"));
                Log.w("GoogleBooks", input);
                JSONObject json = new JSONObject(input);

                try{ returnData = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("description").toString() + "\nThis description was automatically fetched from google books"; }
                catch(Exception e){ returnData = json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("description").toString() + "\nThis description was automatically fetched from google books"; }

            } catch (Exception e) {
                this.exception = e;
            }
        }

        return returnData;
    }

    protected void onPostExecute(String string) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}