package com.whereby.demoapp;

import static com.whereby.sdk.WherebyConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.whereby.sdk.WherebyRoomEvent;
import com.whereby.sdk.WherebyRoomParameters;

import java.net.MalformedURLException;
import java.net.URL;

public class RoomHelper {

    /**
     * Replace with your room URL.
     * See https://docs.whereby.com/creating-and-deleting-rooms
     */
    private static String mRoomUrlString = "";

    public static URL createRoomUrl() {
        URL roomURL = null;
        try {
            roomURL = new URL(mRoomUrlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return roomURL;
    }

    public static WherebyRoomParameters createRoomParameters() {
        WherebyRoomParameters roomParameters = new WherebyRoomParameters();
        roomParameters.setDisplayName("Participant name");
        //...
        return roomParameters;
    }

    public static BroadcastReceiver registerLocalBroadcastReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter(ROOM_BROADCAST_EVENT_ACTION);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WherebyRoomEvent event = (WherebyRoomEvent) intent.getSerializableExtra(ROOM_BROADCAST_EVENT_NAME);

                // Process event:
                Log.d("WherebyRoomEvent", event.getRaw());
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
        return broadcastReceiver;
    }

    public static void unregisterLocalBroadcastReceiver(BroadcastReceiver broadcastReceiver, Context context) {
        if (broadcastReceiver == null) {
            return;
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

}
