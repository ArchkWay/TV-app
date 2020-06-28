package com.example.tvapplication

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.tvapplication.movies.Movie
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_play.*


class PlaybackActivity : FragmentActivity() {

    lateinit var player: ExoPlayer
    lateinit var url: String
    private var mediaDataSourceFactory: DataSource.Factory? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        playerView.requestFocus()
        url = (intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie).videoUrl ?: ""
        preparePlayer(url)
    }

    private fun preparePlayer(videoUrl: String) {
        val trackSelector = DefaultTrackSelector()
        val loadControl = DefaultLoadControl()
        val renderersFactory = DefaultRenderersFactory(this)
        mediaDataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, "TV")
        )
        player = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector, loadControl)
        playerView.player = player;
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        val mediaSource = ExtractorMediaSource
            .Factory(DefaultDataSourceFactory(this, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(Uri.parse(videoUrl))
        val subtitleUrl = (intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie).subtitleUrl ?: ""
        println(subtitleUrl)
        val subtitleSource = SingleSampleMediaSource(
            Uri.parse(subtitleUrl),
            mediaDataSourceFactory,
            Format.createTextSampleFormat(null, MimeTypes.TEXT_VTT, Format.NO_VALUE, "en", null),
            C.TIME_UNSET
        )
        val mergedSource = MergingMediaSource(mediaSource, subtitleSource)
        player.prepare(mergedSource)
        playerView.subtitleView?.alpha = 0.6f
        player.playWhenReady = true
    }


    override fun onResume() {
       if(player.isPlaying) preparePlayer(url)
        super.onResume()
    }


    override fun onPause() {
        player.playWhenReady = false
        player.stop()
        player.seekTo(0)
        super.onPause()
    }

}