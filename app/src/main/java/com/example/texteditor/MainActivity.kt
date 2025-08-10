package com.example.texteditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import android.widget.ImageButton


class MainActivity : AppCompatActivity() {

    private lateinit var editor: EditText
    private val OPEN_REQUEST_CODE = 41
    private val CREATE_REQUEST_CODE = 42
    private var currentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        editor = findViewById(R.id.editText)

        findViewById<ImageButton>(R.id.btnOpen).setOnClickListener {
            openFile()
        }

        findViewById<ImageButton>(R.id.btnSave).setOnClickListener {
            if (currentUri != null) {
                saveToUri(currentUri!!)
            } else {
                saveFile()
            }
        }

        findViewById<ImageButton>(R.id.btnNew).setOnClickListener {
            editor.setText("")
            currentUri = null
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, OPEN_REQUEST_CODE)
    }

    private fun saveFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "newfile.txt")
        }
        startActivityForResult(intent, CREATE_REQUEST_CODE)
    }

    private fun saveToUri(uri: Uri) {
        contentResolver.openOutputStream(uri)?.use {
            it.write(editor.text.toString().toByteArray())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                when (requestCode) {
                    OPEN_REQUEST_CODE -> {
                        currentUri = uri
                        val text = readTextFromUri(uri)
                        editor.setText(text)
                    }
                    CREATE_REQUEST_CODE -> {
                        currentUri = uri
                        saveToUri(uri)
                    }
                }
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
    }
}