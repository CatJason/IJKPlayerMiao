package com.miao.ijkplayermiao

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.Surface
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException

class IJKPlayerService : Service() {

    private var ijkMediaPlayer: IjkMediaPlayer? = null
    private var mSurface: Surface? = null

    private val mBinder: IJKPlayerServiceAIDL.Stub = object : IJKPlayerServiceAIDL.Stub() {
        override fun setSurface(surface: Surface?) {
            mSurface = surface
            // 在服务中使用Surface，例如传递给IJK播放器
            initializePlayer()
        }

        override fun play() {
            ijkMediaPlayer?.start()
        }

        override fun pause() {
            ijkMediaPlayer?.pause()
        }

        override fun stop() {
            ijkMediaPlayer?.stop()
            ijkMediaPlayer?.reset()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    private fun initializePlayer() {
        if (mSurface != null && ijkMediaPlayer == null) {
            // 初始化 IJK 播放器
            IjkMediaPlayer.loadLibrariesOnce(null)
            IjkMediaPlayer.native_profileBegin("libijkplayer.so")

            // 创建播放器实例
            ijkMediaPlayer = IjkMediaPlayer().apply {
                // 设置准备监听器
                setOnPreparedListener { video -> // 视频已准备好

                }
                // 设置显示界面的 Holder
                setSurface(mSurface)

                try {
                    // 设置播放地址
                    dataSource = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // 播放前准备
                prepareAsync()
                // 开始播放
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 释放播放器资源
        ijkMediaPlayer?.reset()
        ijkMediaPlayer?.release()
        ijkMediaPlayer = null
    }
}