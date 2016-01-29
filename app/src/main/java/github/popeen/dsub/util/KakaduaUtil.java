package github.popeen.dsub.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Random;

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

    public static char randomChar(){
        Random r = new Random();
        int c = r.nextInt(26) + (byte)'a';
        return (char) c;
    }
}
