package com.openclassrooms.firebaseoc.mentor_chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.openclassrooms.firebaseoc.R;
import com.openclassrooms.firebaseoc.models.Message;

public class mentorChatAdapter extends FirestoreRecyclerAdapter<Message, MessageViewHolder> {

    public mentorChatAdapter(FirestoreRecyclerOptions<Message> options, RequestManager glide, Listener callback, String idCurrentUser) {
        super(options);
        this.glide = glide;
        this.idCurrentUser = idCurrentUser;
        this.callback = callback;
    }

    public interface Listener {
        void onDataChanged();
    }

    //FOR DATA
    private final RequestManager glide;
    private final String idCurrentUser;

    //FOR COMMUNICATION
    private Listener callback;

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {

        holder.updateWithMessage(model, this.idCurrentUser, this.glide);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MessageViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.activity_mentor_chat_item, parent, false));
    }


@Override
public void onDataChanged() {
    super.onDataChanged();
    this.callback.onDataChanged();

}

}
