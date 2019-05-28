package nitish.build.com.saavntest1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
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
import com.wang.avi.AVLoadingIndicatorView;

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
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Album_Song_List extends AppCompatActivity {
    String albumID,jsonData,finUrl,downUrl,downpath,fName,folderName="RandomAlbum",
            chanelId="test1",dataType,dot=" • ",url_img="FAILED";
    JSONArray songArr ;
    JSONObject songObj;
    int listSize=1,notificationID=100;
    Long totMB,curMB;
    AVLoadingIndicatorView avl;
    TextView tv_ALPLname,tv_artists,tv_TypeTot;
    ImageView img_album;
    DecimalFormat f = new DecimalFormat("##.00");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album__song__list);

        avl=findViewById(R.id.AVL_ALBUM_LOADING);
        Button btn_back=findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent fromHoome = getIntent();
        albumID = fromHoome.getStringExtra("TYPE_ID");
        dataType=fromHoome.getStringExtra("TYPE");

        tv_ALPLname=findViewById(R.id.tv_ALPLname);
        tv_artists=findViewById(R.id.tv_artists);
        tv_TypeTot=findViewById(R.id.tv_typetot);
        img_album=findViewById(R.id.img_album);

        if (dataType.equals("ALBUM"))
            jsonData=DataHandlers.getAlbumJson(albumID);
        else if (dataType.equals("PLAYLIST"))
            jsonData=DataHandlers.getPlaylistJson(albumID);





        Log.i("OLDJSON",jsonData);
        try {
            JSONObject albumJson = new JSONObject(jsonData);
            Log.i("JSONDATA",albumJson.toString());
            if (dataType.equals("ALBUM")){
                tv_artists.setText(albumJson.getString("primary_artists"));
                tv_ALPLname.setText(albumJson.getString("title"));
            }
            else{
                tv_artists.setText(albumJson.getString("follower_count")+" followers");
                tv_ALPLname.setText(albumJson.getString("listname"));
            }
            url_img=albumJson.getString("image");
            if(url_img.length()<=5)
                url_img="FAILED";
            songArr = new JSONArray(albumJson.getString("songs"));
            Log.i("SONGARR",songArr.toString());
            listSize = songArr.length();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        tv_TypeTot.setText(dataType+dot+listSize+" Songs");
        File imgFile = new  File(DataHandlers.makeDir("cache")+"/"+tv_ALPLname.getText()+".jpg");
        if(dataType.equals("ALBUM") && imgFile.exists()){
            Toast.makeText(Album_Song_List.this, "SET", Toast.LENGTH_SHORT).show();
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img_album.setImageBitmap(myBitmap);
        }else{
        PRDownloader.download(url_img, DataHandlers.makeDir("cache"), tv_ALPLname.getText()+".jpg")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        Toast.makeText(Album_Song_List.this, "START_IMG", Toast.LENGTH_SHORT).show();
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

                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        File imgFile = new  File(DataHandlers.makeDir("cache")+"/"+tv_ALPLname.getText()+".jpg");
//                        Log.i("IMGPATH1",imgFile.exists());
                        Log.i("IMGPATH2",imgFile.getAbsolutePath());
                        if(imgFile.exists()){
                            Toast.makeText(Album_Song_List.this, "SET", Toast.LENGTH_SHORT).show();
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            img_album.setImageBitmap(myBitmap);
                        }
                    }

                    @Override
                    public void onError(Error error) {

                    }
                });}



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
                    notificationID=DataHandlers.generateNotificationID(songJsn.getString("id"));
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
                                totMB=progress.totalBytes/1048576;
                                curMB=progress.currentBytes/1048576;

                              mBuilder.setProgress((int)progress.totalBytes,(int) progress.currentBytes,false);
                              notificationManagerCompat.notify(notificationID,mBuilder.build());
                              mBuilder.setContentText(f.format(curMB)+"MB/"+f.format(totMB)+"MB");
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
            TextView duration = convertView.findViewById(R.id.cus_duration);

            try {
                songObj =songArr.getJSONObject(position);
                songName.setText(songObj.getString("song"));
                artists.setText(songObj.getString("album")+dot+
                        songObj.getString("primary_artists"));
                duration.setText(DataHandlers.getSongDuration(songObj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (position==(listSize-1))
                avl.hide();


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
