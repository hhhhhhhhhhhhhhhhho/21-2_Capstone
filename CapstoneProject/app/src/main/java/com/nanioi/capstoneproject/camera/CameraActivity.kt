package com.nanioi.capstoneproject.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nanioi.capstoneproject.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}