package nitish.build.com.saavntest1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Album_Song_List extends AppCompatActivity {
    String albumID,jsonData,finUrl,downUrl,downpath,fName,folderName="RandomAlbum",albumArtUrl="",
            chanelId="test1",dataType,dot=" • ",url_img="FAILED",prevAct="",tempSJson="";
    static String nowDownS="";
    JSONArray songArr ;
    JSONObject songObj;
    int listSize=1,notificationID=100;
    Float totMB,curMB;
    AVLoadingIndicatorView avl;
    TextView tv_ALPLname,tv_artists,tv_TypeTot,tv_DownCount;
    ImageView img_album;
    DecimalFormat f = new DecimalFormat("##.00");
    RadioGroup kbpsGroup;
    RadioButton rad_320,rad_128,rad_96;
    NotificationCompat.Builder mBuilder;
    NotificationManagerCompat notificationManagerCompat;
    Button btn_toDownPage;


//    @Override
//    public void onBackPressed() {
//
//        if (prevAct.equals("WEB_ACT"))
//            startActivity(new Intent(getApplicationContext(),SaavnWebView.class));
//        else if (prevAct.equals("SEARCH_ACT"))
//            startActivity(new Intent(getApplicationContext(),Search_Songs.class));
//        else
//        super.onBackPressed();
//
//    }

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
        prevAct=fromHoome.getStringExtra("PREV_ACT");


        tv_ALPLname=findViewById(R.id.tv_ALPLname);
        tv_artists=findViewById(R.id.tv_artists);
        tv_TypeTot=findViewById(R.id.tv_typetot);
        img_album=findViewById(R.id.img_album);
        kbpsGroup=findViewById(R.id.radioGroup);
        rad_96=findViewById(R.id.radio_96);
        rad_128=findViewById(R.id.radio_160);
        rad_320=findViewById(R.id.radio_320);
        tv_DownCount=findViewById(R.id.tv_totDowns);
        btn_toDownPage=findViewById(R.id.btn_down_alb);

        btn_toDownPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getApplicationContext(),Downloads_Page.class));
            }
        });

        SharedPreferences pref_main = getApplicationContext().getSharedPreferences(getResources().getString(R.string.pref_main),MODE_PRIVATE);
        String tmpS= Integer.toString(pref_main.getInt(getResources().getString(R.string.pref_tot_downloads),0));
        tv_DownCount.setText(tmpS);



        if (dataType.equals("FAILED")){
            new AlertDialog.Builder(Album_Song_List.this)
                    .setTitle("No Album or Playlist Found")
                    .setMessage("Please use another way to find the Song you want to downoad..!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();
            jsonData=getResources().getString(R.string.FailedJson);
            dataType="ALBUM";
        }


        if (dataType.equals("SONG"))
            dataType="ALBUM";

        if (dataType.equals("ALBUM"))
            jsonData=DataHandlers.getAlbumJson(albumID);
        else if (dataType.equals("PLAYLIST"))
            jsonData=DataHandlers.getPlaylistJson(albumID);



        try {
            JSONObject albumJson = new JSONObject(jsonData);

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

            listSize = songArr.length();
            if (albumJson.getString("title").equals("FAILED"))
                listSize=0;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        tv_TypeTot.setText(dataType+dot+listSize+" Songs");


        PRDownloader.download(url_img, DataHandlers.makeDir(".cache"), tv_ALPLname.getText()+".jpg")
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
                        File imgFile = new  File(DataHandlers.makeDir(".cache")+"/"+tv_ALPLname.getText()+".jpg");

                        if(imgFile.exists()){
                            Toast.makeText(Album_Song_List.this, "SET", Toast.LENGTH_SHORT).show();
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            img_album.setImageBitmap(myBitmap);
                        }
                    }

                    @Override
                    public void onError(Error error) {

                    }
                });



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
        mBuilder = new NotificationCompat.Builder(getApplicationContext(),chanelId)
                .setSmallIcon(R.drawable.ic_arrow_downward)
                .setContentTitle("Download Name")
                .setContentText("progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100,0,false);
        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());




        song_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject songJsn;
                String kbps = findViewById(kbpsGroup.getCheckedRadioButtonId()).getTag().toString();

                try {
                    songJsn = songArr.getJSONObject(position);
                    tempSJson=songJsn.toString();

                    SharedPreferences pref_main = getApplicationContext().getSharedPreferences(getResources().getString(R.string.pref_main),MODE_PRIVATE);
                    int totDownloads=pref_main.getInt(getResources().getString(R.string.pref_tot_downloads),0),
                            awaitingDown=pref_main.getInt(getResources().getString(R.string.pref_awaiting),0),
                            nowDownloading=pref_main.getInt(getResources().getString(R.string.pref_now_downloading),0);

                    SharedPreferences.Editor editor=pref_main.edit();
                    editor.putInt(getResources().getString(R.string.pref_awaiting),awaitingDown+1);
                    editor.putInt(getResources().getString(R.string.pref_tot_downloads),totDownloads+1);
                    ArrayList<String> idList=new ArrayList<>();
                    ArrayList<String> songList=new ArrayList<>();
                    try {
                        String ids = pref_main.getString(getResources().getString(R.string.pref_id_list),ObjectSerializer.serialize(new ArrayList<>()));
                        idList=(ArrayList<String>) ObjectSerializer.deserialize(ids);
                        String tempID=songJsn.getString("id");
                        idList.add(tempID);
                        editor.putString(getResources().getString(R.string.pref_JsonFromID)+tempID,tempSJson);
                        editor.putString(getResources().getString(R.string.pref_KbpsFromID)+tempID,kbps);
                        editor.putString(getResources().getString(R.string.pref_id_list),ObjectSerializer.serialize(idList));

                        String songsL = pref_main.getString(getResources().getString(R.string.pref_song_list),ObjectSerializer.serialize(new ArrayList<>()));
                        songList =(ArrayList<String>)ObjectSerializer.deserialize(songsL);
                        String tempSo = songJsn.getString("song")+"_"+kbps+"kbps";
                        songList.add(tempSo);
                        editor.putString(getResources().getString(R.string.pref_song_list),ObjectSerializer.serialize(songList));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int tempCount=pref_main.getInt(getResources().getString(R.string.pref_counter),0);
                    tempCount=tempCount+1;
                    if (tempCount==5){
                        tempCount=0;
                        //Toast.makeText(getApplicationContext(), "Add...", Toast.LENGTH_SHORT).show();
                    }
                    editor.putInt(getResources().getString(R.string.pref_counter),tempCount);
                    editor.commit();
                    String tmpS= Integer.toString(pref_main.getInt(getResources().getString(R.string.pref_tot_downloads),0));
                    tv_DownCount.setText(tmpS);
                    Toast.makeText(getApplicationContext(), "Added to download Queue", Toast.LENGTH_SHORT).show();




                if (nowDownloading==0 && totDownloads==0) {
                    TestDownload testDownload = new TestDownload();
                    testDownload.execute();
                }


//                    folderName = DataHandlers.getAlbumName(songJsn);
//                    fName=songJsn.getString("song")+".m4a";
//                    notificationID=DataHandlers.generateNotificationID(songJsn.getString("id"));
//                    downUrl=DataHandlers.getDownloadLink(songJsn,kbps);
//                    albumArtUrl=songJsn.getString("image");

                } catch (Exception e) {
                    e.printStackTrace();
                }



//                downpath = DataHandlers.makeDir(folderName);





//                int downloadId = PRDownloader.download(downUrl, downpath, fName)
//                        .build()
//                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
//                            @Override
//                            public void onStartOrResume() {
//                                mBuilder.setContentTitle(fName.replace("m4a","")).setOnlyAlertOnce(true);
//                                Toast.makeText(Album_Song_List.this, "DOWNLOAD STARTED", Toast.LENGTH_SHORT).show();
//                                notificationManagerCompat.notify(notificationID,mBuilder.build());
//                            }
//                        })
//                        .setOnPauseListener(new OnPauseListener() {
//                            @Override
//                            public void onPause() {
//
//                            }
//                        })
//                        .setOnCancelListener(new OnCancelListener() {
//                            @Override
//                            public void onCancel() {
//
//                            }
//                        })
//                        .setOnProgressListener(new OnProgressListener() {
//                            @Override
//                            public void onProgress(Progress progress) {

//                                totMB=((float)(progress.totalBytes))/1048576;
//                                curMB=((float)(progress.currentBytes))/1048576;
//
//                              mBuilder.setProgress((int)progress.totalBytes,(int) progress.currentBytes,false);
//                              notificationManagerCompat.notify(notificationID,mBuilder.build());
//                              mBuilder.setContentText(f.format(curMB)+"MB/"+f.format(totMB)+"MB");
//                              notificationManagerCompat.notify(notificationID,mBuilder.build());
//
//                            }
//                        })
//                        .start(new OnDownloadListener() {
//                            @Override
//                            public void onDownloadComplete() {
//                                Toast.makeText(Album_Song_List.this, "completed!", Toast.LENGTH_SHORT).show();
//                                mBuilder.setContentText("Download Complete");
//                                mBuilder.setProgress(0,0,false);
//                                notificationManagerCompat.notify(notificationID,mBuilder.build());
//
//                                try {
//                                    File albArt=new File(downpath+"/"+"albumArt.jpg");
//                                    if (albArt.exists())
//                                    DataHandlers.setTags2(downpath+"/"+fName,tempSJson);
//                                    else {
//                                        PRDownloader.download(albumArtUrl, downpath, "albumArt.jpg")
//                                                .build()
//                                                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
//                                                    @Override
//                                                    public void onStartOrResume() {
//
//                                                    }
//                                                })
//                                                .setOnPauseListener(new OnPauseListener() {
//                                                    @Override
//                                                    public void onPause() {
//
//                                                    }
//                                                })
//                                                .setOnCancelListener(new OnCancelListener() {
//                                                    @Override
//                                                    public void onCancel() {
//
//                                                    }
//                                                })
//                                                .setOnProgressListener(new OnProgressListener() {
//                                                    @Override
//                                                    public void onProgress(Progress progress) {
//
//                                                    }
//                                                })
//                                                .start(new OnDownloadListener() {
//                                                    @Override
//                                                    public void onDownloadComplete() {
//                                                        try {
//                                                            DataHandlers.setTags2(downpath+"/"+fName,tempSJson);
//                                                        } catch (Exception e) {
//                                                            e.printStackTrace();
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onError(Error error) {
//
//                                                    }
//                                                });}
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onError(Error error) {
//
//                            }
//                        });



            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {

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


    public class TestDownload extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences pref_main = getApplicationContext().getSharedPreferences(getResources().getString(R.string.pref_main),MODE_PRIVATE);
            SharedPreferences.Editor editor=pref_main.edit();
            int totDownloads=pref_main.getInt(getResources().getString(R.string.pref_tot_downloads),0),
                awaitingDown=pref_main.getInt(getResources().getString(R.string.pref_awaiting),0),
                nowDownloading=pref_main.getInt(getResources().getString(R.string.pref_now_downloading),0);
            ArrayList<String> idList=new ArrayList<>();
            try {
                String ids = pref_main.getString(getResources().getString(R.string.pref_id_list),ObjectSerializer.serialize(new ArrayList<>()));
                idList=(ArrayList<String>) ObjectSerializer.deserialize(ids);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String curId = idList.get(0);
            String kbps=pref_main.getString(getResources().getString(R.string.pref_KbpsFromID)+curId,"320");
            String sjsn=pref_main.getString(getResources().getString(R.string.pref_JsonFromID)+curId,null);
            if(!(sjsn.equals(null))){
                try {
                    JSONObject songJsn=new JSONObject(sjsn);
                    tempSJson=songJsn.toString();
                    folderName = DataHandlers.getAlbumName(songJsn);
                    fName=songJsn.getString("song")+".m4a";
                    notificationID=100;
                    downUrl=DataHandlers.getDownloadLink(songJsn,kbps);
                    albumArtUrl=songJsn.getString("image");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                downpath = DataHandlers.makeDir(folderName);


                if (nowDownloading==0){
                    try {
                        File albArt=new File(downpath+"/"+"albumArt.jpg");
                        if (!(albArt.exists())) {
                            PRDownloader.download(albumArtUrl, downpath, "albumArt.jpg")
                                    .build()
                                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                                        @Override
                                        public void onStartOrResume() {

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

                                        }

                                        @Override
                                        public void onError(Error error) {

                                        }
                                    });}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                PRDownloader.download(downUrl, downpath, fName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                SharedPreferences pref_main = getApplicationContext().getSharedPreferences(getResources().getString(R.string.pref_main),MODE_PRIVATE);
                                SharedPreferences.Editor editor=pref_main.edit();
                                editor.putInt(getResources().getString(R.string.pref_now_downloading),1);
                                editor.putInt(getResources().getString(R.string.pref_awaiting),awaitingDown-1);

                                ArrayList<String> songList=new ArrayList<>();
                                try {
                                    String songsL = pref_main.getString(getResources().getString(R.string.pref_song_list),ObjectSerializer.serialize(new ArrayList<>()));
                                    songList =(ArrayList<String>)ObjectSerializer.deserialize(songsL);
                                    nowDownS=songList.get(0);
                                    songList.remove(0);
//                                    Downloads_Page.songList=songList;
//                                    Downloads_Page.arrayAdapter.notifyDataSetChanged();
                                    editor.putString(getResources().getString(R.string.pref_song_list),ObjectSerializer.serialize(songList));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                editor.commit();
//                                Downloads_Page.tv_DownSong.setText(fName.replace(".m4a","_"+kbps+"kbps"));
                                mBuilder.setContentTitle(fName.replace(".m4a","_"+kbps+"kbps")).setOnlyAlertOnce(true);
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

                                totMB=((float)(progress.totalBytes))/1048576;
                                curMB=((float)(progress.currentBytes))/1048576;

                                mBuilder.setProgress((int)progress.totalBytes,(int) progress.currentBytes,false);
                                notificationManagerCompat.notify(notificationID,mBuilder.build());
                                mBuilder.setContentText(f.format(curMB)+"MB/"+f.format(totMB)+"MB");

//                                Downloads_Page.tv_prog.setText(f.format(curMB)+"MB/"+f.format(totMB)+"MB");
//                                Downloads_Page.pb_downPage.setMax((int)progress.totalBytes);
//                                Downloads_Page.pb_downPage.setProgress((int)progress.currentBytes);

                                notificationManagerCompat.notify(notificationID,mBuilder.build());

                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {

                                try {
                                    //DataHandlers.setTags2(downpath+"/"+fName,tempSJson);
                                    JSONObject songJson = new JSONObject(tempSJson);
                                    if(songJson.toString().contains("song")) {
                                        String ALBUM_N = songJson.getString("album");
                                        String YEAR = songJson.getString("year"),
                                                ARTIST = songJson.getString("primary_artists"),
                                                ALBUM_ARTISTS = songJson.getString("singers"),
                                                COMPOSER = songJson.getString("music"),
                                                DESCR = songJson.getString("starring"),
                                                COPYR = songJson.getString("copyright_text"),
                                                url_img=songJson.getString("image");

                                        File albdirectory = new File(downpath+"/"+fName);

                                        AudioFile audioFile = AudioFileIO.read(albdirectory);
                                        Mp4Tag mp4tag = (Mp4Tag) audioFile.getTag();
                                        mp4tag.setField(Mp4FieldKey.ALBUM, ALBUM_N);
                                        mp4tag.setField(Mp4FieldKey.ARTIST, ARTIST);
                                        mp4tag.setField(Mp4FieldKey.ALBUM_ARTIST, ALBUM_ARTISTS);
                                        mp4tag.setField(Mp4FieldKey.DAY, YEAR);
                                        mp4tag.setField(Mp4FieldKey.COMPOSER, COMPOSER);
                                        mp4tag.setField(Mp4FieldKey.DESCRIPTION, DESCR);
                                        mp4tag.setField(Mp4FieldKey.COPYRIGHT, COPYR);
                                        audioFile.commit();
                                        Log.i("PLSW",ALBUM_N);

                                        File img_art = new File(Environment.getExternalStorageDirectory() + "/Saavn_Downloader/" + ALBUM_N+"-"+YEAR + "/" + "albumArt.jpg");
                                        Log.i("PLSW",YEAR);

                                        Artwork artwork = ArtworkFactory.createArtworkFromFile(img_art);
                                        mp4tag.setField(artwork);

                                        RandomAccessFile imageFile = new RandomAccessFile(img_art, "r");
                                        byte[] imagedata = new byte[(int) imageFile.length()];
                                        imageFile.read(imagedata);
                                        mp4tag.createArtworkField(imagedata);

                                        audioFile.commit();


                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                SharedPreferences pref_main = getApplicationContext().getSharedPreferences(getResources().getString(R.string.pref_main),MODE_PRIVATE);
                                int totDownloads=pref_main.getInt(getResources().getString(R.string.pref_tot_downloads),0);
                                SharedPreferences.Editor editor=pref_main.edit();
                                editor.putInt(getResources().getString(R.string.pref_tot_downloads),totDownloads-1);
                                ArrayList<String> idList=new ArrayList<>();
                                try {
                                    String ids = pref_main.getString(getResources().getString(R.string.pref_id_list),ObjectSerializer.serialize(new ArrayList<>()));
                                    idList=(ArrayList<String>) ObjectSerializer.deserialize(ids);
                                    String tempID=idList.get(0);
                                    idList.remove(0);
                                    editor.remove(getResources().getString(R.string.pref_JsonFromID)+tempID);
                                    editor.remove(getResources().getString(R.string.pref_KbpsFromID)+tempID);
                                    editor.putString(getResources().getString(R.string.pref_id_list),ObjectSerializer.serialize(idList));

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                editor.putInt(getResources().getString(R.string.pref_now_downloading),0);
                                editor.commit();
                                totDownloads=totDownloads-1;

                                if (totDownloads>0){
                                    TestDownload testDownload=new TestDownload();
                                    testDownload.execute();
                                    tv_DownCount.setText(Integer.toString(totDownloads));
                                }else
                                    tv_DownCount.setText("0");
                            }

                            @Override
                            public void onError(Error error) {

                            }
                        });
                }

            }


            return null;
        }
    }



}
