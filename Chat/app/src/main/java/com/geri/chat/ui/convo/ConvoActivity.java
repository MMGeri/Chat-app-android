package com.geri.chat.ui.convo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geri.chat.R;
import com.geri.chat.alarms.AlarmReceiver;
import com.geri.chat.data.model.Message;
import com.geri.chat.data.model.User;
import com.geri.chat.services.NotificationJobService;
import com.geri.chat.services.NotificationService;
import com.geri.chat.ui.UserMain.UserMainActivity;
import com.geri.chat.ui.UserMain.fragments.chats.ChatsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConvoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ConvoAdapter mAdapter;
    private FirebaseFirestore db;
    private EditText editText;


    private User receiver;
    private User user;


    private DocumentReference docRefUser;
    private DocumentReference docRef;
    private String docRefId;
    private ArrayList<Message> mMessagesList;

    private PendingIntent pendingIntent;
    private AlarmManager mAlarmManager;


    private ListenerRegistration listener;
    private ListenerRegistration listenerForUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convo);

        setUsers();

        db = FirebaseFirestore.getInstance();
        mMessagesList = new ArrayList<>();
        mAdapter = new ConvoAdapter(this, mMessagesList);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        editText = findViewById(R.id.convo_input);
        setAlarmManager();
        setUpRecyclerView();



        //set convo name
        TextView convo_name = findViewById(R.id.convo_name);
        convo_name.setText((getIntent().getStringExtra("sender_name")));

        createSpinner();
        db.collection("users").document(user.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>) documentSnapshot.get("friends");
                    if (map != null) {
                        for (String key : map.keySet()) {
                            if (key.equals(receiver.getId())) {
                                DocumentReference docRef = (DocumentReference) map.get(key).get("chat");
                                docRefId = docRef.getId();
                                syncData(docRef.getId());
                            }
                        }
                    }
                    destroySpinner();
                } else {
                    Toast.makeText(ConvoActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.convo_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        setScrollToBottom();
    }

    private void setUsers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String time = getIntent().getStringExtra("sender_lastMessageSent");
        String lastMessage = getIntent().getStringExtra("sender_lastMessage");
        String sender_name = getIntent().getStringExtra("sender_name");
        String sender_id = getIntent().getStringExtra("sender_id");
        receiver = new User(sender_id, sender_name, lastMessage, time);
        user = new User(currentUser.getUid(), currentUser.getDisplayName(), "", LocalDateTime.now());
    }

    public void setAlarmManager() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("userId", user.getId());
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 60000, pendingIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setAlarmManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlarmManager.cancel(pendingIntent);
        if(NotificationJobService.listenerRegistration != null)
            NotificationJobService.listenerRegistration.remove();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listener.remove();
        listenerForUser.remove();
    }

    private void syncData(String docId) {
        docRef = db.collection("chats").document(docId);
        docRefUser = db.collection("users").document(receiver.getId());
        listener = checkMessages();
        listenerForUser = checkUser();
    }

    private ListenerRegistration checkUser() {
        return docRefUser.addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                Log.d("KozosDebug", "Current data: LOADED");
                if (((Map<String, Object>) snapshot.getData().get("friends")).get(user.getId()) == null)
                    finish();
            }
        });
    }

    private ListenerRegistration checkMessages() {
        return docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("KozosDebug", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("KozosDebug", "Current data: LOADED");
                    loadMessages(snapshot.getData());
                } else {
                    Log.d("KozosDebug", "Current data: null");
                }
            }

            private void loadMessages(Map<String, Object> data) {
                ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) data.get("messages");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                mMessagesList.clear();
                if (list != null) {
                    for (HashMap<String, String> map : list) {
                        LocalDateTime time;
                        time = LocalDateTime.parse(map.get("time").replace('T', ' '), formatter);
                        User sender = new User(map.get("sender_id"), user.getId().equals(map.get("sender_id")) ? user.getName() : receiver.getName(), "", time);
                        User receiver2 = new User(map.get("receiver_id"), "", "", time);
                        mMessagesList.add(new Message(map.get("message"), sender, receiver2, time));
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    private void setScrollToBottom() {
        View rootView = findViewById(R.id.convo_root);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff > 100) {
                    if (mAdapter.getItemCount() > 0)
                        recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                }
            }
        });
    }


    public void send_message(View view) {
        String message = ((EditText) findViewById(R.id.convo_input)).getText().toString();
        if (!message.isEmpty()) {
            editText.clearFocus();
            editText.setText("");
            LocalDateTime time = LocalDateTime.now();

            db.collection("chats").document(docRefId).update("messages", FieldValue.arrayUnion(new HashMap<String, String>() {{
                put("message", message);
                put("sender_id", user.getId());
                put("time", time.toString());
                put("receiver_id", receiver.getId());
            }}));

            CollectionReference collectionReference = db.collection("users");
            collectionReference.document(receiver.getId()).update("friends." + user.getId(), new HashMap<String, Object>() {{
                put("chat", docRef);
                put("name", user.getName());
                put("lastMessage", message);
                put("lastMessageTime", time.toString());
            }});
        }
    }

    private void createSpinner() {
        findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
    }

    private void destroySpinner() {
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
    }


    public void backToConvoList(View view) {
        finish();
    }
}