package com.axel.axelai.Ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel.axelai.Adapter.ChatAdapter;
import com.axel.axelai.Api.ChatApiService;
import com.axel.axelai.Model.Message;
import com.axel.axelai.R;
import com.axel.axelai.Request.ChatRequest;
import com.axel.axelai.Request.ChatResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ChatBot";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    private ChatApiService chatApiService;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private EditText messageInput;
    private ImageView sendBtn, bgImg;

    private Queue<String> messageQueue; // Queue for messages
    private boolean isAnimatingResponse = false; // Flag to check if response is animating

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views and variables
        messageInput = findViewById(R.id.messageInput);
        recyclerView = findViewById(R.id.recyclerView);
        bgImg = findViewById(R.id.bg_img);
        sendBtn = findViewById(R.id.sendButton);
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        chatApiService = retrofit.create(ChatApiService.class);
        messageQueue = new LinkedList<>(); // Initialize message queue

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only allow sending message if not animating response
                if (!isAnimatingResponse) {
                    bgImg.setVisibility(View.GONE);
                    sendMessage();
                }
            }
        });
    }

    private void sendMessage() {
        String userMessage = messageInput.getText().toString();
        if (!userMessage.isEmpty()) {
            // Immediately add the user's message to the chat
            messages.add(new Message("user", userMessage));
            chatAdapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);

            // Clear the input field after sending
            messageInput.setText("");

            // Add the user message to the queue for processing
            messageQueue.add(userMessage);
            processNextMessage();
        }
    }

    private void processNextMessage() {
        if (!messageQueue.isEmpty() && !isAnimatingResponse) {
            String userMessage = messageQueue.poll(); // Get the next message from the queue
            sendMessageToChatbot(userMessage); // Send only the user message
        }
    }

    private void sendMessageToChatbot(String userMessage) {
        // Create request object with conversation context
        List<ChatRequest.Part> parts = new ArrayList<>();

        // Append the entire conversation to provide context
        StringBuilder contextBuilder = new StringBuilder();
        for (Message msg : messages) {
            contextBuilder.append(msg.getSender()).append(": ").append(msg.getText()).append("\n");
        }
        contextBuilder.append("User: ").append(userMessage); // Append the new user message
        parts.add(new ChatRequest.Part(contextBuilder.toString()));

        // Wrap it in the Content object
        List<ChatRequest.Content> contents = new ArrayList<>();
        contents.add(new ChatRequest.Content(parts)); // Wrap parts in contents

        // Create the ChatRequest
        ChatRequest request = new ChatRequest(contents);

        // Add a loading message while waiting for the response
        Message loadingMessage = new Message("assistant", "Thinking...");
        messages.add(loadingMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Make API call
        chatApiService.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();

                    // Check if candidates are present
                    if (chatResponse.getCandidates() != null && !chatResponse.getCandidates().isEmpty()) {
                        // Get the first candidate
                        ChatResponse.Candidate candidate = chatResponse.getCandidates().get(0);

                        // Check if content is present
                        if (candidate.getContent() != null) {
                            List<ChatResponse.Candidate.Content.Part> parts = candidate.getContent().getParts();

                            // Check if parts are present
                            if (parts != null && !parts.isEmpty()) {
                                String reply = parts.get(0).getText(); // Assuming getText() is a valid method
                                Log.d(TAG, "Response: " + reply);

                                // Remove loading message
                                messages.remove(messages.size() - 1); // Remove the loading message
                                chatAdapter.notifyItemRemoved(messages.size()); // Notify adapter to remove the loading message

                                // Add a new message for the assistant's response
                                messages.add(new Message("assistant", reply)); // Set assistant's response
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                recyclerView.scrollToPosition(messages.size() - 1);
                            } else {
                                Log.e(TAG, "Parts are null or empty.");
                                handleErrorResponse();
                            }
                        } else {
                            Log.e(TAG, "Candidate content is null.");
                            handleErrorResponse();
                        }
                    } else {
                        Log.e(TAG, "Candidates are null or empty.");
                        handleErrorResponse();
                    }
                } else {
                    Log.e(TAG, "Response error: " + response.code() + " - " + response.message());
                    handleErrorResponse();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                // Remove loading message if there was a failure
                messages.remove(messages.size() - 1); // Remove the loading message
                chatAdapter.notifyItemRemoved(messages.size()); // Notify adapter to remove the loading message
            }
        });
    }

    private void handleErrorResponse() {
        // Remove loading message if there was an error
        if (!messages.isEmpty() && messages.get(messages.size() - 1).getText().equals("Thinking...")) {
            messages.remove(messages.size() - 1); // Remove the loading message
            chatAdapter.notifyItemRemoved(messages.size()); // Notify adapter to remove the loading message
        }
    }
}
