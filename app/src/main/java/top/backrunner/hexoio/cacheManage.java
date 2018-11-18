package top.backrunner.hexoio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class cacheManage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private simpleTextAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView)findViewById(R.id.cacheManageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new simpleTextAdapter(this);
        adapter.setOnItemClickListener(new simpleTextAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position){
                    case 1:
                        try {
                            DataCacheManager.clearAllCache(getApplicationContext());
                            Toast.makeText(cacheManage.this,"缓存清理成功" ,Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable(){
                                public void run() {
                                    //要执行的任务
                                    setRecyclerViewValues();
                                }
                            }, 500);
                        } catch (Exception e){
                            Toast.makeText(cacheManage.this,"缓存清理失败" ,Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

        setRecyclerViewValues();
    }

    private void setRecyclerViewValues(){
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> descs = new ArrayList<>();
        titles.add("当前缓存");
        try {
            descs.add(DataCacheManager.getTotalCacheSize(getApplicationContext()));
        } catch (Exception e) {
            descs.add("缓存获取错误");
            e.printStackTrace();
        }
        titles.add("缓存清理");
        descs.add("该操作将清理掉程序现有的所有缓存");
        //set adapter
        adapter.setContents(titles,descs);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
