package github.popeen.dsub.util;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by P on 2015-10-13.
 */
public class BookInfoAPI extends AsyncTask<String, Void, String> {

    private Context context;

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
    protected String doInBackground(String... urls) {
        try {
            String input = readJson(urls[0]);
            JSONObject json = new JSONObject(input);

            if(Util.isTagBrowsing(context)){
                return json.getJSONObject("subsonic-response").getJSONObject("album").get("description").toString();
            }else {
                return json.getJSONObject("subsonic-response").getJSONObject("directory").get("description").toString();
            }

        } catch (Exception e) {
            this.exception = e;
            return "Couldn't collect any info about the book, is the server running a Booksonic server?";
        }
    }

    protected void onPostExecute(String string) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}