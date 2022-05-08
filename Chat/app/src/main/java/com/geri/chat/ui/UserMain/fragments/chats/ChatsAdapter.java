package com.geri.chat.ui.UserMain.fragments.chats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.geri.chat.R;
import com.geri.chat.data.model.User;
import com.geri.chat.services.NotificationService;
import com.geri.chat.ui.convo.ConvoActivity;

import java.util.ArrayList;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> implements Filterable {
    private ArrayList<User> users;
    private ArrayList<User> filteredUsers;
    private Context context;
    private int lastPosition = -1;

    public ChatsAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.filteredUsers = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
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
        private TextView lastMessage;
        private ImageView profileImage;

        ChatsViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            profileImage = itemView.findViewById(R.id.user_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    itemView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
                    Intent intent = new Intent(context, ConvoActivity.class);
                    intent.putExtra("sender_id", filteredUsers.get(getAdapterPosition()).getId());
                    intent.putExtra("sender_name", filteredUsers.get(getAdapterPosition()).getName());
                    intent.putExtra("sender_lastMessage", filteredUsers.get(getAdapterPosition()).getLastMessage());
                    intent.putExtra("sender_lastMessageSent", filteredUsers.get(getAdapterPosition()).getLastMessageSentAsString());


                    context.startActivity(intent);
                    context.stopService(new Intent(context, NotificationService.class));
                }
            });

        }

        public void bindTo(User currentUser) {
            username.setText(currentUser.getName());
            lastMessage.setText(currentUser.getLastMessage());
            profileImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_profile_pic));

//            Glide.with(context).load(currentUser.getImageResource()).into(profileImage);
        }
    }
}

