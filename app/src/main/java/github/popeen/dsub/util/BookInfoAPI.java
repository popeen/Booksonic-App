package github.popeen.dsub.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

import github.popeen.dsub.domain.ServerInfo;

/**
 * Created by Patrik "Popeen" Johansson on 2015-10-13.
 */


public class BookInfoAPI extends AsyncTask<BookInfoAPIParams, Void, String[]> {

    private final Context context;

    public BookInfoAPI(Context context) {
        this.context = context;
    }

    protected String[] doInBackground(BookInfoAPIParams... apiParams) {
        String[] returnData = new String[2];
        returnData[0] = "noInfo";


        // Get the info from the servers API
        try {
            String url = apiParams[0].getURL();
            String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

            JSONObject json = new JSONObject(input);
            String respRoot = "subsonic-response";
            if (ServerInfo.isMadsonic6(context)) {
                respRoot = "madsonic-response";
            }

            if (Util.isTagBrowsing(context)) {
                returnData[0] = json.getJSONObject(respRoot).getJSONObject("album").get("description").toString();
                returnData[1] = json.getJSONObject(respRoot).getJSONObject("album").get("reader").toString();
            } else {
                returnData[0] = json.getJSONObject(respRoot).getJSONObject("directory").get("description").toString();
                returnData[1] = json.getJSONObject(respRoot).getJSONObject("directory").get("reader").toString();
            }
        } catch (Exception ignored) {
        }

        // If the server has no info or is not Booksonic API compatible try to get it from the internet
        SharedPreferences prefs = Util.getPreferences(context);
        if ((returnData[0].equals("noInfo") || returnData[0].equals("No description availiable")) && prefs.getBoolean(Constants.PREFERENCES_KEY_ENABLE_INTERNET_METADATA, true)){

            String language = context.getResources().getConfiguration().locale.getDisplayLanguage();
            try {

                String services = "";
                if(prefs.getBoolean(Constants.PREFERENCES_KEY_ALLOW_AI, true)) {
                    services += "ai,";
                }
                if(prefs.getBoolean(Constants.PREFERENCES_KEY_ALLOW_GOOGLE, true)) {
                    services += "google,";
                }
                if(prefs.getBoolean(Constants.PREFERENCES_KEY_ALLOW_BOKTIPSET, true)) {
                    services += "boktipset,";
                }


                String url = "https://booksonic.org/api/bookinfo/?author=" + URLEncoder.encode(apiParams[0].getAuthor(), Constants.UTF_8) + "&book=" + URLEncoder.encode(apiParams[0].getTitle(), Constants.UTF_8) + "&lang=" + language + "&services=" + services;
                String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                Log.w("Booksonic URL", url);
                Log.w("Booksonic response", input);
                JSONObject json = new JSONObject(input);

                try {
                    String description = json.get("description").toString();
                    returnData[0] = description;
                } catch (Exception ignored) {
                }

            } catch (Exception e) {
                Log.w("Booksonic data error", e);
            }
        }

        return returnData;
    }

    protected void onPostExecute(String[] string) {
    }
}