package nitish.build.com.saavntest1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Album_Song_List extends AppCompatActivity {
    String albumID,jsonData,finUrl,downUrl,downpath,fName,folderName="RandomAlbum",chanelId="test1";
    JSONArray songArr ;
    JSONObject songObj;
    int listSize=1,notificationID=100;
    Long percentP= Long.valueOf(0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album__song__list);

        Intent fromHoome = getIntent();
        albumID = fromHoome.getStringExtra("ALBUM_ID");
        jsonData=DataHandlers.getAlbumJson(albumID);




        Log.i("OLDJSON",jsonData);
        try {
            JSONObject albumJson = new JSONObject(jsonData);
            Log.i("JSONDATA",albumJson.toString());
            songArr = new JSONArray(albumJson.getString("songs"));
            Log.i("SONGARR",songArr.toString());
            listSize = songArr.length();

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ListView song_list =findViewById(R.id.song_list);
        CustomAdapter song_list_Adapter = new CustomAdapter();
        song_list.setAdapter(song_list_Adapter);

        // Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        File directory = new File(Environment.getExternalStorageDirectory()+ "/Saavn_Downloader/");
        if (!directory.exists())
            if(directory.mkdirs())
                Log.i("DIRDONE1","1DONE");
            else
                Log.i("DIRDONE1","FAILED");

        createNotificationChannel();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(),chanelId)
                .setSmallIcon(R.drawable.ic_arrow_downward)
                .setContentTitle("Download Name")
                .setContentText("progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100,0,false);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());




        song_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject songJsn;
                try {
                    songJsn = songArr.getJSONObject(position);
                    Log.i("SJSON",songJsn.toString());
                    folderName = DataHandlers.getAlbumName(songJsn);
                    fName=songJsn.getString("song")+".m4a";
                    downUrl=DataHandlers.getDownloadLink(songJsn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("DOWNURL",downUrl);


                downpath = DataHandlers.makeDir(folderName);
                Log.i("DOWNPATH",downpath);




                int downloadId = PRDownloader.download(downUrl, downpath, fName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                mBuilder.setContentTitle(fName.replace("m4a","")).setOnlyAlertOnce(true);
                                Toast.makeText(Album_Song_List.this, "DOWNLOAD STARTED", Toast.LENGTH_SHORT).show();
                                notificationManagerCompat.notify(notificationID,mBuilder.build());
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {

                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                Log.i("CURDEMO",Long.toString(progress.currentBytes));
                                Log.i("TOTDEMO",Long.toString(progress.totalBytes));
                                percentP =  ((progress.currentBytes/progress.totalBytes)*100);

                              mBuilder.setProgress((int)progress.totalBytes,(int) progress.currentBytes,false);
                              notificationManagerCompat.notify(notificationID,mBuilder.build());
                              mBuilder.setContentText(percentP+"%");
                              notificationManagerCompat.notify(notificationID,mBuilder.build());

                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                Toast.makeText(Album_Song_List.this, "completed!", Toast.LENGTH_SHORT).show();
                                mBuilder.setContentText("Download Complete");
                                mBuilder.setProgress(0,0,false);
                                notificationManagerCompat.notify(notificationID,mBuilder.build());
                            }

                            @Override
                            public void onError(Error error) {

                            }
                        });



            }
        });









    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            //Log.i("Countaaaa",Integer.toString(COURSES.length));
            //Toast.makeText(syllabus_select_course.this, COURSES.length, Toast.LENGTH_SHORT).show();
            return (listSize);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.custom_download_list,null);

            TextView songName= convertView.findViewById(R.id.cus_songName);
            TextView artists = convertView.findViewById(R.id.cus_artist);

            try {
                songObj =songArr.getJSONObject(position);
                songName.setText(songObj.getString("song"));
                artists.setText(songObj.getString("primary_artists"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return convertView;
        }
    }

    public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name ="Download Notif";
            String description = "include all download notif";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(chanelId,name,importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


}
