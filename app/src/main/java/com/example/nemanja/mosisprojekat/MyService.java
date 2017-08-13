package com.example.nemanja.mosisprojekat;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

public class MyService extends Service {

    private int mNotificationId = 001;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_menu_camera)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        mBuilder.setVibrate(new long[] {125,75,125,275,200,275,125,75,125,275,200,600,200,600});

        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        mNotificationId++;

        for(int i=0;i<5;i++){
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
            mNotificationId++;
        }
    }
}
