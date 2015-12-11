package mvp_master.demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.okhttp.Request;

import java.io.IOException;

import mvp_master.okhttp.CacheType;
import mvp_master.okhttp.JsonCallBack;
import mvp_master.okhttp.OKHttpUtils;

public class MainActivity extends AppCompatActivity {

    private OKHttpUtils okHttpUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        okHttpUtils = new OKHttpUtils.Builder(this).build();
        initView();
    }

    private void initView() {
        findViewById(R.id.tv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(CacheType.ONLY_NETWORK);
            }
        });

        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(CacheType.ONLY_CACHED);
            }
        });

        findViewById(R.id.tv3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(CacheType.NETWORK_ELSE_CACHED);
            }
        });


        findViewById(R.id.tv4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(CacheType.CACHED_ELSE_NETWORK);
            }
        });

        findViewById(R.id.tv5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void getData(final CacheType cacheType){
        okHttpUtils.get("http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json", cacheType, null, new JsonCallBack<DateModule>() {
            @Override
            public void onFailure(Request request, Exception e) {

            }

            @Override
            public void onResponse(final DateModule object) throws IOException {
                Log.e("lcc",object.getResult().getDatetime_2());
            }
        });
    }

}
