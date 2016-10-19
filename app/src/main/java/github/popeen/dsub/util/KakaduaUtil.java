package github.popeen.dsub.util;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import github.popeen.dsub.R;
import github.popeen.dsub.service.ssl.SSLSocketFactory;
import github.popeen.dsub.service.ssl.TrustSelfSignedStrategy;

/**
 * Created by P on 29-Jan-16.
 */
public class KakaduaUtil {

    public static String base64Encode(String s){
        byte[] data = new byte[0];
        try{
            data = s.getBytes("UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }finally{
            return Base64.encodeToString(data, Base64.NO_WRAP);
        }
    }

    public static String base64Decode(String s){
        byte[] dataDec = Base64.decode(s, Base64.NO_WRAP);
        String decodedString = "";
        try{
            decodedString = new String(dataDec, "UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }finally{
            return decodedString;
        }
    }

    public static void toastShort(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public static char randomChar(){
        return randomChar("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    public static char randomChar(String alphabet){
        return alphabet.charAt(new Random().nextInt(alphabet.length()));
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception{
        FileInputStream fin = new FileInputStream(new File(filePath));
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    public static String http_get_contents(String url) {
        return http_get_contents(url, "UTF-8");
    }
    public static String http_get_contents(String url, String encoding) {
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

    public static String http_get_contents_all_cert(String url) { return http_get_contents_all_cert(url, "UTF-8"); }
    public static String http_get_contents_all_cert(String str, String encoding){

        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 20);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(20));
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 10000);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", createSSLSocketFactory(), 443));
        ThreadSafeClientConnManager connManager;
        DefaultHttpClient client;
        connManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(connManager, params);
        StringBuilder builder = new StringBuilder();
        HttpGet httpGet = new HttpGet(str);
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

    private static SocketFactory createSSLSocketFactory() {
        try {
            return new SSLSocketFactory(new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Throwable x) {
            Log.e("TAG", "Failed to create custom SSL socket factory, using default.", x);
            return org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
        }
    }
}
