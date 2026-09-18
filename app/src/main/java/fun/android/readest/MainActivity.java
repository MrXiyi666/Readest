package fun.android.readest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.context= this;
        // ========== 全屏核心代码（写在setContentView之前！） ==========
        Window window = getWindow();
        // 隐藏状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏底部导航栏（可选，想要彻底全屏加上）
        int uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        window.getDecorView().setSystemUiVisibility(uiFlags);
        // 开启
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        getOnBackPressedDispatcher().addCallback(this, App.callback);
        NotificationManagerCompat.from(this).cancel(App.NOTIFY_ID);
        //注册广播
        registerReceiver(FunBroadcast.refreshReceiver, new IntentFilter(App.ACTION_REFRESH_WEB), Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(FunBroadcast.closeReceiver, new IntentFilter(App.ACTION_CLOSE_APP), Context.RECEIVER_NOT_EXPORTED);

        App.funWebView = new FunWebView();
        App.main = findViewById(R.id.main);
        App.textView = findViewById(R.id.textView);

        App.main.post(new Runnable() {
            @Override
            public void run() {
                App.main.addView(App.funWebView.webView, 0, App.layoutParams);
            }
        });



        // Android13+ 通知权限请求
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 没有权限：弹窗申请，不要在这里启动服务，等待回调
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){ // 和上面的100对应
            if (grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MainActivity.this, MenuService.class);
                        startForegroundService(intent);
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, MenuService.class);
                    startForegroundService(intent);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.w("webview", "onPause");
        // 页面切后台，就取消常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (App.funWebView.webView == null) return;
        App.funWebView.webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, MenuService.class);
                startForegroundService(intent);
            }
        } else {
            Intent intent = new Intent(MainActivity.this, MenuService.class);
            startForegroundService(intent);
        }

        // 页面回到前台，开启常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (App.funWebView.webView == null) return;

        String urlNow = App.funWebView.webView.getUrl();
        // 已经加载目标站点，直接return；null代表还没加载页面
        //如果 urlNow 不为空，并且 urlNow 是以 `https://web.readest.com/` 开头的网址
        if (urlNow != null && urlNow.startsWith("https://web.readest.com/")) {
            App.funWebView.webView.onResume();
            return;
        }
        App.funWebView.webView.loadUrl("https://web.readest.com/");
    }

    @Override
    protected void onDestroy() {
        App.onDestroy();
        super.onDestroy();
    }


}