package nitish.build.com.saavntest1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    String finUrl,resName,resID,str_ptrn,str_NamePtrn,chanelId="test_chnl";
    TextView tv_Album_ID,tv_Album_Name;
    EditText et_url;
    int notificationID = 100;

//    String finString="failed";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----------Permission part------------------//
        String permission1 = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String permission2 = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        if (getApplicationContext().checkCallingOrSelfPermission(permission1)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        }
        if (getApplicationContext().checkCallingOrSelfPermission(permission2)== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
        //---------------Done Permission--------------//

        finUrl="https://www.jiosaavn.com/album/luka-chuppi/IBCxeZ5VVNU_";
        tv_Album_ID=findViewById(R.id.txv_albumID);
        tv_Album_Name = findViewById(R.id.txv_AlbumName);
        et_url=findViewById(R.id.edt_url);
        Button btn_Search = findViewById(R.id.btn_search1);
        Button btn_Download_list = findViewById(R.id.btn_songlist);
        Button btn_testSearch=findViewById(R.id.btn_testNotif);
        Button btn_testWeb=findViewById(R.id.btn_test2);

        btn_testSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSearch= new Intent(getApplicationContext(),Search_Songs.class);
                startActivity(toSearch);
            }
        });

        resName="Failed";
        resID="Failed";
//        str_ptrn = "\\[\'albumid\',\'"+"(.*?)"+"\'\\]";
//        str_NamePtrn = "\'#"+"(.*?)"+"\'";

        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finUrl = et_url.getText().toString();
                resID=DataHandlers.getAlbumID(finUrl);

                tv_Album_ID.setText(resID);
                tv_Album_Name.setText(resName);

            }
        });

        btn_Download_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSongList = new Intent(getApplicationContext(),Album_Song_List.class);
                toSongList.putExtra("ALBUM_ID",resID);
                startActivity(toSongList);
            }
        });

        btn_testWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSaavnWeb = new Intent(getApplicationContext(),SaavnWebView.class);
                startActivity(toSaavnWeb);
            }
        });





    }



}
