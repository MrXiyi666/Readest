package fun.android.readest;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.core.app.NotificationManagerCompat;

public class App extends Application {
    public static RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    public static Context context;
    public static RelativeLayout main;
    public static TextView textView;
    public static final String ACTION_CLOSE_APP = "fun.readest.ACTION_CLOSE_APP";
    public static final String ACTION_REFRESH_WEB = "fun.readest.REFRESH_WEBVIEW";
    public static FunWebView funWebView;

    // 通知渠道ID，自定义字符串
    public static final String CHANNEL_ID = "fun_readest";
    // 通知唯一id，用来更新/取消通知
    public static final int NOTIFY_ID = 0015;

    // 开启返回监听
    public static OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // 如果H5视频全屏，优先退出全屏
            if (funWebView.customView != null) {
                funWebView.customViewCallback.onCustomViewHidden();
                return;
            }

            if (funWebView.webView != null && funWebView.webView.canGoBack()) {
                funWebView.webView.goBack(); //网页回上一页
            } else {
                assert funWebView.webView != null;
                funWebView.webView.clearHistory();
                // 已经到首页，拦截，不退出App，这里可以加Toast
                Toast.makeText(context, "已经是首页", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // 卸载残留通知
        NotificationManagerCompat.from(this).cancel(NOTIFY_ID);
        //注册广播
        registerReceiver(FunBroadcast.refreshReceiver, new IntentFilter(ACTION_REFRESH_WEB), Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(FunBroadcast.closeReceiver, new IntentFilter(ACTION_CLOSE_APP), Context.RECEIVER_NOT_EXPORTED);
    }

    public static void onDestroy(){
        Toast.makeText(context, "关闭 Readest", Toast.LENGTH_SHORT).show();
        NotificationManagerCompat.from(context).cancel(App.NOTIFY_ID);
        if(App.funWebView != null){
            App.funWebView.onDestroy();
            App.funWebView = null;
        }
        try {
            context.unregisterReceiver(FunBroadcast.refreshReceiver);
            context.unregisterReceiver(FunBroadcast.closeReceiver);
        } catch (Exception e) {
            Log.w("error", e);
        }
    }
}
