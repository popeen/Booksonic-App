package github.popeen.dsub.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

import github.popeen.dsub.domain.ServerInfo;

/**
 * Created by P on 2015-10-13.
 */


public class BookInfoAPI extends AsyncTask<BookInfoAPIParams, Void, String[]> {

    private Context context;

    public BookInfoAPI(Context context){
        this.context = context;
    }
    private Exception exception;
    public String readJson(String url) {
        return readJson(url, "UTF-8");
    }
    public String readJson(String url, String encoding) {
        return KakaduaUtil.http_get_contents_all_cert(url, encoding);
    }

    protected String[] doInBackground(BookInfoAPIParams... apiParams) {
        String[] returnData = new String[2];
        returnData[0] = "noInfo";

        try {
            String url = apiParams[0].getURL();
            String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

            JSONObject json = new JSONObject(input);
            String respRoot = "subsonic-response";
            if(ServerInfo.isMadsonic6(context)){
                respRoot = "madsonic-response";
            }

            if(Util.isTagBrowsing(context)){
                returnData[0] = json.getJSONObject(respRoot).getJSONObject("album").get("description").toString();
                returnData[1] = json.getJSONObject(respRoot).getJSONObject("album").get("reader").toString();
            }else {
                returnData[0] = json.getJSONObject(respRoot).getJSONObject("directory").get("description").toString();
                returnData[1] = json.getJSONObject(respRoot).getJSONObject("directory").get("reader").toString();
            }
        } catch (Exception e) {
            this.exception = e;
        }

        if(returnData[0].equals("noInfo")){
            if(context.getResources().getConfiguration().locale.getDisplayLanguage().equals("svenska")) {
                try {

                    String url = "http://api.boktipset.se/book/search.cgi?accesskey="+EnvironmentVariables.BOKTIPSET_API_KEY+"&format=json&value="+URLEncoder.encode(apiParams[0].getTitle().replace(""+apiParams[0].getYear()+" - ", "").replace(" - Svensk", "").replace(" - Engelsk", ""));
                    String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    Log.w("Boktipset", input);
                    JSONObject json = new JSONObject(input);
                    String title = json.getJSONObject("answer").getJSONObject("books").getJSONArray("book").getJSONObject(0).getString("name");
                    String id = json.getJSONObject("answer").getJSONObject("books").getJSONArray("book").getJSONObject(0).getString("id");


                    url = "http://api.boktipset.se/book/book.cgi?accesskey="+EnvironmentVariables.BOKTIPSET_API_KEY+"&format=json&book=" + id;
                    input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    String description = new JSONObject(input).getJSONObject("answer").getString("saga").replace("\n", " ").replace("<br />", "").replace("<br/>", "").replace("<p>", "").replace("</p>", "").replace("<b>", "").replace("</b>", "").replaceAll("\\s+", " ").trim();

                    returnData[0] = "Följande beskrivning är för boken "+title+".\n\nBeskrivningen hämtades automatiskt från Boktipset.se\n\n" + description;
                }catch(Exception e){Log.w("Boktipset Error", e);}
            }
            if(returnData[0].equals("noInfo")){ //Language is not swedish or no description was collected from boktipset
                try {
                    String url = "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(apiParams[0].getAuthor()) + "+" + URLEncoder.encode(apiParams[0].getTitle());
                    String input = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    Log.w("GoogleBooks", input);
                    JSONObject json = new JSONObject(input);

                    try {
                        String description = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("description").toString();
                        returnData[0] =  "The following description is for the book " + json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").get("title").toString() + ".\n\nIt was automatically fetched from google books\n\n" + description;
                    } catch (Exception e) {
                        String description = json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("description").toString();
                        returnData[0] =  "The following is for the book " + json.getJSONArray("items").getJSONObject(1).getJSONObject("volumeInfo").get("title").toString() + ".\nIt was automatically fetched from google books\n\n" + description;
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