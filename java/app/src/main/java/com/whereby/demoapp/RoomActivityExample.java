package com.whereby.demoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.whereby.sdk.*;

import static com.whereby.demoapp.RoomHelper.*;
import static com.whereby.sdk.WherebyConstants.*;

public class RoomActivityExample extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room_example1);
        setTitle("Example 1: WherebyRoomActivity");
        Button joinRoomButton = findViewById(R.id.button_join_room);
        joinRoomButton.setOnClickListener(view -> {
            navigateToCustomWherebyRoomActivity();
        });
    }

    @Override
    protected void onResume() {
        unregisterLocalBroadcastReceiver(mBroadcastReceiver, this);
        super.onResume();
    }
    //endregion

    //region private
    private void navigateToCustomWherebyRoomActivity() {
        Intent intent = new Intent(RoomActivityExample.this, CustomWherebyRoomActivity.class);
        WherebyRoom room = createWherebyRoom();
        intent.putExtra(ROOM_ARGUMENT_KEY, room);
        startActivity(intent);
    }

    private WherebyRoom createWherebyRoom() {
        WherebyRoom room = new WherebyRoom(createRoomUrl());

        // Optional: set parameters to customize the room before joining the meeting.
        // Comment the following line to skip room customization.
        room.setParameters(createRoomParameters());

        // Optional: this allows to receive async events through a broadcast receiver during the meeting.
        // Comment the following lines to disable.
        room.setEventBroadcastEnabled(true);
        mBroadcastReceiver = registerLocalBroadcastReceiver(this);

        return room;
    }
    //endregion
}