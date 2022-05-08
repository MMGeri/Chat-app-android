package com.geri.chat.ui.UserMain.fragments.friends.friends_add;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.geri.chat.R;
import com.geri.chat.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {
    private SearchView searchView;
    private RecyclerView recyclerView;

    private ArrayList<User> mUsersList;
    private AddFriendsAdapter mAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mUsersList = new ArrayList<>();
        mAdapter = new AddFriendsAdapter(this, mUsersList);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();
        getData(currentUser.getUid());

        createSpinner();

        recyclerView = findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        searchView = findViewById(R.id.friends_search_view);
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
        getData(currentUser.getUid());
    }

    private void getData(String userId) {
        Source source = Source.SERVER;
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DocumentSnapshot document = task.getResult();
                if (document.exists()) {

                    HashMap<String,HashMap<String, Object>> friendsIds = (HashMap<String,HashMap<String, Object>>) (document.getData().get("friends"));
                    //arraylist containing all the friends ids
                    ArrayList<String> friendsIdsList = new ArrayList<>();
                    for (String key : friendsIds.keySet()) {
                        friendsIdsList.add(key);
                    }
                    CollectionReference users = db.collection("users");

                    users.whereNotEqualTo("id", userId).get(source).addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            mUsersList.clear();
                            for (DocumentSnapshot doc : task2.getResult()) {
                                if (!friendsIdsList.contains(doc.getId())) {
                                    String id = doc.getId();
                                    String name = doc.getString("name");
                                    mUsersList.add(new User(id, name, "", LocalDateTime.now()));
                                }
                            }
                            //sort mUsersList by name alphabetically
                            Collections.sort(mUsersList, new Comparator<User>() {
                                @Override
                                public int compare(User o1, User o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });


                            mAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("KozosDebug", "Error getting documents: ", task2.getException());
                        }
                    });
                }

            } else {
                Log.d("KozosDebug", "Error getting documents: ", task.getException());
            }
            destroySpinner();
        });
    }


    private void createSpinner() {
        findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
    }

    private void destroySpinner() {
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
    }


}

