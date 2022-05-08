package com.geri.chat.ui.UserMain.fragments.friends;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.geri.chat.R;
import com.geri.chat.data.DAO;
import com.geri.chat.data.model.User;
import com.geri.chat.ui.convo.ConvoActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ChatsViewHolder> implements Filterable {
    private ArrayList<User> users;
    private ArrayList<User> filteredUsers;
    private Context context;
    private int lastPosition = -1;

    public FriendsAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.filteredUsers = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_friends, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        User currentUser = filteredUsers.get(position);

        holder.bindTo(currentUser);
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    @Override
    public Filter getFilter() {
        return chatsFilter;
    }

    private Filter chatsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<User> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.count = users.size();
                results.values = users;
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (User user : users) {
                    Log.i("KozosDebug", "user: " + user.getName());
                    if (user.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }


            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredUsers = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    };


    class ChatsViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageView profileImage;
        private Button friendButton;

        ChatsViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.friends_name);
            profileImage = itemView.findViewById(R.id.friends_image);
            friendButton = itemView.findViewById(R.id.friends_button);

            friendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFriend(filteredUsers.get(getAdapterPosition()).getId());
                }
            });


        }

        private void deleteFriend(String id) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference chat = filteredUsers.get(getAdapterPosition()).getChatRef();
            chat.delete();

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId);
            docRef.update("friends."+id, FieldValue.delete());

            DocumentReference docRef2 = FirebaseFirestore.getInstance().collection("users").document(id);
            docRef2.update("friends."+currentUserId, FieldValue.delete());
        }

        public void bindTo(User currentUser) {
            username.setText(currentUser.getName());
            profileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile_pic));

//            Glide.with(context).load(currentUser.getImageResource()).into(profileImage);
        }
    }
}

