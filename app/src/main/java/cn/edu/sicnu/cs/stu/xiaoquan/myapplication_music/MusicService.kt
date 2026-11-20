package cn.edu.sicnu.cs.stu.xiaoquan.myapplication_music

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class MusicService : android.app.Service() {
    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var listener: OnProgressUpdateListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if(it.isPlaying){
                    listener?.onProgressUpdate(it.currentPosition, it.duration)
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    interface OnProgressUpdateListener {
        fun onProgressUpdate(current: Int, total: Int)
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    fun setOnProgressUpdateListener(l: OnProgressUpdateListener?) {
        listener = l
    }

    fun play() {
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, R.raw.test)
            mediaPlayer?.setOnPreparedListener {
                it.start()
                handler.post(progressRunnable)
            }
            mediaPlayer?.setOnCompletionListener {
                handler.removeCallbacks(progressRunnable)
            }
        }else{
            mediaPlayer?.start()
            handler.post(progressRunnable)
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stopPlay() {
        handler.removeCallbacks(progressRunnable)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun prev() {
        mediaPlayer?.seekTo(0)
        mediaPlayer?.start()
    }

    fun next() {
        mediaPlayer?.seekTo(0)
        mediaPlayer?.start()
    }

    fun seekTo(pos: Int) {
        mediaPlayer?.seekTo(pos)
    }

    fun getCurrentSongName(): String {
        return "测试歌曲"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel("music", "音乐通知", NotificationManager.IMPORTANCE_LOW)
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(chan)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
