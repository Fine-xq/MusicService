package cn.edu.sicnu.cs.stu.xiaoquan.myapplication_music

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var seekBar: SeekBar
    private lateinit var tvSongName: TextView

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.setOnProgressUpdateListener(object : MusicService.OnProgressUpdateListener {
                override fun onProgressUpdate(current: Int, total: Int) {
                    runOnUiThread {
                        seekBar.max = total
                        seekBar.progress = current
                        tvSongName.text = musicService?.getCurrentSongName()
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seekBar = findViewById(R.id.seekBar)
        tvSongName = findViewById(R.id.tvSongName)
        val btnPlay: Button = findViewById(R.id.btnPlay)
        val btnPause: Button = findViewById(R.id.btnPause)
        val btnStop: Button = findViewById(R.id.btnStop)
        val btnPrev: Button = findViewById(R.id.btnPrev)
        val btnNext: Button = findViewById(R.id.btnNext)

        requestPermission()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)

        btnPlay.setOnClickListener { if (isBound) musicService?.play() }
        btnPause.setOnClickListener { if (isBound) musicService?.pause() }
        btnStop.setOnClickListener { if (isBound) musicService?.stopPlay() }
        btnPrev.setOnClickListener { if (isBound) musicService?.prev() }
        btnNext.setOnClickListener { if (isBound) musicService?.next() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                if (isBound) musicService?.seekTo(sb?.progress ?: 0)
            }
        })
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) unbindService(conn)
    }
}
