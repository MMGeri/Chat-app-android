package com.geri.chat.services;

import android.app.PendingIntent;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.geri.chat.R;
import com.geri.chat.ui.UserMain.UserMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class NotificationJobService extends JobIntentService {
    public static final int JOB_ID = 1;
    public static ListenerRegistration listenerRegistration;
    private static DocumentSnapshot lastDoc;
    private FirebaseFirestore db;


    public NotificationJobService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void enqueueWork(Context context, Intent work) {
        Log.i("Alarm", "enqueueWork");
        enqueueWork(context, NotificationJobService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        db = FirebaseFirestore.getInstance();
        Log.i("NotificationJobService", "Started Notification Service");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i("NotificationJobService", "User ID: " + userId);
        syncData(userId,this);
    }

    private void syncData(String userId, Context context) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        DocumentReference docRef = db.collection("users").document(userId);
        listenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("NotificationJobService", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("NotificationJobService", "Current data: LOADED");
                    sendNotification(snapshot);
                } else {
                    Log.d("NotificationJobService", "Current data: null");
                }
            }

            private void sendNotification(DocumentSnapshot snapshot) {
                Log.i("NotificationJobService", "snapshot Doc: " + lastDoc);
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
    }


}