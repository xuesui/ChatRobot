package com.example.chatrobot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String URL_KEY = "http://www.tuling123.com/openapi/api";
    private static final String API_key = "98e8a48049784263bcf27c7020bce824";
    private List<Msg> msgList = new ArrayList<>();
    private EditText editText;
    private Button send;
    private RecyclerView recyclerView;
    private MsgAdapter msgAdapter;
    private DrawerLayout drawerLayout;
    private static int IMAGE_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //drawerlayout的设置
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_setting);
        }

        navigationView.setCheckedItem(R.id.nav_call);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                Toast.makeText(MainActivity.this, "暂时无逻辑", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        //更换头像
        View headView=navigationView.inflateHeaderView(R.layout.nav_header);
        CircleImageView head=(CircleImageView)headView.findViewById(R.id.icon_image);
        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });
        //界面初始化和更新
        editText = (EditText) findViewById(R.id.input_edit);
        send = (Button) findViewById(R.id.send_button);
        recyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        msgAdapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(msgAdapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = editText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    msgAdapter = new MsgAdapter(msgList);
                    msgAdapter.notifyItemInserted(msgList.size() - 1);
                    recyclerView.scrollToPosition(msgList.size() - 1);
                    editText.setText("");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url = URL_KEY + "?key=" + API_key + "&info=" + content;
                                Log.d("code", "run: " + content);
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(url)
                                        .build();
                                Response response = client.newCall(request).execute();
                                String back = response.body().string();
                                JSONObject jsonObject = new JSONObject(back);
                                msgList.add(new Msg(jsonObject.getString("text"), Msg.TYPE_RECEIVED));
                                msgAdapter = new MsgAdapter(msgList);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msgAdapter.notifyItemInserted(msgList.size() - 1);
                                        recyclerView.scrollToPosition(msgList.size() - 1);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

}
