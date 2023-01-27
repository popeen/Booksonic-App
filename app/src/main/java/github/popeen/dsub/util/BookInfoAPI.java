package github.popeen.dsub.util;

import android.content.Context;
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
        if (returnData[0].equals("noInfo")) {

            // If the apps language is Swedish try to get the info from Boktipset
            if (context.getResources().getConfiguration().locale.getDisplayLanguage().equals("svenska")) {
                try {

                    String url = "http://api.boktipset.se/book/search.cgi?accesskey=" + EnvironmentVariables.BOKTIPSET_API_KEY + "&format=json&value=" + URLEncoder.encode(apiParams[0].getTitle().replace("" + apiParams[0].getYear() + " - ", "").replace(" - Svensk", "").replace(" - Engelsk", ""), Constants.UTF_8);
                    String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    Log.w("Boktipset", input);
                    JSONObject json = new JSONObject(input);
                    String title = json.getJSONObject("answer").getJSONObject("books").getJSONArray("book").getJSONObject(0).getString("name");
                    String id = json.getJSONObject("answer").getJSONObject("books").getJSONArray("book").getJSONObject(0).getString("id");


                    url = "http://api.boktipset.se/book/book.cgi?accesskey=" + EnvironmentVariables.BOKTIPSET_API_KEY + "&format=json&book=" + id;
                    input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    String description = new JSONObject(input).getJSONObject("answer").getString("saga").replace("\n", " ").replace("<br />", "").replace("<br/>", "").replace("<p>", "").replace("</p>", "").replace("<b>", "").replace("</b>", "").replaceAll("\\s+", " ").trim();

                    returnData[0] = "Följande beskrivning är för boken " + title + ".\n\nBeskrivningen hämtades automatiskt från Boktipset.se\n\n" + description;

                } catch (Exception e) {
                    Log.w("Boktipset Error", e);
                }
            }

            //If we still have no info try to get it from Google Books
            if (returnData[0].equals("noInfo")) {
                try {

                    String url = "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(apiParams[0].getAuthor(), Constants.UTF_8) + "+" + URLEncoder.encode(apiParams[0].getTitle(), Constants.UTF_8);
                    String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    Log.w("GoogleBooks", input);
                    JSONObject json = new JSONObject(input);

                    try {
                        String description = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("description").toString();
                        returnData[0] = "The following description is for the book " + json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("title") + ".\n\nIt was automatically fetched from google books\n\n" + description;
                    } catch (Exception e) {
                        String description = json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("description").toString();
                        returnData[0] = "The following is for the book " + json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("title") + ".\nIt was automatically fetched from google books\n\n" + description;
                    }

                } catch (Exception e) {
                    Log.w("GoogleBooks Error", e);
                }
            }
        }

        return returnData;
    }

    protected void onPostExecute(String[] string) {
    }
}