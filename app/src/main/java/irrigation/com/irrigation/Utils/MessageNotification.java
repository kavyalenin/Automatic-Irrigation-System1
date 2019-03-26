package irrigation.com.irrigation.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.widget.RemoteViews;

import java.util.concurrent.atomic.AtomicInteger;

import irrigation.com.irrigation.Actvities.MainActivity;
import irrigation.com.irrigation.R;

public class MessageNotification {
    /**
     * The unique identifier for this type of notification.
     */
    static Context context_notification;
    private static final String NOTIFICATION_TAG = "MessageIndospace";

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification, String channelid) {
        context_notification=context;
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelid, channelid, importance);
            nm.createNotificationChannel(mChannel);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, NotificationID.getID(), notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }

    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {

        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }


    public void notify(final Context context,
                       final String title1, final String exampleString, String channelid) {
        final Resources res = context.getResources();



        final String ticker = exampleString;
        final String title = title1;
        final String text = exampleString;
        try {
            Intent intent = new Intent(context, MainActivity.class);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            Notification.Builder mNotificationBuilder = new Notification.Builder(context);
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.pushnotification_layout);

            contentView.setTextViewText(R.id.txt_title,title);
            contentView.setTextViewText(R.id.description,text);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationBuilder.setSmallIcon(R.drawable.logo)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContent(contentView)
                        .setAutoCancel(true)
                        .setGroup("indospace")
                        .setGroupSummary(true)
                        .setChannelId(channelid)
//                        .setContentText(title)
                        // Set the pending intent to be initiated when the user touches
                        // the notification.
                        .setContentIntent(contentIntent)

                        //set notification priority
                        .setPriority(Notification.PRIORITY_MAX);

            }else {
                mNotificationBuilder.setSmallIcon(R.drawable.logo)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContent(contentView)
                        .setAutoCancel(true)
                        .setGroup("Irrigation")
                        .setGroupSummary(true)
//                        .setContentText(title)
                        // Set the pending intent to be initiated when the user touches
                        // the notification.
                        .setContentIntent(contentIntent)

                        //set notification priority
                        .setPriority(Notification.PRIORITY_MAX);
            }


            notify(context, mNotificationBuilder.build(),channelid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class NotificationID {
        private static final AtomicInteger c = new AtomicInteger(0);
        public static int getID() {
            return c.incrementAndGet();
        }
    }

}