package com.whereby.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.net.MalformedURLException;
import java.net.URL;

import com.whereby.sdk.*;

import static com.whereby.sdk.WherebyConstants.*;

public class RoomActivityExample extends AppCompatActivity {

    private final static String TAG = RoomActivityExample.class.getSimpleName();

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
    protected void onStop() {
        unregisterLocalBroadcastReceiver();
        super.onStop();
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
        registerLocalBroadcastReceiver();

        return room;
    }

    private URL createRoomUrl() {
        URL roomURL = null;
        try {
            roomURL = new URL(Constants.roomUrlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return roomURL;
    }

    private WherebyRoomParameters createRoomParameters() {
        WherebyRoomParameters roomParameters = new WherebyRoomParameters();
        roomParameters.setDisplayName("Participant name");
        //...
        return roomParameters;
    }

    private void registerLocalBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(ROOM_BROADCAST_EVENT_ACTION);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WherebyRoomEvent event = (WherebyRoomEvent) intent.getSerializableExtra(ROOM_BROADCAST_EVENT_NAME);

                // Process event:
                Log.d(TAG, event.getRaw());
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterLocalBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            return;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }
    //endregion
}