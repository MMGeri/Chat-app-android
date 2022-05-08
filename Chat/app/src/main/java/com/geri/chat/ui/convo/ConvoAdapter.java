package com.geri.chat.ui.convo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.geri.chat.R;
import com.geri.chat.data.model.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ConvoAdapter extends RecyclerView.Adapter implements Filterable {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = -1;
    private ArrayList<Message> filteredMessages;
    private Context context;
    private int lastPosition = -1;

    public ConvoAdapter(Context context, ArrayList<Message> messages) {
        this.filteredMessages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_message_right, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_message, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
//        return new ReceivedMessageHolder(LayoutInflater.from(context).inflate(R.layout.list_message, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) filteredMessages.get(position);
        if (message.getSender().getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) filteredMessages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bindTo(message);
                if(holder.getAdapterPosition() > lastPosition) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                    holder.itemView.startAnimation(animation);
                    lastPosition = holder.getAdapterPosition();
                }
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bindTo(message);
                if(holder.getAdapterPosition() > lastPosition) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
                    holder.itemView.startAnimation(animation);
                    lastPosition = holder.getAdapterPosition();
                }
        }
    }

    @Override
    public int getItemCount() {
        return filteredMessages.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }


    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private TextView sender;
        private TextView messageText;
        private TextView timeSent;
        private ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            sender = itemView.findViewById(R.id.sender_name);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.sender_image);

        }

        public void bindTo(Message currentMessage) {
            sender.setText(currentMessage.getSender().getName());
            messageText.setText(currentMessage.getText());
            profileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile_pic));
            //Glide.with(context).load(currentMessage.getImageResource()).into(profileImage);
        }
    }

        class SentMessageHolder extends RecyclerView.ViewHolder {
            private TextView messageText;
            private TextView timeSent;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.message_text_right);

            }

            public void bindTo(Message currentMessage) {
                messageText.setText(currentMessage.getText());
                //Glide.with(context).load(currentMessage.getImageResource()).into(profileImage);
            }


        }
    }

