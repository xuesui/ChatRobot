package com.example.chatrobot;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String URL_KEY = "http://www.tuling123.com/openapi/api";
    private static final String API_key = "98e8a48049784263bcf27c7020bce824";
    private List<Msg> msgList = new ArrayList<>();
    private EditText editText;
    private Button send;
    private RecyclerView recyclerView;
    private MsgAdapter msgAdapter;
    private DrawerLayout drawerLayout;
    private static int IMAGE_REQUEST_CODE = 2;
    private static int USERS_IMAGE = 1;
    private PopupWindow mPopWindow;
    private String paths;
    private CircleImageView head;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //drawerlayout的设置
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_setting);
        }

        navigationView.setCheckedItem(R.id.nav_call);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_call:
                        Toast.makeText(MainActivity.this, "暂时无逻辑", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_friends:
                        Toast.makeText(MainActivity.this, "暂时无逻辑", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_location:
                        Toast.makeText(MainActivity.this, "暂时无逻辑", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_mail:
                        Toast.makeText(MainActivity.this, "暂时无逻辑", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.settings:
                        showPopupWindow();
                }
                return true;
            }
        });


        //更换头像
        View headView = navigationView.inflateHeaderView(R.layout.nav_header);
        head = (CircleImageView) headView.findViewById(R.id.icon_image);
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

    //相册选取更换头像
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                paths = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bitmap = BitmapFactory.decodeFile(paths);
                head.setImageBitmap(bitmap);
                CircleImageView user = (CircleImageView) findViewById(R.id.user_image);
                user.setImageBitmap(bitmap);
                Toast.makeText(this, "聊天框换头像有bug，未完善", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == USERS_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            paths = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap1 = BitmapFactory.decodeFile(paths);
            CircleImageView robot = (CircleImageView) findViewById(R.id.robot_image);
            robot.setImageBitmap(bitmap1);
            Toast.makeText(this, "此操作目前有bug，还未完善", Toast.LENGTH_SHORT).show();
        }
    }


    //popupwindow的显示和点击事件
    private void showPopupWindow() {
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popuplayout, null);
        mPopWindow = new PopupWindow(contentView,
                DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);
        //设置各个控件的点击响应
        TextView tv1 = (TextView) contentView.findViewById(R.id.pop_clear);
        TextView tv2 = (TextView) contentView.findViewById(R.id.pop_cancel);
        TextView tv3 = (TextView) contentView.findViewById(R.id.change_robot);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        //显示PopupWindow
        View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
        mPopWindow.showAtLocation(rootview, Gravity.HORIZONTAL_GRAVITY_MASK, 0, 0);
    }

    //popuplayout点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_clear:
                msgList.clear();
                msgAdapter = new MsgAdapter(msgList);
                msgAdapter.notifyDataSetChanged();
                msgAdapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
                mPopWindow.dismiss();
                break;
            case R.id.pop_cancel:
                mPopWindow.dismiss();
                break;
            case R.id.change_robot:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, USERS_IMAGE);
                mPopWindow.dismiss();
                break;
            default:
                break;
        }
    }

    //drawwerlayout的显示点击事件
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
