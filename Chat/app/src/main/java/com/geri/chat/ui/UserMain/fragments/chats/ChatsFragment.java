
package com.geri.chat.ui.UserMain.fragments.chats;

import androidx.appcompat.widget.SearchView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.geri.chat.R;

import com.geri.chat.data.model.User;
import com.geri.chat.services.NotificationService;
import com.geri.chat.ui.convo.ConvoActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ChatsFragment extends Fragment {
    private View view;


    private ChatsAdapter mAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private DocumentReference docRef;
    private ArrayList<User> mUsersList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsersList = new ArrayList<>();

        mAdapter = new ChatsAdapter(this.getContext(), mUsersList);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();

        createNotificationChannel();

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chats_fragment, container, false);


        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }
        });


        RecyclerView recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        destroySpinner();

        createSpinner();
        syncData(currentUser.getUid());


        return view;
    }


    private void syncData(String userId) {
        docRef = db.collection("users").document(userId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("KozosDebug", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("KozosDebug", "Current data: LOADED");
                    loadFriends(snapshot.getData());
                } else {
                    Log.d("KozosDebug", "Current data: null");
                }
                destroySpinner();
            }


            private void loadFriends(Map<String, Object> data) {
                HashMap<String, HashMap<String, String>> map = null;
                try {
                    map = (HashMap<String, HashMap<String, String>>) data.get("friends");
                } catch (Exception e) {
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                mUsersList.clear();
                if (map != null) {
                    for (String key : map.keySet()) {
                        String id = key;
                        String name = map.get(key).get("name");
                        String lastMessage = map.get(key).get("lastMessage");
                        LocalDateTime lastMessageTime = LocalDateTime.parse(map.get(key).get("lastMessageTime").replace('T', ' '), formatter);
                        mUsersList.add(new User(id, name, lastMessage, lastMessageTime));
                    }
                    Collections.sort(mUsersList, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return o1.getLastMessageSent().isBefore(o2.getLastMessageSent()) ? 1 : -1;
                        }
                    });
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        CharSequence name = "default channel";
        String description = "default channel for notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("default", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        notificationManager.createNotificationChannel(channel);


        name = "silent channel";
        description = "default channel for running in background";
        importance = NotificationManager.IMPORTANCE_NONE;
        channel = new NotificationChannel("silent", name, importance);
        channel.setDescription(description);
        notificationManager = getContext().getSystemService(NotificationManager.class);
        channel.enableVibration(false);
        notificationManager.createNotificationChannel(channel);

    }


    private void createSpinner() {
        view.findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
    }

    private void destroySpinner() {
        view.findViewById(R.id.loading_spinner).setVisibility(View.GONE);
    }


}