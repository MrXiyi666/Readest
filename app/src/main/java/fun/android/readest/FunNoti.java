package fun.android.readest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class FunNoti {
    // 创建通知渠道 API26+ 必须
    public static void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(App.CHANNEL_ID, "Menu", importance);
        NotificationManager notificationManager = App.context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void showNotification() {
        RemoteViews remoteViewsSmall = new RemoteViews(App.context.getPackageName(), R.layout.notification_small_layout);
        // 【展开状态布局】和上面用同一个布局，下拉后默认展开这个
        RemoteViews remoteViewsBig = new RemoteViews(App.context.getPackageName(), R.layout.notification_big_layout);
        // 按钮点击事件：发送广播刷新WebView
        Intent refreshIntent = new Intent(App.ACTION_REFRESH_WEB);
        refreshIntent.setPackage(App.context.getPackageName()); // 新增
        PendingIntent btnPendingIntent = PendingIntent.getBroadcast(App.context, 200, refreshIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.refresh_button, btnPendingIntent);
        remoteViewsSmall.setOnClickPendingIntent(R.id.refresh_button, btnPendingIntent);

        // ========== 关闭按钮 代码 ==========
        Intent closeIntent = new Intent(App.ACTION_CLOSE_APP);
        closeIntent.setPackage(App.context.getPackageName()); // 新增
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(App.context, 201, closeIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsBig.setOnClickPendingIntent(R.id.close_button, closePendingIntent);
        remoteViewsSmall.setOnClickPendingIntent(R.id.close_button, closePendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.context, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.kong)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsSmall)
                .setCustomBigContentView(remoteViewsBig);


        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.context);

        if (ActivityCompat.checkSelfPermission(App.context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(App.NOTIFY_ID, notification);
    }
}
