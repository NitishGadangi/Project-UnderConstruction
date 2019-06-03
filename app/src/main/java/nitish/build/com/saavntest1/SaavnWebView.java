package nitish.build.com.saavntest1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class SaavnWebView extends AppCompatActivity {
    WebView webView;
    ProgressDialog progressDialog;
    TextView tv_link;
    Button btn_Download,tv_found;
    String curUrl,songType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saavn_web_view);
        webView=findViewById(R.id.webView);
        tv_link=findViewById(R.id.tv_test_link);
        tv_found=findViewById(R.id.btn_found);
        btn_Download=findViewById(R.id.btn_downloadSongs);


        startWebView("https://www.jiosaavn.com/");

        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress) {
                progressDialog.show();
                if (progress == 100) {
                    progressDialog.dismiss();
                    curUrl=webView.getUrl();
                    tv_link.setText(curUrl);
                    songType=DataHandlers.getLinkType(curUrl);
                    if(songType.equals("FAILED")){
                        btn_Download.setVisibility(View.INVISIBLE);
                        tv_found.setText(R.string.downloads_not_founds);
                    }else {
                        btn_Download.setVisibility(View.VISIBLE);
                        tv_found.setText(R.string.downloads_found);
                    }

                } else {
                    progressDialog.show();

                }
            }
        });

        btn_Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                Intent toDownloadList = new Intent(getApplicationContext(),Album_Song_List.class);
                String typeID=DataHandlers.getDirectID(curUrl);
                toDownloadList.putExtra("TYPE_ID",typeID);
                toDownloadList.putExtra("TYPE",songType);
                toDownloadList.putExtra("PREV_ACT","WEB_ACT");
                startActivity(toDownloadList);
            }
        });
    }
    private void startWebView(String url) {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        progressDialog = new ProgressDialog(SaavnWebView.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Error:" + description, Toast.LENGTH_SHORT).show();

            }
        });
        webView.loadUrl(url);
    }

    public void wBtmSrch(View v){
        startActivity(new Intent(getApplicationContext(),Search_Songs.class));
    }
    public void wBtmBrws(View v){
//        startActivity(new Intent(getApplicationContext(),SaavnWebView.class));
    }
    public void wBtmDown(View v){
        startActivity(new Intent(getApplicationContext(),Downloads_Page.class));
    }
    public void wBtmMore(View v){
        startActivity(new Intent(getApplicationContext(),MorePage.class));
    }
}
