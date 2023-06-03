package io.github.wykopmobilny.ui.modules.embedview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout.LayoutParams
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.ErrorReason
import com.google.android.youtube.player.YouTubePlayerView
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.WykopApp
import io.github.wykopmobilny.utils.youtubeTimestampToMsOrNull
import java.net.URLDecoder

object YouTubeUrlParser {
    private val consentRegex = "consent.youtube.com/.+[?&]continue=([a-z0-9-_.~%]+[^&\\n])".toRegex(RegexOption.IGNORE_CASE)
    private val videoRegex =
        "(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/\\S+/|(?:v|e(?:mbed)?)/|\\S*?[?&]v=)|youtu\\.be/)([a-z0-9_-]{11})".toRegex(
            RegexOption.IGNORE_CASE,
        )
    private val timestampRegex = "t=([^#&\\n\\r]+)".toRegex(RegexOption.IGNORE_CASE)

    fun getVideoId(videoUrl: String): String? {
        val unwrappedUrl = unwrapConsentYoutubeUrl(videoUrl)
        return findInUrl(videoRegex, unwrappedUrl)
    }

    fun getTimestamp(videoUrl: String): String? {
        return findInUrl(timestampRegex, videoUrl)
    }

    fun getVideoUrl(videoId: String): String {
        return "http://youtu.be/$videoId"
    }

    fun isVideoUrl(url: String): Boolean {
        return videoRegex.find(unwrapConsentYoutubeUrl(url)) != null
    }

    private fun findInUrl(regex: Regex, url: String): String? {
        val match = regex.find(url)
        return match?.groupValues?.get(1)
    }

    private fun unwrapConsentYoutubeUrl(url: String): String {
        val match = consentRegex.find(url) ?: return url
        return URLDecoder.decode(match.groupValues[1], "utf-8")
    }
}

object StatusBarUtil {

    fun hide(activity: Activity) {
        val decorView = activity.window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }
}

object AudioUtil {

    private val mSingletonLock = Any()
    private var audioManager: AudioManager? = null

    private fun getInstance(context: Context?): AudioManager? {
        synchronized(mSingletonLock) {
            if (audioManager != null) {
                return audioManager
            }
            if (context != null) {
                audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }
            return audioManager
        }
    }

    fun adjustMusicVolume(context: Context, up: Boolean, showInterface: Boolean) {
        val direction = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        val flag = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or if (showInterface) AudioManager.FLAG_SHOW_UI else 0
        getInstance(context)!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, flag)
    }
}

enum class Orientation {
    AUTO,
    AUTO_START_WITH_LANDSCAPE,
    ONLY_LANDSCAPE,
    ONLY_PORTRAIT,
}

