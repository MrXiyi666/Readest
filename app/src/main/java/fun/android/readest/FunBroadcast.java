package fun.android.readest;

import static fun.android.readest.App.NOTIFY_ID;
import static fun.android.readest.App.funWebView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;

public class FunBroadcast {
    public static final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(App.ACTION_REFRESH_WEB.equals(intent.getAction())){
                // 在主线程刷新webview
                if(funWebView.webView != null){
                   //Log.w("webview", "ACTION_REFRESH_WEB");
                    funWebView.onDestroy();
                    funWebView = null;
                    funWebView = new FunWebView();
                    App.main.addView(App.funWebView.webView, 0, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
                funWebView.onDestroy();
                funWebView = null;
                System.exit(0);
            }
        }
    };
    public FunBroadcast(Context context){

    }
}
