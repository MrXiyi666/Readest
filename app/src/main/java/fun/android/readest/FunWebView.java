package fun.android.readest;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;

public class FunWebView {
    private boolean isLoaded = false;
    public WebView webView;
    public View customView;
    public WebChromeClient.CustomViewCallback customViewCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable handlerRunnable = () -> {
        if(App.textView == null){
            return;
        }
        App.textView.setVisibility(View.GONE);
        isLoaded = true;
    };
    public FunWebView(){
        // 1. 创建WebView
        webView = new WebView(App.context);
        CreateWebSettings();
        webView.setBackgroundColor(Color.parseColor("#bfbfbf"));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(isLoaded){
                    return;
                }
                App.textView.setVisibility(View.VISIBLE);
                App.textView.setText("开始加载" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.requestFocus();

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
                super.onHideCustomView();
                customViewCallback.onCustomViewHidden();
                customView = null;
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                //Log.w("webview", "加载资源中" + newProgress + "%");
                view.requestFocus();
                if(isLoaded){
                    return;
                }
                if(App.textView == null){
                    return;
                }
                if (newProgress == 100) {
                    App.textView.setText("加载完成");
                    // 先取消旧任务，防止多次触发堆积
                    handler.removeCallbacks(handlerRunnable);
                    // 延迟500ms执行
                    handler.postDelayed(handlerRunnable, 500);
                    return;
                }
                App.textView.setVisibility(View.VISIBLE);
                App.textView.setText("加载资源中" + newProgress + "%");
            }
        });


        webView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                webView.loadUrl("https://web.readest.com/");
                isLoaded = false;
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                webView.stopLoading();
                webView.clearCache(true);
                // 清空历史栈（可选）
                webView.clearHistory();
                webView.destroy();
                webView = null;
            }
        });


    }

    private void CreateWebSettings(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setNeedInitialFocus(true);

// JS
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

// 文件访问（访问在线网页，关闭危险file跨域）
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

//缓存
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

//视口
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setTextZoom(100);

//存储
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

//图片
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);

//视频自动播放
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDefaultTextEncodingName("utf-8");

//混合内容
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

//多窗口
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowContentAccess(true);
        // 开启硬件加速（Activity里也要打开硬件加速）
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
    }

    public void onDestroy(){
        if (webView != null) {
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }
        }
        customView = null;
        customViewCallback = null;
    }
}
