package nitish.build.com.saavntest1;

import android.Manifest;
import android.app.AlertDialog;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;




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

        SharedPreferences pref_main = getApplicationContext().getSharedPreferences(getResources().getString(R.string.pref_main),MODE_PRIVATE);
        SharedPreferences.Editor editor=pref_main.edit();
        editor.clear();
        editor.commit();

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
        Button btn_AVLshow=findViewById(R.id.btn_AVL_show);
        Button btn_AVlhide=findViewById(R.id.btn_AVL_Hide);


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
                resName=DataHandlers.getLinkType(finUrl);
                resID=DataHandlers.getDirectID(finUrl);
                tv_Album_ID.setText(resID);
                tv_Album_Name.setText(resName);

            }
        });

        btn_Download_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSongList = new Intent(getApplicationContext(),Album_Song_List.class);
                toSongList.putExtra("TYPE_ID",resID);
                toSongList.putExtra("TYPE",resName);
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

        AVLoadingIndicatorView avi=findViewById(R.id.AVLloader);
        avi.hide();

        btn_AVLshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        btn_AVlhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    DataHandlers.setTags2();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("STAG",e.toString());
                }
            }
        });





    }

    public void btmSrch(View v){
        startActivity(new Intent(getApplicationContext(),Search_Songs.class));
    }
    public void btmBrws(View v){
        startActivity(new Intent(getApplicationContext(),SaavnWebView.class));
    }
    public void btmDown(View v){
        startActivity(new Intent(getApplicationContext(),Downloads_Page.class));
    }
    public void btmMore(View v){
        startActivity(new Intent(getApplicationContext(),MorePage.class));
    }



}
