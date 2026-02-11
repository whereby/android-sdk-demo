package com.whereby.demoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.whereby.sdk.*;

public class MainActivity extends AppCompatActivity {

    // ─────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────

    private static final String TAG_ROOM_FRAGMENT = "WHEREBY_ROOM_FRAGMENT";

    /**
     * Replace with your room URL.
     * See https://docs.whereby.com/creating-and-deleting-rooms
     */
    private static final String roomUrlString = "";

    // ─────────────────────────────────────────────
    // Views
    // ─────────────────────────────────────────────

    private TextInputEditText textInput;
    private Button toggleCameraButton, toggleMicrophoneButton, removeFragmentButton;
    private FrameLayout fullScreenFrameLayout;

    // ─────────────────────────────────────────────
    // Activity lifecycle
    // ─────────────────────────────────────────────

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initMediaButtons();
        setupClickListeners();
    }

    // ─────────────────────────────────────────────
    // UI setup
    // ─────────────────────────────────────────────

    private void bindViews() {
        textInput = findViewById(R.id.text_input_room_url);
        textInput.setText(roomUrlString);

        toggleMicrophoneButton = findViewById(R.id.button_toggle_microphone);
        toggleCameraButton = findViewById(R.id.button_toggle_camera);
        removeFragmentButton = findViewById(R.id.button_remove_fragment);

        fullScreenFrameLayout = findViewById(R.id.layout_fragment_container_fullscreen);
    }

    private void setControlsEnabled(boolean enabled) {
        toggleMicrophoneButton.setEnabled(enabled);
        toggleCameraButton.setEnabled(enabled);
    }

    private void initMediaButtons() {
        toggleCameraButton.setBackgroundColor(Color.GRAY);
        toggleMicrophoneButton.setBackgroundColor(Color.GRAY);
        setControlsEnabled(false);
    }

    private void setupClickListeners() {
        Button loadEmbeddedButton = findViewById(R.id.button_start_embedded);
        Button loadFullscreenButton = findViewById(R.id.button_start_fullscreen);

        loadEmbeddedButton.setOnClickListener(v -> {
            navigateToRoomFragment(R.id.layout_fragment_container_embedded);
            setControlsEnabled(true);
        });

        loadFullscreenButton.setOnClickListener(v -> {
            fullScreenFrameLayout.setVisibility(View.VISIBLE);
            navigateToRoomFragment(R.id.layout_fragment_container_fullscreen);
        });

        toggleCameraButton.setOnClickListener(v -> {
            WherebyRoomFragment fragment = getRoomFragment();
            if (fragment != null) fragment.toggleCameraEnabled();
        });

        toggleMicrophoneButton.setOnClickListener(v -> {
            WherebyRoomFragment fragment = getRoomFragment();
            if (fragment != null) fragment.toggleMicrophoneEnabled();
        });

        removeFragmentButton.setOnClickListener(view -> this.removeRoomFragment());
    }

    // ─────────────────────────────────────────────
    // Fragment navigation
    // ─────────────────────────────────────────────

    private void navigateToRoomFragment(int containerId) {
        setControlsEnabled(false);
        removeRoomFragment();

        WherebyRoomConfig roomConfig = buildWherebyRoomConfig();

        if (roomConfig == null) return;

        WherebyRoomFragment fragment = WherebyRoomFragment.newInstance(roomConfig);
        attachWherebyRoomListener(fragment);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment, TAG_ROOM_FRAGMENT)
                .commit();
    }

    private void removeRoomFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment existing = fm.findFragmentByTag(TAG_ROOM_FRAGMENT);

        if (existing != null) {
            fm.beginTransaction()
                    .remove(existing)
                    .commit();
        }

        initMediaButtons();
    }

    @Nullable
    private WherebyRoomFragment getRoomFragment() {
        Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_ROOM_FRAGMENT);
        return (f instanceof WherebyRoomFragment) ? (WherebyRoomFragment) f : null;
    }

    // ─────────────────────────────────────────────
    // Whereby room configuration
    // ─────────────────────────────────────────────

    @Nullable
    private WherebyRoomConfig buildWherebyRoomConfig() {
        if(textInput.getText() == null) {
            return null;
        }
        String roomUrl = textInput.getText().toString().trim();

        WherebyRoomConfig roomConfig = new WherebyRoomConfig(roomUrl);

        // Optional: customize the room before joining the meeting.
        // Comment the following lines to skip room customization.
        roomConfig.setDisplayName("Name");
        roomConfig.setRoomBackgroundVisible(false);
        // ...
        return roomConfig;
    }

    // ─────────────────────────────────────────────
    // Whereby events
    // ─────────────────────────────────────────────

    private void attachWherebyRoomListener(WherebyRoomFragment fragment) {
        fragment.setListener(new WherebyRoomListener() {

            // All the methods below are optional:
            @Override
            public void onRoomReady() {
                runOnUiThread(() -> {
                    setControlsEnabled(true);
                });
            }

            @Override
            public void onLocalParticipantKnocked() {
                // ...
            }

            @Override
            public void onLocalParticipantJoined() {
                // ...
            }

            @Override
            public void onLocalParticipantLeft(boolean isRemoved) {
                runOnUiThread(() -> {
                    initMediaButtons();
                });
            }

            @Override
            public void onRemoteParticipantJoined(WherebyRoomParticipant participant) {
                // ...
                Log.d("onRemoteParticipantJoined", "Participant metadata: " + participant.getMetadata());
            }

            @Override
            public void onRemoteParticipantLeave(WherebyRoomParticipant participant) {
                // ...
            }

            @Override
            public void onParticipantCountUpdated(int count) {
                Log.d("onParticipantCountUpdated", "Count: " + count);
                // ...
            }

            @Override
            public void onMicrophoneToggled(boolean enabled) {
                    setButtonBackgroundColor(toggleMicrophoneButton, enabled);
            }

            @Override
            public void onCameraToggled(boolean enabled) {
                    setButtonBackgroundColor(toggleCameraButton, enabled);
            }

            // Helper:
            private void setButtonBackgroundColor(Button button, boolean enabled) {
                button.setBackgroundColor(enabled
                        ? getResources().getColor(R.color.green)
                        : getResources().getColor(R.color.red));
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Log.e("onError", "Whereby SDK:", error);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Whereby error: " + (error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName()),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

            //...
        });
    }

    // ─────────────────────────────────────────────
    // Back navigation
    // ─────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        if (fullScreenFrameLayout.getVisibility() == View.VISIBLE) {
            fullScreenFrameLayout.setVisibility(View.INVISIBLE);
            removeRoomFragment();
            return;
        }
        super.onBackPressed();
    }

}