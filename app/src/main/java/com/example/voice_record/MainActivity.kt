package com.example.voice_record

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val soundVisualizerView: SoundVisualizerView by lazy {
        findViewById<SoundVisualizerView>(R.id.soundVisualizerView)
    }

    private val countUpView: CountUpView by lazy {
        findViewById<CountUpView>(R.id.recordTimeTextView)
    }

    private val recordButton: RecordButton by lazy {
        findViewById<RecordButton>(R.id.recordButton)
    }

    private val resetButton: AppCompatButton by lazy {
        findViewById<AppCompatButton>(R.id.resetButton)
    }

    private val requiredPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    private var recorder: MediaRecorder? = null

    private var player: MediaPlayer? = null

    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            resetButton.isEnabled = value == State.AFTER_RECODING || value == State.ON_PLAYING

            recordButton.updateIconWithState(value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()

        initViews()

        initRecordButton()

        initResetButton()

        soundVisualizerView.onRequestCurrentAmplitude = {recorder?.maxAmplitude ?: 0}

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {

        }
        else {
            Toast.makeText(this, "음성녹음 권한이 반드시 필요합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$packageName"))
            startActivity(intent)
            finish()
        }
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initViews() {
        recordButton.updateIconWithState(state)
    }

    private fun initRecordButton() {
        recordButton.setOnClickListener {
            when(state) {
                State.BEFORE_RECORDING -> startRecording()
                State.ON_RECODING -> stopRecording()
                State.AFTER_RECODING -> startPlaying()
                State.ON_PLAYING -> stopPlaying()
            }
        }
    }

    private fun initResetButton() {
        resetButton.setOnClickListener {
            stopPlaying()
            stopRecording()
            soundVisualizerView.clearVisualization()
            countUpView.clearCountUpView()
            state = State.BEFORE_RECORDING
            initViews()
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            prepare()
            start()
        }
        soundVisualizerView.startVisualizing(false)
        countUpView.startCountUp()
        state = State.ON_RECODING
    }

    private fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null

        soundVisualizerView.stopVisualizing()
        countUpView.stopCountUp()
        state = State.AFTER_RECODING
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare() // 스트리밍 등 Uri를 가져올때 : prepareAsync()
        }
        // 재생이 완료되었을때 호출됨
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECODING
        }
        player?.start()
        soundVisualizerView.startVisualizing(true)
        countUpView.startCountUp()
        state = State.ON_PLAYING
    }

    private fun stopPlaying() {
        player?.release()
        player = null

        soundVisualizerView.stopVisualizing()
        countUpView.stopCountUp()
        state = State.AFTER_RECODING
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}