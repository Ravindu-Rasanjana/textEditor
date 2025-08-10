package com.example.texteditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.texteditor.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.collapsingToolbar.title = "SyntaxQuill"

        binding.fabAddFile.setOnClickListener {
            // TODO: Handle new file creation
        }
    }
}
