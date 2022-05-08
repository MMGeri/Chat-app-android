package com.geri.chat.ui.UserMain.fragments.friends;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geri.chat.R;
import com.geri.chat.data.DAO;
import com.geri.chat.data.model.User;
import com.geri.chat.ui.UserMain.fragments.chats.ChatsAdapter;
import com.geri.chat.ui.UserMain.fragments.chats.ChatsFragment;
import com.geri.chat.ui.UserMain.fragments.friends.friends_add.AddFriendActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class FriendsFragment extends Fragment {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private View view;

    private ArrayList<User> mUsersList;
    private FriendsAdapter mAdapter;
    private FirebaseFirestore db;

    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsersList = new ArrayList<>();
        mAdapter = new FriendsAdapter(this.getContext(), mUsersList);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();
        syncData(currentUser.getUid());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.friends_fragment, container, false);
        createSpinner();


        searchView = view.findViewById(R.id.friends_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.i("KozosDebug", mAdapter.getItemCount() + "");
                mAdapter.getFilter().filter(s);
                return false;
            }
        });


        recyclerView = view.findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = view.findViewById(R.id.friends_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddFriendActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    private void syncData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
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
                HashMap<String, HashMap<String, Object>> map = null;
                try {
                    map = (HashMap<String, HashMap<String, Object>>) data.get("friends");
                } catch (Exception e) {
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                mUsersList.clear();
                if (map != null) {
                    for (String key : map.keySet()) {
                        String id = key;
                        String name = (String) map.get(key).get("name");
                        String lastMessage =(String) map.get(key).get("lastMessage");
                        LocalDateTime lastMessageTime = LocalDateTime.parse(((String)map.get(key).get("lastMessageTime")).replace('T', ' '), formatter);
                        mUsersList.add(new User(id, name, lastMessage, lastMessageTime,(DocumentReference) map.get(key).get("chat")));
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


    private void createSpinner() {
        view.findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
    }

    private void destroySpinner() {
        view.findViewById(R.id.loading_spinner).setVisibility(View.GONE);
    }


}