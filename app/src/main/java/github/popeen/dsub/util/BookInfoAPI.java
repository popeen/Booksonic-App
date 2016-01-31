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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by P on 2015-10-13.
 */


public class BookInfoAPI extends AsyncTask<BookInfoAPIParams, Void, String[]> {

    private Context context;
    private String author;
    private String bookName;

    public BookInfoAPI(Context context){
        this.context = context;
    }
    private Exception exception;

    public String readJsonutf8(String url){
        Reader reader = null;
        StringBuilder builder = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            // ...
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
            char[] buffer = new char[8192];

            for (int length = 0; (length = reader.read(buffer)) > 0;) {
                builder.append(buffer, 0, length);
                //loading.setProgress(length);
            }
        } catch(Exception e){}
        finally {
            conn.disconnect();
            if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
        }

        return builder.toString();
    }
    public String readJson(String url) {
        return readJson(url, "UTF-8");
    }
    public String readJson(String url, String encoding) {
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(content, encoding));
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

    public static String convertToUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    protected String[] doInBackground(BookInfoAPIParams... apiParams) {
        String[] returnData = new String[2];
        returnData[0] = "noInfo";

        try {

            String input = readJson(apiParams[0].getURL());
            JSONObject json = new JSONObject(input);

            if(Util.isTagBrowsing(context)){
                returnData[0] = json.getJSONObject("subsonic-response").getJSONObject("album").get("description").toString();
                returnData[1] = json.getJSONObject("subsonic-response").getJSONObject("album").get("reader").toString();
            }else {
                returnData[0] = json.getJSONObject("subsonic-response").getJSONObject("directory").get("description").toString();
                returnData[1] = json.getJSONObject("subsonic-response").getJSONObject("directory").get("reader").toString();
            }
        } catch (Exception e) {
            this.exception = e;
        }

        if(returnData[0].equals("noInfo")){
            if(context.getResources().getConfiguration().locale.getDisplayLanguage().equals("svenska")) {
                try {
                    String boktipsetUrl1 = "http://api.boktipset.se/book/search.cgi?accesskey="+EnvironmentVariables.BOKTIPSET_API_KEY+"&format=json&value="+URLEncoder.encode(apiParams[0].getTitle().replace(""+apiParams[0].getYear()+" - ", "").replace(" - Svensk", "").replace(" - Engelsk", ""), "iso-8859-1");
                    Log.w("Boktipset", boktipsetUrl1);
                    String input = readJson(boktipsetUrl1, "iso-8859-1");
                    Log.w("Boktipset", input);
                    JSONObject json = new JSONObject(input);
                    String title = json.getJSONObject("answer").getJSONObject("books").getJSONArray("book").getJSONObject(0).getString("name");
                    String id = json.getJSONObject("answer").getJSONObject("books").getJSONArray("book").getJSONObject(0).getString("id");

                    String boktipsetUrl2 = "http://api.boktipset.se/book/book.cgi?accesskey="+EnvironmentVariables.BOKTIPSET_API_KEY+"&format=json&book=" + id;
                    Log.w("Boktipset", boktipsetUrl2);
                    String input2 = readJson(boktipsetUrl2, "iso-8859-1");
                    Log.w("Boktipset", input2);
                    String description = new JSONObject(input2).getJSONObject("answer").getString("saga").replace("\n", " ").replace("<br />", "").replace("<br/>", "").replace("<p>", "").replace("</p>", "").replace("<b>", "").replace("</b>", "").replaceAll("\\s+", " ").trim();
                    Log.w("Boktipset", description);
                    returnData[0] = description+" \n<b>Den här bekrivningen för "+title+" hämtades automatiskt från Boktipset.se</b>";
                }catch(Exception e){Log.w("Boktipset Error", e);}
            }
            if(returnData[0].equals("noInfo")){ //Language is not swedish or no description was collected
                try {
                    Log.w("GoogleBooks", "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(apiParams[0].getAuthor(), "UTF-8") + "+" + URLEncoder.encode(apiParams[0].getTitle(), "UTF-8"));
                    String input = readJson("https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(apiParams[0].getAuthor(), "UTF-8") + "+" + URLEncoder.encode(apiParams[0].getTitle(), "UTF-8"));
                    Log.w("GoogleBooks", input);
                    JSONObject json = new JSONObject(input);

                    try {
                        returnData[0] = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("description").toString() + "\n<b>This description for " + json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("title").toString() + " was automatically fetched from google books</b>";
                    } catch (Exception e) {
                        returnData[0] = json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("description").toString() + "\n<b>This description for " + json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("title").toString() + " was automatically fetched from google books</b>";
                    }

                } catch (Exception e) {
                    Log.w("GoogleBooks Error", e);
                }
            }
        }

        return returnData;
    }

    protected void onPostExecute(String[] string){}
}