package top.backrunner.hexoio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class setting extends AppCompatActivity {

    RecyclerView recyclerView;
    settingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settingToolbar);
        setSupportActionBar(toolbar);

        //set layout
        recyclerView = (RecyclerView)findViewById(R.id.settingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new settingAdapter(this);
        adapter.setOnItemClickListener(new settingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position){
                    case 0:
                        Intent updateSettingIntent = new Intent(setting.this,updateSetting.class);
                        startActivity(updateSettingIntent);
                        break;
                    case 1:
                        Intent cacheManageIntent = new Intent(setting.this,cacheManage.class);
                        startActivity(cacheManageIntent);
                        break;
                    case 2:
                        Intent aboutIntent = new Intent(setting.this,aboutActivity.class);
                        startActivity(aboutIntent);
                        break;
                }
            }
        });
        setRecyclerViewValues();

        //set actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setRecyclerViewValues(){
        ArrayList<String> names = new ArrayList<>();
        names.add("自动更新");
        names.add("缓存清理");
        names.add("关于");
        //set adapter
        adapter.setContents(names);
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
