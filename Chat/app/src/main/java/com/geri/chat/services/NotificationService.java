package com.geri.chat.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.geri.chat.R;
import com.geri.chat.data.model.User;
import com.geri.chat.ui.UserMain.UserMainActivity;
import com.geri.chat.ui.convo.ConvoActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {
    private ListenerRegistration listenerRegistration;
    private DocumentSnapshot lastDoc;
    private FirebaseFirestore db;


    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = FirebaseFirestore.getInstance();
        Log.i("ALARMDEBROY", "Started Notification Service");
//        fixNotification(this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i("ALARMDEBROY", "User ID: " + userId);
        syncData(userId,this);
        return START_STICKY;
    }

    private void syncData(String userId, Context context) {
        DocumentReference docRef = db.collection("users").document(userId);
        listenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ALARMDEBROY", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("ALARMDEBROY", "Current data: LOADED");
                    sendNotification(snapshot);
                } else {
                    Log.d("ALARMDEBROY", "Current data: null");
                }
            }

            private void sendNotification(DocumentSnapshot snapshot) {
                if (lastDoc == null)
                    lastDoc = snapshot;

                Map<String, HashMap<String, Object>> lastDocMap = (Map<String, HashMap<String, Object>>) lastDoc.getData().get("friends");
                Map<String, HashMap<String, Object>> currentDocMap = (Map<String, HashMap<String, Object>>) snapshot.getData().get("friends");
                if(currentDocMap == null || lastDocMap == null)
                    return;
                for (Map.Entry<String, HashMap<String, Object>> entry : currentDocMap.entrySet()) {
                    if (lastDocMap.containsKey(entry.getKey())) {
                        if (!entry.getValue().get("lastMessage").equals(lastDocMap.get(entry.getKey()).get("lastMessage")) && entry.getValue().get("lastMessage") != "") {

                            Intent intent = new Intent(context, UserMainActivity.class);
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addNextIntentWithParentStack(intent);

                            PendingIntent pendingIntent =
                                    stackBuilder.getPendingIntent( 0,  PendingIntent.FLAG_IMMUTABLE);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                                    .setSmallIcon(R.drawable.ic_profile_pic)
                                    .setContentTitle(entry.getValue().get("name").toString())
                                    .setContentText(entry.getValue().get("lastMessage").toString())
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);



                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.notify(1, builder.build());


                            lastDoc = snapshot;
                        }
                    }
                }

            }
        });

    }

    private void fixNotification(Context context) {
        Intent intent = new Intent(context, UserMainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent( 0,  PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "silent")
                .setSmallIcon(R.drawable.ic_profile_pic)
                .setContentTitle("App is running in background")
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);



        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        startForeground(1, builder.build());
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        listenerRegistration.remove();
    }




    public class NotificationBinder extends android.os.Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }
}