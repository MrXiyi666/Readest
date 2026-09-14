package fun.android.readest;

import static fun.android.readest.App.NOTIFY_ID;
import static fun.android.readest.App.funWebView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;

public class FunBroadcast {
    public static final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(App.ACTION_REFRESH_WEB.equals(intent.getAction())){
                // 在主线程刷新webview
                if(funWebView.webView != null){
                    // 1.停止正在加载的网页
                    funWebView.webView.stopLoading();
                    // 3.清空缓存，false=内存缓存，true=磁盘+内存一起清
                    funWebView.webView.clearCache(false);
                    funWebView.webView.reload();
                    Toast.makeText(context, "已刷新 WebView", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "刷新 WebView 失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public static final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(App.ACTION_CLOSE_APP.equals(intent.getAction())){
                // 关闭MainActivity
                //MainActivity.this.finish();
                Toast.makeText(context, "关闭 Readest", Toast.LENGTH_SHORT).show();
                NotificationManagerCompat.from(context).cancel(NOTIFY_ID);
                System.exit(0);
            }
        }
    };
    public FunBroadcast(Context context){

    }
}
