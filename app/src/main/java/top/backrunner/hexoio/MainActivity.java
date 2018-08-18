package top.backrunner.hexoio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //views define
    private WebView mWebView;
    private NavigationView mNaviView;

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

        mNaviView = (NavigationView) findViewById(R.id.nav_view);
        mNaviView.setNavigationItemSelectedListener(this);

        //set WebView
        mWebView = findViewById(R.id.mWebView);

        //add js interface
        mWebView.addJavascriptInterface(new navStatus(), "navStatus");

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setUserAgentString("app/hexoio");
        mWebView.clearCache(false);
        mWebView.loadUrl("https://io.backrunner.top");

        mNaviView.setCheckedItem(R.id.nav_home);

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                currentUrl = url;

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
                if (url.contains(".html")) {
                    Intent intent = new Intent(MainActivity.this, mainText.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                //refuse to load changyan script
                if (request.getUrl().toString().contains("changyan")){
                    String noresponse = "";
                    WebResourceResponse intercepted = new WebResourceResponse("text/javascript","utf8", new ByteArrayInputStream(noresponse.getBytes()));
                    return intercepted;
                } else {
                    return super.shouldInterceptRequest(view, request);
                }
            }

        });
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
}
