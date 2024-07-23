package com.xmlvideoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// need this dependencies to fetch from URL
// implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
// implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
// need this dependencies to play the video
// implementation("androidx.media3:media3-exoplayer:1.3.1")
// implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
// implementation("androidx.media3:media3-ui:1.3.1")

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Use XML layout

        // Initialize ExoPlayer and PlayerView
        initializePlayer()

        fetchAndDisplayMediaFiles("https://dsp.mediacare.com.tw/Vast?screenid=mobile&mode=MS&uuid=E7A7B60A-3BA0-43DB-8B3D-1C2B74A6D050&deviceid=5f3bf9e1f75f92a2")
    }

    private fun initializePlayer() {
        try {
            playerView = findViewById(R.id.player_view)
            if (playerView == null) {
                throw RuntimeException("PlayerView not found in the layout")
            }

            player = ExoPlayer.Builder(this).build()
            playerView.player = player
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            throw RuntimeException("ExoPlayer initialization failed", e)
        }
    }

    private fun fetchAndDisplayMediaFiles(urlString: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val mediaFiles = parseMediaFilesFromUrl(urlString)
                displayFirstMediaFile(mediaFiles)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun displayFirstMediaFile(mediaFiles: List<MediaFile>) {
        val firstMediaFile = mediaFiles.firstOrNull()
        if (firstMediaFile != null) {
            // Update your UI here with the details of the first media file
            println("First MediaFile:")
            println("URL: ${firstMediaFile.url}")
            println("Type: ${firstMediaFile.type}")
            println("Width: ${firstMediaFile.width}")
            println("Height: ${firstMediaFile.height}")

            if (firstMediaFile.url != null) {
                // Play the media file
                playMediaFile(firstMediaFile.url as String)
            }
        } else {
            println("No MediaFile found.")
        }
    }

    private fun playMediaFile(url: String) {
        // Ensure URL is valid
        if (url.isBlank()) {
            throw IllegalArgumentException("Invalid URL")
        }

        // Create a media item
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
