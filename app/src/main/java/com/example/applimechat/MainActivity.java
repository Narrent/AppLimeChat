package com.example.applimechat;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;

import android.widget.Button;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://lime-chat-3781d-default-rtdb.europe-west1.firebasedatabase.app");
    DatabaseReference myRef = database.getReference("messages");
    private DatabaseReference userCountRef = database.getReference("userCount");

    EditText mEditTextMessage;
    Button mSendButton;
    RecyclerView mMessagesRecycler;
    private TextView mUserCountText;
    ArrayList<String> messages = new ArrayList<>();

    private static int maxLength = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mSendButton = findViewById(R.id.send_message_b);
        mEditTextMessage = findViewById(R.id.message_input);
        mMessagesRecycler = findViewById(R.id.messages_recycler);
        mUserCountText = findViewById(R.id.user_count_text);

        mMessagesRecycler.setLayoutManager(new LinearLayoutManager(this));

        DataAdapter dataAdapter = new DataAdapter(this,messages);

        mMessagesRecycler.setAdapter(dataAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener()
        {
           @Override
            public void onClick(View view)
           {
               String msg = mEditTextMessage.getText().toString();
               if(msg.isEmpty())
               {
                   Toast.makeText(getApplicationContext(),"Поле пусте!", Toast.LENGTH_SHORT).show();
                   return;
               }
               if (msg.length()==maxLength)
               {
                   Toast.makeText(getApplicationContext(),"Надто длине повідомлення!", Toast.LENGTH_SHORT).show();
                   return;
               }

               myRef.push().setValue(msg);
               mEditTextMessage.setText("");

           }

        });

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String msg = snapshot.getValue(String.class);
                messages.add(msg);
                dataAdapter.notifyDataSetChanged();
                mMessagesRecycler.smoothScrollToPosition(messages.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        userCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long userCount = snapshot.getValue(Long.class);
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