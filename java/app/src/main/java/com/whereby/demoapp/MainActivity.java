package com.whereby.demoapp;

import static com.whereby.sdk.WherebyConstants.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import com.whereby.sdk.*;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    /**
     * Replace with your room URL.
     * See https://docs.whereby.com/creating-and-deleting-rooms
     */
    private String mRoomUrlString = "";

    private Button mToggleCameraButton, mToggleMicrophoneButton;
    private WherebyRoomFragment mRoomFragment;
    private boolean mIsPresentingFullScreen = false;

    //region Activity lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startEmbeddedButton = findViewById(R.id.button_start_embedded);
        Button startFullScreenButton = findViewById(R.id.button_start_fullscreen);
        mToggleCameraButton = findViewById(R.id.button_toggle_camera);
        mToggleMicrophoneButton = findViewById(R.id.button_toggle_microphone);
        Button removeFragmentButton = findViewById(R.id.button_remove_fragment);

        startEmbeddedButton.setOnClickListener(view -> embedInFragment(R.id.layout_fragment_container_embedded, false));
        startFullScreenButton.setOnClickListener(view -> embedInFragment(R.id.layout_fragment_container_fullscreen, true));
        mToggleCameraButton.setOnClickListener(view -> mRoomFragment.toggleCameraEnabled());
        mToggleMicrophoneButton.setOnClickListener(view -> mRoomFragment.toggleMicrophoneEnabled());
        removeFragmentButton.setOnClickListener(view -> this.removeRoomFragment());

        initMediaButtons();
    }

    @Override
    public void onBackPressed() {
        if (mIsPresentingFullScreen) {
            mIsPresentingFullScreen = false;
            removeRoomFragment();
        } else {
            super.onBackPressed();
        }
    }
    //endregion

    //region private
    private void embedInFragment(int frameLayout, boolean isFullScreen) {
        mIsPresentingFullScreen = isFullScreen;
        initMediaButtons();

        if (mRoomFragment != null) {
            removeRoomFragment();
        }

        mRoomFragment = new WherebyRoomFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ROOM_CONFIG_KEY, createWherebyRoomConfig());
        mRoomFragment.setArguments(bundle);

        // Optional: this allows to receive async events during the meeting when using the room fragment, by implementing the
        // WherebyEventListener methods.
        // Comment the following line to disable.
        setRoomFragmentEventListener();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(frameLayout, mRoomFragment);
        fragmentTransaction.commit();
        mRoomFragment.join();
    }

    private WherebyRoomConfig createWherebyRoomConfig() {
        WherebyRoomConfig roomConfig = new WherebyRoomConfig(createRoomUrl());

        // Optional: customize the room before joining the meeting.
        // Comment the following lines to skip room customization.
        roomConfig.setMicrophoneEnabledAtStart(false);
        roomConfig.setCameraEnabledAtStart(true);
        roomConfig.setDisplayName("Participant name");
        //...
        return roomConfig;
    }

    private URL createRoomUrl() {
        URL roomURL = null;
        try {
            roomURL = new URL(mRoomUrlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return roomURL;
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

        if (mRoomFragment == null) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(mRoomFragment);
        fragmentTransaction.commit();
        mRoomFragment = null;
    }

    private void initMediaButtons() {
        mToggleCameraButton.setBackgroundColor(Color.GRAY);
        mToggleMicrophoneButton.setBackgroundColor(Color.GRAY);
        mToggleCameraButton.setEnabled(false);
        mToggleMicrophoneButton.setEnabled(false);
    }
    //endregion
}