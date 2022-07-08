package com.whereby.demoapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.whereby.sdk.WherebyRoomFragment
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.whereby.demoapp.R
import com.whereby.sdk.WherebyConstants
import com.whereby.sdk.WherebyRoomConfig
import com.whereby.sdk.WherebyEventListener
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    /**
     * Replace with your room URL.
     * See https://docs.whereby.com/creating-and-deleting-rooms
     */
    private val mRoomUrlString = ""
    private var mToggleCameraButton: Button? = null
    private var mToggleMicrophoneButton: Button? = null
    private var mRoomFragment: WherebyRoomFragment? = null
    private var mIsPresentingFullScreen = false

    //region Activity lifecycle
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startEmbeddedButton = findViewById<Button>(R.id.button_start_embedded)
        val startFullScreenButton = findViewById<Button>(R.id.button_start_fullscreen)
        mToggleCameraButton = findViewById(R.id.button_toggle_camera)
        mToggleMicrophoneButton = findViewById(R.id.button_toggle_microphone)
        val removeFragmentButton = findViewById<Button>(R.id.button_remove_fragment)
        startEmbeddedButton.setOnClickListener {
            embedInFragment(
                R.id.layout_fragment_container_embedded,
                false
            )
        }
        startFullScreenButton.setOnClickListener {
            embedInFragment(
                R.id.layout_fragment_container_fullscreen,
                true
            )
        }
        mToggleCameraButton!!.setOnClickListener(View.OnClickListener { mRoomFragment!!.toggleCameraEnabled() })
        mToggleMicrophoneButton!!.setOnClickListener(View.OnClickListener { mRoomFragment!!.toggleMicrophoneEnabled() })
        removeFragmentButton.setOnClickListener { removeRoomFragment() }
        initMediaButtons()
    }

    override fun onBackPressed() {
        if (mIsPresentingFullScreen) {
            mIsPresentingFullScreen = false
            removeRoomFragment()
        } else {
            super.onBackPressed()
        }
    }
    //endregion

    //region private
    private fun embedInFragment(frameLayout: Int, isFullScreen: Boolean) {
        mIsPresentingFullScreen = isFullScreen
        initMediaButtons()
        if (mRoomFragment != null) {
            removeRoomFragment()
        }
        mRoomFragment = WherebyRoomFragment()
        val bundle = Bundle()
        bundle.putSerializable(WherebyConstants.ROOM_CONFIG_KEY, createWherebyRoomConfig())
        mRoomFragment!!.arguments = bundle

        // Optional: this allows to receive async events during the meeting when using the room fragment, by implementing the
        // WherebyEventListener methods.
        // Comment the following line to disable.
        setRoomFragmentEventListener()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(frameLayout, mRoomFragment!!)
        fragmentTransaction.commit()
        mRoomFragment!!.join()
    }

    private fun createWherebyRoomConfig(): WherebyRoomConfig {
        val roomConfig = WherebyRoomConfig(createRoomUrl()!!)

        // Optional: customize the room before joining the meeting.
        // Comment the following lines to skip room customization.
        roomConfig.isMicrophoneEnabledAtStart = false
        roomConfig.isCameraEnabledAtStart = true
        roomConfig.displayName = "Participant name"
        //...
        return roomConfig
    }

    private fun createRoomUrl(): URL? {
        var roomURL: URL? = null
        try {
            roomURL = URL(mRoomUrlString)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return roomURL
    }

    private fun setRoomFragmentEventListener() {
        mRoomFragment!!.setEventListener(object : WherebyEventListener {
            // All the methods below are optional:
            override fun onRoomReady() {
                runOnUiThread {
                    mToggleCameraButton!!.isEnabled = true
                    mToggleMicrophoneButton!!.isEnabled = true
                }
            }

            override fun onMicrophoneToggled(enabled: Boolean) {
                setButtonBackgroundColor(mToggleMicrophoneButton, enabled)
            }

            override fun onCameraToggled(enabled: Boolean) {
                setButtonBackgroundColor(mToggleCameraButton, enabled)
            }

            // Helper:
            private fun setButtonBackgroundColor(button: Button?, enabled: Boolean) {
                button!!.setBackgroundColor(
                    if (enabled) resources.getColor(R.color.green) else resources.getColor(
                        R.color.red
                    )
                )
            }

            override fun onLocalParticipantLeft(isRemoved: Boolean) {
                runOnUiThread { initMediaButtons() }
            } //...
        })
    }

    private fun removeRoomFragment() {
        initMediaButtons()
        if (mRoomFragment == null) {
            return
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(mRoomFragment!!)
        fragmentTransaction.commit()
        mRoomFragment = null
    }

    private fun initMediaButtons() {
        mToggleCameraButton!!.setBackgroundColor(Color.GRAY)
        mToggleMicrophoneButton!!.setBackgroundColor(Color.GRAY)
        mToggleCameraButton!!.isEnabled = false
        mToggleMicrophoneButton!!.isEnabled = false
    } 
    //endregion
}