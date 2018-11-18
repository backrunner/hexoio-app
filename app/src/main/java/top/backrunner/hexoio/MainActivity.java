package top.backrunner.hexoio;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.util.Log;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    //views define
    private WebView mWebView;
    private NavigationView mNaviView;
    private SwipeRefreshLayout mRefresh;

    //preferences
    private SharedPreferences versionPreferences;
    private int historyVersionCode = BuildConfig.VERSION_CODE;

    public static Context mContext;

    //set up handler
    private static Handler navHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //version check
        try{
            versionPreferences = getSharedPreferences("versionPreferences",0);
            historyVersionCode = versionPreferences.getInt("versionCode",0);
        } catch (Exception e){
            e.printStackTrace();
        }
        if (historyVersionCode < BuildConfig.VERSION_CODE){
            //update checked
            DataCacheManager.clearAllCache(getApplicationContext());
            try {
                SharedPreferences.Editor editor = versionPreferences.edit();
                editor.putInt("versionCode", BuildConfig.VERSION_CODE);
                editor.commit();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //set context
        mContext = this;

        //set refresh layout
        mRefresh = (SwipeRefreshLayout)findViewById(R.id.main_swipeRefresh);

        mNaviView = (NavigationView) findViewById(R.id.nav_view);
        mNaviView.setNavigationItemSelectedListener(this);

        //set WebView
        mWebView = findViewById(R.id.mWebView);

        mRefresh.setRefreshing(true);

        //add js interface
        mWebView.addJavascriptInterface(new navStatus(), "navStatus");

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setUserAgentString("app/hexoio");
        mWebView.clearCache(false);
        mWebView.loadUrl("https://io.backrunner.top");

        mNaviView.setCheckedItem(R.id.nav_home);

        //set refresh event
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mRefresh.canChildScrollUp()) {
                    mWebView.reload();
                    mRefresh.setRefreshing(true);
                }
            }
        });

        mRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback(){
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
                return mWebView.getScrollY()>0;
            }
        });

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                currentUrl = url;
                mRefresh.setRefreshing(true);

                if (currentUrl.contains("categories")) {
                    mNaviView.setCheckedItem(R.id.nav_categories);
                    currentSelectedNavItem = R.id.nav_categories;
                } else if(currentUrl.contains("tags")){
                    mNaviView.setCheckedItem(R.id.nav_tag);
                    currentSelectedNavItem = R.id.nav_tag;
                } else {
                    mNaviView.setCheckedItem(R.id.nav_home);
                    currentSelectedNavItem = R.id.nav_home;
                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mWebView.scrollTo(0,0);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                //外链检测
                if (!url.contains("io.backrunner.top")) {
                    Uri externalUri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, externalUri);
                    startActivity(intent);
                    return true;
                } else {
                    if (url.contains(".html")) {
                        Intent intent = new Intent(MainActivity.this, mainText.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        return true;
                    } else {
                        return super.shouldOverrideUrlLoading(view, request);
                    }
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100){
                    mRefresh.setRefreshing(false);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        //set update
        CheckUpdateTask checkUpdateTask = new CheckUpdateTask();
        checkUpdateTask.execute();
    }

    @Override
    protected void onDestroy() {
        navHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private String currentUrl = "";
    private int currentSelectedNavItem;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Log.d("Current URL",currentUrl);
            Log.d("Bool",Boolean.toString(currentUrl == "https://io.backrunner.top/" || currentUrl == "https://io.backrunner.top"));
            Log.d("NavItem",Integer.toString(currentSelectedNavItem));
            //Drawer is off
            if (currentSelectedNavItem == R.id.nav_search){
                mWebView.loadUrl("javascript:onPopupClose();");
                if (currentUrl.contains("categories")) {
                    mNaviView.setCheckedItem(R.id.nav_categories);
                    currentSelectedNavItem = R.id.nav_categories;
                } else if(currentUrl.contains("tags")){
                    mNaviView.setCheckedItem(R.id.nav_tag);
                    currentSelectedNavItem = R.id.nav_tag;
                } else {
                    mNaviView.setCheckedItem(R.id.nav_home);
                    currentSelectedNavItem = R.id.nav_home;
                }
            } else {
                if (currentUrl.equals("https://io.backrunner.top/") || currentUrl.equals("https://io.backrunner.top")) {
                    Log.d("Recycle","Back Pressed");
                    navHandler.removeCallbacksAndMessages(null);
                    super.onBackPressed();
                } else {
                    mWebView.goBack();
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        currentSelectedNavItem = id;
        if (id == R.id.nav_home) {
            mWebView.loadUrl("https://io.backrunner.top");
        } else if (id == R.id.nav_tag) {
            mWebView.loadUrl("https://io.backrunner.top/tags");
        } else if (id == R.id.nav_categories) {
            mWebView.loadUrl("https://io.backrunner.top/categories");
        } else if (id == R.id.nav_search) {
            mWebView.loadUrl("javascript:searchTrigger();");
        } else if (id == R.id.nav_settings) {
            Intent settingIntent = new Intent(MainActivity.this,setting.class);
            startActivity(settingIntent);
        } else if (id == R.id.nav_exit) {
            AlertDialog ad = new AlertDialog.Builder(this).setTitle("确认退出吗？").setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    }).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public final class navStatus{
        @JavascriptInterface
        public void restoreNavStatus(){
            navHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (currentUrl.contains("categories")) {
                        mNaviView.setCheckedItem(R.id.nav_categories);
                        currentSelectedNavItem = R.id.nav_categories;
                    } else if(currentUrl.contains("tags")){
                        mNaviView.setCheckedItem(R.id.nav_tag);
                        currentSelectedNavItem = R.id.nav_tag;
                    } else {
                        mNaviView.setCheckedItem(R.id.nav_home);
                        currentSelectedNavItem = R.id.nav_home;
                    }
                }
            });
        }
    }

    //about update
    private class CheckUpdateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            return UpdateUtil.readJSON(UpdateUtil.updateUrl+"ver.json");
        }

        @Override
        protected void onPostExecute(String res) {
            try{
                JSONObject jsonObject = new JSONObject(res);
                //get update info
                int versionCode = jsonObject.getInt("versionCode");
                if (versionCode>BuildConfig.VERSION_CODE) {
                    String versionName = jsonObject.getString("versionName");
                    String updateLog = jsonObject.getString("updateLog");
                    final String apkUrl = UpdateUtil.updateUrl+"apk/brnote_"+versionName+".apk";
                    //set alert dialog
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.mContext);
                    alertDialogBuilder.setTitle("更新");
                    alertDialogBuilder.setMessage("检测到新版本："+versionName+"\n\n"+"更新内容：\n"+updateLog);
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setPositiveButton("下载",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //start browser download new apk
                            Uri apkUri = Uri.parse(apkUrl);
                            Intent intent =  new Intent(Intent.ACTION_VIEW, apkUri);
                            startActivity(intent);
                        }
                    });
                    alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });
                    alertDialogBuilder.create().show();
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
