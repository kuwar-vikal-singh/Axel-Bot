package com.axel.axelai.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axel.axelai.Model.Message;
import com.axel.axelai.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message.getSender().equals("user")) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.assistantMessageTextView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getText());
        } else {
            holder.assistantMessageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setVisibility(View.GONE);
            holder.assistantMessageTextView.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, assistantMessageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            assistantMessageTextView = itemView.findViewById(R.id.assistantMessageTextView);
        }
    }
}
