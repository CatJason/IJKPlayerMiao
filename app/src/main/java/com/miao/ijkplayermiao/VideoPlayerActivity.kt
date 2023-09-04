package com.miao.ijkplayermiao

import IJKPlayerServiceAIDL
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import tv.danmaku.ijk.media.player.IjkMediaPlayer


class VideoPlayerActivity : AppCompatActivity() {
    private var ijkMediaPlayer: IjkMediaPlayer? = null
    private var surfaceView: SurfaceView? = null
    private var playButton: Button? = null
    private var pauseButton: Button? = null
    private var stopButton: Button? = null
    private var ijkPlayerService: IJKPlayerServiceAIDL? = null
    private var isServiceConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        // 初始化 IJK 播放器
        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.native_profileBegin("libijkplayer.so")
        surfaceView = findViewById(R.id.surface_view)

        // 获取屏幕宽度
        // 获取屏幕宽度
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        playButton = findViewById<Button>(R.id.play_button).apply {
            setOnClickListener {
                onPlayButtonClick()
            }
        }
        pauseButton = findViewById<Button>(R.id.pause_button).apply {
            setOnClickListener {
                onPauseButtonClick()
            }
        }
        stopButton = findViewById<Button>(R.id.stop_button).apply {
            setOnClickListener {
                onStopButtonClick()
            }
        }

        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // Do nothing in surfaceCreated
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                if(isServiceConnected){
                    ijkPlayerService?.setSurface(surfaceView!!.holder.surface)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // Do nothing in surfaceDestroyed
            }
        })

        // 绑定IJKPlayerService
        val serviceIntent = Intent(this, IJKPlayerService::class.java)
        bindService(serviceIntent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                isServiceConnected = true
                ijkPlayerService = IJKPlayerServiceAIDL.Stub.asInterface(service)
                if (surfaceView?.holder?.surface != null) {
                    ijkPlayerService?.setSurface(surfaceView!!.holder.surface)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                ijkPlayerService = null
                isServiceConnected = false
            }
        }, Context.BIND_AUTO_CREATE)
    }


    private fun onPlayButtonClick() {
        // 开始播放
        if (!ijkMediaPlayer!!.isPlaying) {
            ijkMediaPlayer!!.start()
        }
    }

    private fun onPauseButtonClick() {
        // 暂停播放
        if (ijkMediaPlayer!!.isPlaying) {
            ijkMediaPlayer!!.pause()
        }
    }

    private fun onStopButtonClick() {
        // 停止播放
        if (ijkMediaPlayer!!.isPlaying) {
            ijkMediaPlayer!!.stop()
            ijkMediaPlayer!!.reset() // 重置播放器以便重新播放
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 释放资源
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer!!.release()
            ijkMediaPlayer = null
        }
        IjkMediaPlayer.native_profileEnd()
    }

}
