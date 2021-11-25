package com.nanioi.closetapplication.User

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.databinding.ActivityCautionBinding
import com.nanioi.closetapplication.databinding.ActivitySignUpBinding

class CautionActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCautionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caution)
    }
}