package fun.android.readest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    // 自定义广播Action，用来关闭程序
    public static final String ACTION_CLOSE_APP = "fun.readest.ACTION_CLOSE_APP";
    public static final String ACTION_REFRESH_WEB = "fun.readest.REFRESH_WEBVIEW";
    // 广播接收器，收到消息刷新webview
    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_REFRESH_WEB.equals(intent.getAction())){
                // 在主线程刷新webview
                if(webView != null){
                    webView.reload();
                    webView.clearCache(false);
                    Toast.makeText(context, "已刷新 WebView", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "刷新 WebView 失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_CLOSE_APP.equals(intent.getAction())){
                // 关闭MainActivity
                MainActivity.this.finish();
                Toast.makeText(context, "关闭 Readest", Toast.LENGTH_SHORT).show();
                //System.exit(0);
            }
        }
    };


    // 通知渠道ID，自定义字符串
    private static final String CHANNEL_ID = "fun_readest";
    // 通知唯一id，用来更新/取消通知
    private static final int NOTIFY_ID = 0015;
    private WebView webView;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private TextView view_loading;
    private String text = "加载资源中";
    private final Handler handler = new Handler(Looper.getMainLooper());
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ====== 全屏代码 放在setContentView之前！======
        // 隐藏状态栏、导航栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // 卸载残留通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(NOTIFY_ID);

        //注册广播
        registerReceiver(refreshReceiver, new IntentFilter(ACTION_REFRESH_WEB), Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(closeReceiver, new IntentFilter(ACTION_CLOSE_APP), Context.RECEIVER_NOT_EXPORTED);

        // 开启返回监听
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 如果H5视频全屏，优先退出全屏
                if (customView != null) {
                    customViewCallback.onCustomViewHidden();
                    return;
                }

                if (webView != null && webView.canGoBack()) {
                    webView.goBack(); //网页回上一页
                } else {
                    // 已经到首页，拦截，不退出App，这里可以加Toast
                    Toast.makeText(MainActivity.this, "已经是首页", Toast.LENGTH_SHORT).show();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        webView = findViewById(R.id.webview);
        view_loading = findViewById(R.id.view_loading);
        webView.setBackgroundColor(Color.BLACK);

        webView.setWebViewClient(new WebViewClient() {
            // 调试临时放行SSL证书，上线务必删除！
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
            // 开始加载网页 → 显示加载文字
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                handler.removeCallbacksAndMessages(null);
                view_loading.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view_loading.setText("加载完毕");
                handler.postDelayed(() -> view_loading.setVisibility(View.GONE),1000);
                view.requestFocus();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // false：交给当前webview自己加载，不跳转外部浏览器
                return false;
            }
            // 捕获加载错误，方便日志排查
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e("WEB_ERR", "加载错误：" + error.getDescription());
                super.onReceivedError(view, request, error);
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                customView = view;
                customViewCallback = callback;
            }

            @Override
            public void onHideCustomView() {
                customViewCallback.onCustomViewHidden();
                customView = null;
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                // newProgress 0~100
                text = "加载资源中" + "\n" + newProgress + "%";
                view_loading.setText(text);
            }
            // JS弹窗不拦截，放行
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }
        });

        WebSettings webSettings = webView.getSettings();
        //自动获取焦点
        webSettings.setNeedInitialFocus(true);

        //1.JS
        webSettings.setJavaScriptEnabled(true); //可以运行JS
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //：JS 代码可以直接调用`window.open()`打开新页面，**不需要用户点击手势触发**

        //2.文件访问
        webSettings.setAllowFileAccess(true); //允许读取本地
        webSettings.setAllowFileAccessFromFileURLs(true); //可以访问本地文件
        webSettings.setAllowUniversalAccessFromFileURLs(true); //本地 html 文件 (file://) 能不能访问任意域名的网络资源

        //3.缓存
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        //4.视口适配
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        //5.文本缩放
        webSettings.setTextZoom(100);

        //6.【重要！】DOM存储，百度等网站必须开启，否则白屏
        webSettings.setDomStorageEnabled(true);  //开启 H5 的 localStorage /sessionStorage
        webSettings.setDatabaseEnabled(true);    //开启 WebSQL（旧版 H5 数据库）

        //7.图片
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);

        //8.媒体自动播放
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        //9.编码
        webSettings.setDefaultTextEncodingName("utf-8");

        //11.混合内容【重点！加版本判断】
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        //12.多窗口
        webSettings.setSupportMultipleWindows(true);
//允许 WebView 读取`content://`系统内容提供者（相册、媒体等）。
        webSettings.setAllowContentAccess(true);


        // 创建通知渠道（只需要创建一次）
        createNotificationChannel();
        // Android13 请求通知权限
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }else{
            showNotification();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){ // 和上面的100对应
            if (grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                // 用户同意通知权限，可以发通知
                showNotification();
            }else{
                // 用户拒绝通知权限，无法弹出通知
            }
        }
    }

    // 创建通知渠道 API26+ 必须
    private void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Menu", importance);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    private void showNotification() {
        // 【折叠状态布局】
        RemoteViews remoteViewsSmall = new RemoteViews(getPackageName(), R.layout.notification_layout);
        // 【展开状态布局】和上面用同一个布局，下拉后默认展开这个
        RemoteViews remoteViewsBig = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // 按钮点击事件：发送广播刷新WebView
        Intent refreshIntent = new Intent(ACTION_REFRESH_WEB);
        refreshIntent.setPackage(getPackageName()); // 新增
        PendingIntent btnPendingIntent = PendingIntent.getBroadcast(this, 200, refreshIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.refresh_button, btnPendingIntent);
        remoteViewsSmall.setOnClickPendingIntent(R.id.refresh_button, btnPendingIntent);

        // ========== 关闭按钮 代码 ==========
        Intent closeIntent = new Intent(ACTION_CLOSE_APP);
        closeIntent.setPackage(getPackageName()); // 新增
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 201, closeIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.close_button, closePendingIntent);
        remoteViewsSmall.setOnClickPendingIntent(R.id.close_button, closePendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle()) //自定义布局必须加这个样式
                .setCustomContentView(remoteViewsSmall)      //收起时布局
                .setCustomBigContentView(remoteViewsBig);    //展开时布局（下拉默认显示这个）
               // .setOngoing(true);  // ✅ 常驻通知，禁止滑动删除

        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFY_ID, notification);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView == null) return;

        String urlNow = webView.getUrl();
        // 已经加载目标站点，直接return；null代表还没加载页面
        if (urlNow != null && urlNow.startsWith("https://web.readest.com/")) {
            return;
        }
        webView.loadUrl("https://web.readest.com/");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(refreshReceiver);
            unregisterReceiver(closeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // 释放WebView
        if (webView != null) {
            webView.clearCache(false);
            webView.stopLoading();          // 停止加载网页
            webView.getSettings().setJavaScriptEnabled(false);
            webView.removeAllViews();       // 移除webview子视图
            webView.destroy();              // 销毁WebView内核
        }

        // 移除通知：清除指定NOTIFY_ID的通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(NOTIFY_ID);
    }


}