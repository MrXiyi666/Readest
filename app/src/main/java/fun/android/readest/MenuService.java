package fun.android.readest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class MenuService extends Service {
    public MenuService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                stopSelf();
                return;
            }
        }
        createNotificationChannel();
        showNotification();

    }

    // 复用你原来地创建渠道方法
    public void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(App.CHANNEL_ID, "Menu", importance);

        // 关闭声音
        channel.setSound(null, null);
        channel.setVibrationPattern(null);
        channel.enableVibration(false); // 新增：关闭震动
        channel.setShowBadge(false);    // 关闭桌面角标
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        // 删除旧渠道，清除缓存配置
        notificationManager.deleteNotificationChannel(App.CHANNEL_ID);
        notificationManager.createNotificationChannel(channel);
    }


    public void showNotification() {
        RemoteViews remoteViewsSmall = new RemoteViews(this.getPackageName(), R.layout.notification_small_layout);
        // 【展开状态布局】和上面用同一个布局，下拉后默认展开这个
        RemoteViews remoteViewsBig = new RemoteViews(this.getPackageName(), R.layout.notification_big_layout);
        // 按钮点击事件：发送广播刷新WebView
        Intent refreshIntent = new Intent(App.ACTION_REFRESH_WEB);
        refreshIntent.setPackage(this.getPackageName()); // 新增
        PendingIntent btnPendingIntent = PendingIntent.getBroadcast(this, 200, refreshIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.refresh_button, btnPendingIntent);

        // ========== 关闭按钮 代码 ==========
        Intent closeIntent = new Intent(App.ACTION_CLOSE_APP);
        closeIntent.setPackage(this.getPackageName()); // 新增
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 201, closeIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.close_button, closePendingIntent);
        remoteViewsSmall.setOnClickPendingIntent(R.id.close_button, closePendingIntent);

        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, 200, openIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.open_button, openPendingIntent);
        remoteViewsSmall.setOnClickPendingIntent(R.id.open_button, openPendingIntent);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.kong)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsSmall)
                .setCustomBigContentView(remoteViewsBig)
                .setOnlyAlertOnce(true);
        Notification notification = builder.build();
        startForeground(App.NOTIFY_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        showNotification();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        stopForeground(true); // 先移除前台通知
        super.onDestroy();    // super放最后！
    }

}