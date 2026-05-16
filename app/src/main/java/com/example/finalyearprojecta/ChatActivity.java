package com.example.finalyearprojecta;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// --- Your OpenAI API interface ---
interface OpenAiApi {
    @retrofit2.http.POST("v1/chat/completions")
    Call<JsonObject> sendMessage(@retrofit2.http.Body JsonObject body);
}

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText input;
    ImageButton send;
    List<ChatMessage> messages = new ArrayList<>();
    ChatAdapter adapter;
    OpenAiApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        recyclerView = findViewById(R.id.recyclerView);
        input = findViewById(R.id.input);
        send = findViewById(R.id.sendBtn);

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // --- OkHttp client with Authorization header ---
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                }).build();

        // --- Retrofit instance ---
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(OpenAiApi.class);

        send.setOnClickListener(v -> {
            String msg = input.getText().toString().trim();
            if (msg.isEmpty()) return;

            // Add user message to RecyclerView
            messages.add(new ChatMessage(msg, true));
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
            input.setText("");

            // Send to OpenAI
            sendToOpenAI(msg);
        });
    }

    private void sendToOpenAI(String userText) {
        // Create message JSON
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userText);

        JsonObject body = new JsonObject();
        body.addProperty("model", "gpt-3.5-turbo");
        body.add("messages", new Gson().toJsonTree(new JsonObject[]{userMessage}));

        api.sendMessage(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonArray choices = response.body().getAsJsonArray("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonObject messageObj = choices.get(0)
                                    .getAsJsonObject()
                                    .getAsJsonObject("message");

                            String reply = "";
                            if (messageObj.has("content")) {
                                reply = messageObj.get("content").getAsString();
                            }

                            messages.add(new ChatMessage(reply, false));
                            adapter.notifyItemInserted(messages.size() - 1);
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "API Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
