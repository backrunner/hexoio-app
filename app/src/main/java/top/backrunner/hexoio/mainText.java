package top.backrunner.hexoio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

public class mainText extends AppCompatActivity{

    private WebView textWebview;
    private SwipeRefreshLayout mRefresh;

    private String originUrl;
    private String currentUrl;

    private String currentTitle;
    private String currentDesc = "这是一条来自BackRunner\'s Note的分享。";

    //set up handler
    private static Handler descHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //浏览器唤起
        Intent awakeIntent = getIntent();
        String awakeAction = awakeIntent.getAction();
        if (Intent.ACTION_VIEW.equals(awakeAction)){
            Uri uri = awakeIntent.getData();
            if (uri != null){
                originUrl = uri.toString();
            }
        } else {
            originUrl = awakeIntent.getExtras().getString("url");
        }

        //set actionbar
        setContentView(R.layout.activity_main_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.textToolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //set webview
        textWebview = (WebView) findViewById(R.id.textWebview);

        //set refresh layout
        mRefresh = (SwipeRefreshLayout)findViewById(R.id.text_swipeRefreshLayout);

        mRefresh.setRefreshing(true);

        textWebview.addJavascriptInterface(new postDesc(),"postDesc");

        WebSettings webSettings = textWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setUserAgentString("app/hexoio");
        textWebview.clearCache(false);

        textWebview.loadUrl(originUrl);


        //set refresh event
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mRefresh.canChildScrollUp()) {
                    textWebview.reload();
                    mRefresh.setRefreshing(true);
                }
            }
        });

        mRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback(){
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
                return textWebview.getScrollY()>0;
            }
        });

        textWebview.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().contains("changyan")){
                    String noresponse = "";
                    WebResourceResponse intercepted = new WebResourceResponse("text/javascript","utf8", new ByteArrayInputStream(noresponse.getBytes()));
                    return intercepted;
                } else {
                    return super.shouldInterceptRequest(view, request);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                currentUrl = url;
                mRefresh.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //always on the top
                textWebview.scrollTo(0,0);
                super.onPageFinished(view, url);
                //get title
                currentTitle = view.getTitle();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                //外链检测
                if (!url.contains("io.backrunner.top")) {
                    Uri externalUri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW,externalUri);
                    startActivity(intent);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }
        });

        textWebview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100){
                    mRefresh.setRefreshing(false);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                currentTitle = title;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!currentUrl.equals(originUrl)){
            textWebview.goBack();
        } else {
            descHandler.removeCallbacksAndMessages(null);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        descHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    //set menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_text_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_share:
                String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                //set permission
                try {
                    //检测是否有写的权限
                    int permission = ActivityCompat.checkSelfPermission(this,
                            "android.permission.WRITE_EXTERNAL_STORAGE");
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // 没有写的权限，去申请写的权限，会弹出对话框
                        ActivityCompat.requestPermissions(this,mPermissionList,123);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                UMImage thumb = new UMImage(mainText.this,R.mipmap.ic_note_avatar_f);
                //set share element
                UMWeb web = new UMWeb(currentUrl);
                web.setTitle(currentTitle);//标题
                web.setThumb(thumb);  //缩略图
                web.setDescription(currentDesc);//描述
                new ShareAction(mainText.this).withMedia(web)
                        .setDisplayList(SHARE_MEDIA.QQ,SHARE_MEDIA.QZONE,SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.WEIXIN_FAVORITE,SHARE_MEDIA.SMS,SHARE_MEDIA.EMAIL)
                        .open();
                return true;
            case android.R.id.home:
                this.finish(); // back button
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    //set desc
    public final class postDesc{
        @JavascriptInterface
        public void post(final String desc){
            descHandler.post(new Runnable() {
                @Override
                public void run() {
                    currentDesc = desc;
                }
            });
        }
    }
}
