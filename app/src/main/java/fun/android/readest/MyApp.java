package fun.android.readest;

import android.app.Application;
import android.webkit.WebView;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 预初始化WebView
        WebView.setWebContentsDebuggingEnabled(true);
    }
}
