package com.whereby.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import com.whereby.sdk.*;

import static com.whereby.demoapp.RoomHelper.*;
import static com.whereby.sdk.WherebyConstants.*;

public class RoomFragmentExample extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;
    private Button mRemoveFragmentButton, mToggleCameraButton, mToggleMicrophoneButton;
    private WherebyRoomFragment mRoomFragment;

    //region Activity lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room_example2);
        setTitle("Example 2: WherebyRoomFragment");

        Button mJoinRoomButton = findViewById(R.id.button_join_room);
        mJoinRoomButton.setOnClickListener(view -> {
            navigateToWherebyRoomFragment(R.id.layout_fragment_container);
            mToggleCameraButton.setOnClickListener(view1 -> mRoomFragment.toggleCamera());
            mToggleMicrophoneButton.setOnClickListener(view2 -> mRoomFragment.toggleMicrophone());
            mRemoveFragmentButton.setOnClickListener(view3 -> this.removeRoomFragment());
        });

        mRemoveFragmentButton = findViewById(R.id.button_remove_fragment);
        mRemoveFragmentButton.setEnabled((false));

        mToggleCameraButton = findViewById(R.id.button_toggle_camera);
        mToggleMicrophoneButton = findViewById(R.id.button_toggle_microphone);
        initMediaButtons();
    }

    @Override
    protected void onStop() {
        unregisterLocalBroadcastReceiver(mBroadcastReceiver, this);
        super.onStop();
    }
    //endregion

    //region private
    private void initMediaButtons() {
        mToggleCameraButton.setBackgroundColor(Color.GRAY);
        mToggleMicrophoneButton.setBackgroundColor(Color.GRAY);
        mToggleCameraButton.setEnabled(false);
        mToggleMicrophoneButton.setEnabled(false);
    }

    private void navigateToWherebyRoomFragment(int frameLayout) {
        mRoomFragment = new WherebyRoomFragment();
        Bundle bundle = new Bundle();
        WherebyRoom room = createWherebyRoom();
        bundle.putSerializable(ROOM_ARGUMENT_KEY, room);
        mRoomFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(frameLayout, mRoomFragment);
        fragmentTransaction.commit();
        mRoomFragment.join();

        initMediaButtons();
        mRemoveFragmentButton.setEnabled(true);
    }

    private WherebyRoom createWherebyRoom() {
        WherebyRoom room = new WherebyRoom(createRoomUrl());

        // Optional: set parameters to customize the room before joining the meeting.
        // Comment the following line to skip room customization.
        room.setParameters(createRoomParameters());

        // Optional: this allows to receive async events through a broadcast receiver during the meeting.
        // Comment the following lines to disable.
        room.setEventBroadcastEnabled(true);
        unregisterLocalBroadcastReceiver(mBroadcastReceiver, this); // Need to unregister first, in case the fragment was not removed
        mBroadcastReceiver = registerLocalBroadcastReceiver(this);

        // Optional: this allows to receive async events through a listener during the meeting, by
        // implementing the WherebyEventListener methods.
        // Comment the following line to disable.
        setRoomFragmentEventListener();

        return room;
    }

    private void setRoomFragmentEventListener() {
        mRoomFragment.setEventListener(new WherebyEventListener() {

            // All the methods below are optional:
            @Override
            public void onRoomReady() {
                runOnUiThread(() -> {
                    mToggleCameraButton.setEnabled(true);
                    mToggleMicrophoneButton.setEnabled(true);
                });
            }

            @Override
            public void onMicrophoneToggled(boolean enabled) {
                setButtonBackgroundColor(mToggleMicrophoneButton, enabled);
            }

            @Override
            public void onCameraToggled(boolean enabled) {
                setButtonBackgroundColor(mToggleCameraButton, enabled);
            }

            // Helper:
            private void setButtonBackgroundColor(Button button, boolean enabled) {
                button.setBackgroundColor(enabled
                        ? getResources().getColor(R.color.green)
                        : getResources().getColor(R.color.red));
            }

            @Override
            public void onLocalParticipantLeft(boolean isRemoved) {
                runOnUiThread(() -> {
                    initMediaButtons();
                });
            }

            //...
        });
    }

    private void removeRoomFragment() {
        initMediaButtons();
        mRemoveFragmentButton.setEnabled(false);
        unregisterLocalBroadcastReceiver(mBroadcastReceiver, this);
        if (mRoomFragment == null) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(mRoomFragment);
        fragmentTransaction.commit();
        mRoomFragment = null;
    }
    //endregion
}
