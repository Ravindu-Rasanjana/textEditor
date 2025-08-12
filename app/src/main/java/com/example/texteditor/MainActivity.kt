package com.example.texteditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.texteditor.ui.theme.TextEditorTheme

class MainActivity : ComponentActivity() {

    private var currentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editorText = mutableStateOf("")
        val fileName = mutableStateOf("Untitled.txt")

        val openFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    currentUri = uri
                    editorText.value = readTextFromUri(uri)
                    fileName.value = uri.lastPathSegment ?: "OpenedFile.txt"
                }
            }
        }

        val createFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    currentUri = uri
                    saveToUri(uri, editorText.value)
                    fileName.value = uri.lastPathSegment ?: "SavedFile.txt"
                }
            }
        }

        setContent {
            TextEditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF111111)
                ) {
                    EditorScreen(
                        textState = editorText,
                        fileName = fileName,
                        onNew = {
                            editorText.value = ""
                            currentUri = null
                            fileName.value = "Untitled.txt"
                        },
                        onOpen = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            openFileLauncher.launch(intent)
                        },
                        onSave = {
                            if (currentUri != null) {
                                saveToUri(currentUri!!, editorText.value)
                            } else {
                                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TITLE, "newfile.txt")
                                }
                                createFileLauncher.launch(intent)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader()
            .use { it?.readText() } ?: ""
    }

    private fun saveToUri(uri: Uri, text: String) {
        contentResolver.openOutputStream(uri)?.use {
            it.write(text.toByteArray())
        }
    }
}

@Composable
fun EditorScreen(
    textState: MutableState<String>,
    fileName: MutableState<String>,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit
) {
    val wordCount = textState.value.trim()
        .split("\\s+".toRegex())
        .filter { it.isNotEmpty() }
        .size

    val charCount = textState.value.length

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top row buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onNew) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_new_file),
                    contentDescription = "New File",
                    tint = Color.White
                )
            }
            IconButton(onClick = onOpen) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_open_file),
                    contentDescription = "Open File",
                    tint = Color.White
                )
            }
            IconButton(onClick = onSave) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save_file),
                    contentDescription = "Save File",
                    tint = Color.White
                )
            }
        }

        // File name
        Text(
            text = fileName.value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Word & character count row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Words: $wordCount", color = Color.Gray, fontSize = 14.sp)
            Text(text = "Characters: $charCount", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text editor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .imePadding()
        ) {
            BasicTextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
            )
        }
    }
}
