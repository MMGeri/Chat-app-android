package com.geri.chat.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.geri.chat.services.NotificationJobService;
import com.geri.chat.services.NotificationService;
import com.google.firebase.firestore.DocumentSnapshot;

public class AlarmReceiver extends BroadcastReceiver {
    private static DocumentSnapshot lastDoc;

    @Override
    public void onReceive(Context context, Intent i) {
        Log.i("NotificationJobService", "Alarm received");
        Intent intent = new Intent(context.getApplicationContext(), NotificationService.class);
        intent.addCategory("NotificationJobService");
        NotificationJobService.enqueueWork(context.getApplicationContext(), intent);
    }


}
