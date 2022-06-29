package com.whereby.demoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Whereby demo app");

        Button joinRoomUsingWherebyRoomActivity = findViewById(R.id.button_join_room_using_whereby_room_activity);
        joinRoomUsingWherebyRoomActivity.setOnClickListener(view -> {
            navigateToActivity(RoomActivityExample.class);
        });

        Button joinRoomUsingWherebyRoomFragment = findViewById(R.id.button_join_room_using_whereby_room_fragment);
        joinRoomUsingWherebyRoomFragment.setOnClickListener(view -> {
            navigateToActivity(RoomFragmentExample.class);
        });
    }

    private void navigateToActivity(Class activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }
}