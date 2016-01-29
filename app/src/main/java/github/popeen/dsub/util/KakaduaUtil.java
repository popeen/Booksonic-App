package github.popeen.dsub.util;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import github.popeen.dsub.R;

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

}