class YTPlayer :
    YouTubeBaseActivity(),
    YouTubePlayer.OnInitializedListener,
    YouTubePlayer.OnFullscreenListener,
    YouTubePlayer.PlayerStateChangeListener {

    private var videoId: String? = null
    private var timestampMs: Long? = null

    private var playerStyle: YouTubePlayer.PlayerStyle? = null
    private var orientation: Orientation? = null
    private var showAudioUi: Boolean = false
    private var handleError: Boolean = false
    private var animEnter: Int = 0
    private var animExit: Int = 0

    private lateinit var playerView: YouTubePlayerView
    private var player: YouTubePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()

        playerView = YouTubePlayerView(this)
        playerView.initialize(findYoutubeApiKey(), this)

        addContentView(playerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        playerView.setBackgroundResource(android.R.color.black)

        StatusBarUtil.hide(this)
    }

    private fun initialize() {
        videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
        if (videoId == null) {
            throw NullPointerException("Video ID must not be null")
        }

        playerStyle = YouTubePlayer.PlayerStyle.DEFAULT
        orientation = Orientation.AUTO
        timestampMs = intent.getStringExtra(EXTRA_TIMESTAMP)?.youtubeTimestampToMsOrNull()
        showAudioUi = intent.getBooleanExtra(EXTRA_SHOW_AUDIO_UI, true)
        handleError = intent.getBooleanExtra(EXTRA_HANDLE_ERROR, true)
        animEnter = intent.getIntExtra(EXTRA_ANIM_ENTER, 0)
        animExit = intent.getIntExtra(EXTRA_ANIM_EXIT, 0)
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider, player: YouTubePlayer, wasRestored: Boolean) {
        this.player = player
        player.setOnFullscreenListener(this)
        player.setPlayerStateChangeListener(this)

        when (orientation) {
            Orientation.AUTO -> player.fullscreenControlFlags = (
                YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                    or YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                    or YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
                    or YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT
                )
            Orientation.AUTO_START_WITH_LANDSCAPE -> {
                player.fullscreenControlFlags = (
                    YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                        or YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                        or YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
                        or YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT
                    )
                player.setFullscreen(true)
            }
            Orientation.ONLY_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                player.fullscreenControlFlags =
                    YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI or YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT
                player.setFullscreen(true)
            }
            Orientation.ONLY_PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                player.fullscreenControlFlags =
                    YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI or YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT
                player.setFullscreen(true)
            }
            else -> Unit
        }

        when (playerStyle) {
            YouTubePlayer.PlayerStyle.CHROMELESS -> player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
            YouTubePlayer.PlayerStyle.MINIMAL -> player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
            YouTubePlayer.PlayerStyle.DEFAULT -> player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
            else -> player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
        }

        if (!wasRestored) {
            player.loadVideo(videoId, timestampMs?.toInt() ?: 0)
        }
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider, errorReason: YouTubeInitializationResult) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()
        } else {
            val errorMessage = "There was an error initializing the YouTubePlayer ($errorReason)"
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            playerView.initialize(findYoutubeApiKey(), this)
        }
    }

    private fun findYoutubeApiKey() = (applicationContext as WykopApp).appConfig.youtubeKey

    // YouTubePlayer.OnFullscreenListener
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runCatching {
            when (orientation) {
                Orientation.AUTO, Orientation.AUTO_START_WITH_LANDSCAPE ->
                    when (newConfig.orientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> player?.setFullscreen(true)
                        Configuration.ORIENTATION_PORTRAIT -> player?.setFullscreen(true)
                        else -> Unit
                    }
                Orientation.ONLY_LANDSCAPE,
                Orientation.ONLY_PORTRAIT,
                null,
                -> Unit
            }
        }
            .onFailure { Napier.i("onConfigurationChanged failed", it) }
    }

    override fun onFullscreen(fullScreen: Boolean) {
        when (orientation) {
            Orientation.AUTO, Orientation.AUTO_START_WITH_LANDSCAPE ->
                requestedOrientation = if (fullScreen) {
                    LANDSCAPE_ORIENTATION
                } else {
                    PORTRAIT_ORIENTATION
                }
            Orientation.ONLY_LANDSCAPE, Orientation.ONLY_PORTRAIT -> Unit
            else -> Unit
        }
    }

    // YouTubePlayer.PlayerStateChangeListener
    override fun onError(reason: ErrorReason) {
        Napier.i("onError : " + reason.name)
        if (ErrorReason.NOT_PLAYABLE == reason) {
            val videoUri = Uri.parse(YouTubeUrlParser.getVideoUrl(videoId!!))
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
                .takeIf { packageManager.queryIntentActivities(it, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty() }
                ?: Intent(Intent.ACTION_VIEW, videoUri)

            if (handleError) {
                startActivity(intent)
            }
            intent.putExtra(EXTRA_HANDLE_ERROR, false)
            handleError = false
        }
    }

    override fun onAdStarted() = Unit

    override fun onLoaded(videoId: String) = Unit

    override fun onLoading() = Unit

    override fun onVideoEnded() = Unit

    override fun onVideoStarted() {
        StatusBarUtil.hide(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    // Audio Managing
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            AudioUtil.adjustMusicVolume(applicationContext, true, showAudioUi)
            StatusBarUtil.hide(this)
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            AudioUtil.adjustMusicVolume(applicationContext, false, showAudioUi)
            StatusBarUtil.hide(this)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Animation
    override fun onBackPressed() {
        super.onBackPressed()
        if (animEnter != 0 && animExit != 0) {
            overridePendingTransition(animEnter, animExit)
        }
    }

    companion object {
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_SHOW_AUDIO_UI = "show_audio_ui"
        const val EXTRA_HANDLE_ERROR = "handle_error"
        const val EXTRA_ANIM_ENTER = "anim_enter"
        const val EXTRA_ANIM_EXIT = "anim_exit"

        private const val RECOVERY_DIALOG_REQUEST = 1
        private const val PORTRAIT_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        private const val LANDSCAPE_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
}
