package com.example.applimechat;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://lime-chat-3781d-default-rtdb.europe-west1.firebasedatabase.app");
    private DatabaseReference myRef = database.getReference("messages");
    private DatabaseReference userCountRef = database.getReference("userCount");

    private EditText mEditTextMessage;
    private Button mSendButton;
    private RecyclerView mMessagesRecycler;
    private TextView mUserCountText;
    private ArrayList<Message> messages = new ArrayList<>();
    private DataAdapter dataAdapter;

    private static int maxLength = 150;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mSendButton = findViewById(R.id.send_message_b);
        mEditTextMessage = findViewById(R.id.message_input);
        mMessagesRecycler = findViewById(R.id.messages_recycler);
        mUserCountText = findViewById(R.id.user_count_text);

        mMessagesRecycler.setLayoutManager(new LinearLayoutManager(this));

        dataAdapter = new DataAdapter(this, messages, deviceId);
        mMessagesRecycler.setAdapter(dataAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mEditTextMessage.getText().toString();
                if (msg.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Поле пусте!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (msg.length() >= maxLength) {
                    Toast.makeText(getApplicationContext(), "Надто длинне повідомлення!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> messageMap = new HashMap<>();
                messageMap.put("text", msg);
                messageMap.put("deviceId", deviceId);

                myRef.push().setValue(messageMap);
                mEditTextMessage.setText("");
            }
        });

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message msg = snapshot.getValue(Message.class);
                if (msg != null) {
                    messages.add(msg);
                    dataAdapter.notifyDataSetChanged();
                    mMessagesRecycler.smoothScrollToPosition(messages.size());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Відстеження кількості користувачів
        userCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long userCount = snapshot.getValue(Long.class);
                if (userCount == null) {
                    userCount = 0L;
                }
                mUserCountText.setText("Користувачів онлайн: " + userCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read user count.", error.toException());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserCount(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserCount(-1);
    }

    private void updateUserCount(final int delta) {
        userCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long currentCount = mutableData.getValue(Long.class);
                if (currentCount == null) {
                    currentCount = 0L;
                }
                mutableData.setValue(currentCount + delta);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.w(TAG, "Failed to update user count.", error.toException());
                }
            }
        });
    }
}