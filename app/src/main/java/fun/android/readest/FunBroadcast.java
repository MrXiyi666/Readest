package fun.android.readest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class FunBroadcast {
    public static final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(App.ACTION_REFRESH_WEB.equals(intent.getAction())){
                if(App.funWebView != null){
                    App.funWebView.onDestroy();
                    App.funWebView = null;
                }
                App.funWebView = new FunWebView();
                App.main.addView(App.funWebView.webView, 0, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
    };

    public static final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(App.ACTION_CLOSE_APP.equals(intent.getAction())){
                App.onDestroy();
                System.exit(0);
            }
        }
    };

}