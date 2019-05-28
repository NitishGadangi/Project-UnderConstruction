package nitish.build.com.saavntest1;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHandlers {
    static String albumApiLink="https://www.jiosaavn.com/api.php?_format=json&__call=content.getAlbumDetails&albumid=",
                    searchApiLink="https://www.jiosaavn.com/api.php?_format=json&__call=autocomplete.get&query=",
                    playlistApiLink="https://www.jiosaavn.com/api.php?_format=json&__call=playlist.getDetails&listid=",
                    artistApiLink="";

    static String getContent(String finUrl){
        String finString="FAILED";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        org.apache.http.client.HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
        HttpGet httpget = new HttpGet(finUrl);
        httpget.setHeader("User-Agent","Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0");// Set the action you want to do
        try {
            HttpResponse response = httpclient.execute(httpget); // Executeit
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent(); // Create an InputStream with the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) // Read line by line
                sb.append(line + "\n");

            finString = sb.toString(); // Result is here
            Log.i("HTTP",finString);

            is.close(); // Close the stream
        }catch (Exception e){
            Log.i("Exception",e.toString());
        }
        return finString;
    }

    static String getAlbumJson(String albumID){
        String finJson ="FAILED";
        String finUrl= albumApiLink+albumID;
        String jsonData=getContent(finUrl);
        jsonData= jsonData.substring(jsonData.indexOf("{"));
        if (jsonData.contains("title"))
            finJson=jsonData;
        return finJson;
    }

    static String getPlaylistJson(String playlistID){
        String finJson ="FAILED";
        String finUrl= playlistApiLink+playlistID;
        String jsonData=getContent(finUrl);
        jsonData= jsonData.substring(jsonData.indexOf("{"));
        if (jsonData.contains("listid"))
            finJson=jsonData;
        return finJson;
    }

    static String getAlbumID(String url){
        String resID="FAILED";
        String data =getContent(url);
        Document doc = Jsoup.parse(data);

        Elements element = doc.select(".play");

        try {
            String tempData = element.toString();
            tempData = tempData.substring(tempData.indexOf("['albumid','"),tempData.indexOf("'])"));
            tempData=tempData.replace("['albumid','","");
            Log.i("ALlog",tempData);
            resID=tempData;
        }catch (Exception e){
            resID="FAILED";
        }

//        String str_ptrn = "\\[\'albumid\',\'"+"(.*?)"+"\'\\]";
//
//        Pattern ptrn_albumId= Pattern.compile(str_ptrn);
//        Matcher mat_albId = ptrn_albumId.matcher(element.toString());
//        Pattern ptrn_albumName= Pattern.compile(str_NamePtrn);
//        Matcher mat_albName = ptrn_albumName.matcher(element.toString());

//        while (mat_albId.find())
//            resID = mat_albId.group(1);
//        while (mat_albName.find())
//            resName = mat_albName.group(1);
        resID=resID.replace(" ","");

        return resID;
    }

    static String getPlaylistID(String url){
        String resID="FAILED";
        String data=getContent(url);
        Document doc = Jsoup.parse(data);

        Elements element = doc.select(".flip-layout");
        data=element.toString();
        Log.i("PLlog",element.toString());
        try {
            data=data.substring(data.indexOf("<"),data.indexOf(">")+1);
            data=data.substring(data.indexOf("data-listid=\""),data.indexOf("\">")+1);
            data=data.replace("data-listid=\"","");
            data=data.replace("\"","");
            Log.i("PLlog2",data);
            resID=data;
            resID=resID.replace(" ","");
            return resID;
        }catch(Exception e){
            return resID;
        }

    }

    static String getDownloadLink(JSONObject songJsn) throws JSONException {

        String downUrl = songJsn.getString("media_preview_url");
        downUrl=downUrl.replace("96_p.mp4","320.mp4");
        downUrl=downUrl.replace("preview","aac");

        return downUrl;
    }

    static String getAlbumName(JSONObject songJsn) throws JSONException {
        return songJsn.getString("album")+"-"+songJsn.getString("year");
    }

    static String makeDir(String folderName){
        File albdirectory = new File(Environment.getExternalStorageDirectory()+ "/Saavn_Downloader/"+folderName+"/");
        if (!albdirectory.exists())
            if(albdirectory.mkdirs())
                Log.i("DIRDONE2","2DONE");
            else
                Log.i("DIRDONE2","FAILED");
        return albdirectory.getPath();
    }

    static String getSearchResult(String query) throws JSONException {
        Log.i("SEDATA","I am here");
        String jsonData=getContent(searchApiLink+query);
        jsonData=jsonData.substring(jsonData.indexOf("{"));
        Log.i("SEDATA",jsonData);
        JSONObject fullJson = new JSONObject(jsonData);
        JSONObject albumsJson = new JSONObject(fullJson.getString("albums"));
        JSONObject playlistsJson = new JSONObject(fullJson.getString("playlists"));


        Log.i("SEDATA_AL",albumsJson.getString("data"));
        Log.i("SEDATA_PL",playlistsJson.getString("data"));

        String al=albumsJson.getString("data"),pl=playlistsJson.getString("data");
        String tempResJson="";
        if(pl.equals("[]"))
            tempResJson="["+al.substring(1,al.length()-1)+"]";
        else if(al.equals("[]"))
            tempResJson="["+pl.substring(1,pl.length()-1)+"]";
        else {
            al = al.replace("[","").replace("]","");
            pl = pl.replace("[","").replace("]","");
            tempResJson = "[" + al+ "," + pl + "]";
        }

        bigLog("SEDATA_FL",tempResJson);

        if(tempResJson.length()>3){
            return tempResJson;
        }
        return "[]";
    }
    static void bigLog(String TAG, String message) {
        int maxLogSize = 1500;
        for(int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            Log.i(TAG+""+i, message.substring(start, end));
        }
    }

    static String getSongDuration(JSONObject songJson) throws JSONException {
        String durStr =songJson.getString("duration");
        int dur = Integer.parseInt(durStr);
        int min = dur/60;
        int sec = dur%60;
        if (sec<=9)
            return min+":0"+sec;
        else
            return min+":"+sec;

    }

    static String getLinkType(String url){
        String albumId=getAlbumID(url);
        String playlistId=getPlaylistID(url);
        if(!(albumId.equals("FAILED"))){
            return "ALBUM";
        }else if(!(playlistId.equals("FAILED"))){
            return "PLAYLIST";
        }
        return "FAILED";
    }

    static int generateNotificationID(String songId){
        int chrAscii=0;
        String resID="";
        for(int i=0;i<songId.length();i++){
           chrAscii= (int)songId.charAt(i);
           resID+=chrAscii;
        }
        try {
            int res=Integer.parseInt(resID);
            return res;
        }catch (Exception e){
            return 200;
        }



    }


}
