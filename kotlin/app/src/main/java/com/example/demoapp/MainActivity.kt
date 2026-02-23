package com.example.demoapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.whereby.sdk.*
import androidx.core.view.isVisible
import androidx.activity.addCallback
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // ─────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────

    companion object {
        private const val TAG_ROOM_FRAGMENT = "WHEREBY_ROOM_FRAGMENT"

        /**
         * Replace with your room URL.
         * See https://docs.whereby.com/creating-and-deleting-rooms
         */
        private const val roomUrlString = ""
    }

    // ─────────────────────────────────────────────
    // Views
    // ─────────────────────────────────────────────

    private lateinit var textInput: TextInputEditText
    private lateinit var toggleCameraButton: Button
    private lateinit var toggleMicrophoneButton: Button
    private lateinit var removeFragmentButton: Button
    private lateinit var fullScreenFrameLayout: FrameLayout

    // ─────────────────────────────────────────────
    // Activity lifecycle
    // ─────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initMediaButtons()
        setupClickListeners()
        setupBackNavigation()
    }

    // ─────────────────────────────────────────────
    // UI setup
    // ─────────────────────────────────────────────

    private fun bindViews() {
        textInput = findViewById(R.id.text_input_room_url)
        textInput.setText(roomUrlString)

        toggleMicrophoneButton = findViewById(R.id.button_toggle_microphone)
        toggleCameraButton = findViewById(R.id.button_toggle_camera)
        removeFragmentButton = findViewById(R.id.button_remove_fragment)

        fullScreenFrameLayout = findViewById(R.id.layout_fragment_container_fullscreen)
    }

    private fun setControlsEnabled(enabled: Boolean) {
        toggleMicrophoneButton.isEnabled = enabled
        toggleCameraButton.isEnabled = enabled
    }

    private fun initMediaButtons() {
        toggleCameraButton.setBackgroundColor(Color.GRAY)
        toggleMicrophoneButton.setBackgroundColor(Color.GRAY)
        setControlsEnabled(false)
    }

    private fun setupClickListeners() {
        val loadEmbeddedButton: Button = findViewById(R.id.button_start_embedded)
        val loadFullscreenButton: Button = findViewById(R.id.button_start_fullscreen)

        loadEmbeddedButton.setOnClickListener {
            navigateToRoomFragment(R.id.layout_fragment_container_embedded)
            setControlsEnabled(true)
        }

        loadFullscreenButton.setOnClickListener {
            fullScreenFrameLayout.visibility = View.VISIBLE
            navigateToRoomFragment(R.id.layout_fragment_container_fullscreen)
        }

        toggleCameraButton.setOnClickListener {
            getRoomFragment()?.toggleCameraEnabled()
        }

        toggleMicrophoneButton.setOnClickListener {
            getRoomFragment()?.toggleMicrophoneEnabled()
        }

        removeFragmentButton.setOnClickListener {
            removeRoomFragment()
        }
    }

    // ─────────────────────────────────────────────
    // Fragment navigation
    // ─────────────────────────────────────────────

    private fun navigateToRoomFragment(containerId: Int) {
        setControlsEnabled(false)
        removeRoomFragment()

        val roomConfig = buildWherebyRoomConfig() ?: return

        val fragment = WherebyRoomFragment.newInstance(roomConfig)
        attachWherebyRoomListener(fragment)

        supportFragmentManager
            .beginTransaction()
            .replace(containerId, fragment, TAG_ROOM_FRAGMENT)
            .commit()
    }

    private fun removeRoomFragment() {
        val fm: FragmentManager = supportFragmentManager
        val existing: Fragment? = fm.findFragmentByTag(TAG_ROOM_FRAGMENT)

        if (existing != null) {
            fm.beginTransaction()
                .remove(existing)
                .commit()
        }

        initMediaButtons()
    }

    private fun getRoomFragment(): WherebyRoomFragment? {
        val f = supportFragmentManager.findFragmentByTag(TAG_ROOM_FRAGMENT)
        return f as? WherebyRoomFragment
    }

    // ─────────────────────────────────────────────
    // Whereby room configuration
    // ─────────────────────────────────────────────

    private fun buildWherebyRoomConfig(): WherebyRoomConfig? {
        val roomUrl = textInput.text?.toString()?.trim() ?: return null

        val roomConfig = WherebyRoomConfig(roomUrl)

        // Optional: customize the room before joining the meeting.
        // Comment the following lines to skip room customization.
        roomConfig.displayName = "Name"
        roomConfig.isRoomBackgroundVisible = false
        // ...

        return roomConfig
    }

    // ─────────────────────────────────────────────
    // Whereby events
    // ─────────────────────────────────────────────

    private fun attachWherebyRoomListener(fragment: WherebyRoomFragment) {
        fragment.setListener(object : WherebyRoomListener {

            override fun onRoomReady() {
                runOnUiThread { setControlsEnabled(true) }
            }

            override fun onLocalParticipantKnocked() {
                // ...
            }

            override fun onLocalParticipantJoined() {
                // ...
            }

            override fun onLocalParticipantLeft(isRemoved: Boolean) {
                runOnUiThread { initMediaButtons() }
            }

            override fun onRemoteParticipantJoined(participant: WherebyRoomParticipant) {
                Log.d("onRemoteParticipantJoined", "Participant metadata: ${participant.metadata}")
            }

            override fun onRemoteParticipantLeave(participant: WherebyRoomParticipant) {
                // ...
            }

            override fun onParticipantCountUpdated(count: Int) {
                Log.d("onParticipantCountUpdated", "Count: $count")
            }

            override fun onMicrophoneToggled(enabled: Boolean) {
                setButtonBackgroundColor(toggleMicrophoneButton, enabled)
            }

            override fun onCameraToggled(enabled: Boolean) {
                setButtonBackgroundColor(toggleCameraButton, enabled)
            }

            override fun onError(error: Throwable) {
                Log.e("onError", "Whereby SDK:", error)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Whereby error: " + (error.message ?: error.javaClass.simpleName),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            private fun setButtonBackgroundColor(button: Button, enabled: Boolean) {
                button.setBackgroundColor(
                    if (enabled) ContextCompat.getColor(this@MainActivity, R.color.green)
                    else ContextCompat.getColor(this@MainActivity, R.color.red)                )
            }
        })
    }

    // ─────────────────────────────────────────────
    // Back navigation
    // ─────────────────────────────────────────────

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (fullScreenFrameLayout.isVisible) {
                fullScreenFrameLayout.visibility = View.INVISIBLE
                removeRoomFragment()
            } else {
                // Disable this callback and let the system handle back
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}