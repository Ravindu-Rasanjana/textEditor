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

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import java.util.regex.Pattern

import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class SyntaxHighlighter(private val context: Context) {

    // Enhanced color scheme for white background
    private val keywordColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark)     // Keywords: deep blue
    private val stringColor = ContextCompat.getColor(context, android.R.color.holo_green_dark)     // Strings: dark green
    private val commentColor = ContextCompat.getColor(context, android.R.color.darker_gray)       // Comments: gray
    private val numberColor = ContextCompat.getColor(context, android.R.color.holo_orange_dark)   // Numbers: dark orange
    private val functionColor = ContextCompat.getColor(context, android.R.color.holo_purple)      // Functions: purple
    private val operatorColor = ContextCompat.getColor(context, android.R.color.white)            // Operators: black for clarity


    fun applySyntaxHighlighting(editText: EditText, fileName: String) {
        val language = detectLanguageFromFileName(fileName)
        val text = editText.text.toString()
        val spannable = editText.text as Spannable

        // Clear existing spans
        val spans = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
        for (span in spans) {
            spannable.removeSpan(span)
        }

        when (language.lowercase()) {
            "kotlin" -> highlightKotlin(spannable, text)
            "java" -> highlightJava(spannable, text)
            "python" -> highlightPython(spannable, text)
            "c" -> highlightC(spannable, text)
            "cpp" -> highlightCpp(spannable, text)
            "javascript" -> highlightJavaScript(spannable, text)
            else -> highlightGeneric(spannable, text)
        }
    }

    private fun detectLanguageFromFileName(fileName: String): String {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "kt" -> "kotlin"
            "java" -> "java"
            "py", "pyw" -> "python"
            "c", "h" -> "c"
            "cpp", "cxx", "cc", "hpp" -> "cpp"
            "js", "mjs" -> "javascript"
            "html", "htm" -> "html"
            "css" -> "css"
            "xml" -> "xml"
            "json" -> "json"
            else -> "plain"
        }
    }

    private fun highlightKotlin(spannable: Spannable, text: String) {
        val keywords = context.resources.getStringArray(R.array.kotlin_keywords)
        highlightKeywords(spannable, text, keywords)
        highlightStrings(spannable, text)
        highlightComments(spannable, text, "//", "/*", "*/")
        highlightNumbers(spannable, text)
        highlightFunctions(spannable, text, "fun\\s+(\\w+)")
        highlightOperators(spannable, text)
    }

    private fun highlightJava(spannable: Spannable, text: String) {
        val keywords = context.resources.getStringArray(R.array.java_keywords)
        highlightKeywords(spannable, text, keywords)
        highlightStrings(spannable, text)
        highlightComments(spannable, text, "//", "/*", "*/")
        highlightNumbers(spannable, text)
        highlightFunctions(spannable, text, "(public|private|protected)?\\s*(static)?\\s*\\w+\\s+(\\w+)\\s*\\(")
        highlightOperators(spannable, text)
    }

    private fun highlightPython(spannable: Spannable, text: String) {
        val keywords = context.resources.getStringArray(R.array.python_keywords)
        highlightKeywords(spannable, text, keywords)
        highlightStrings(spannable, text)
        highlightComments(spannable, text, "#")
        highlightNumbers(spannable, text)
        highlightFunctions(spannable, text, "def\\s+(\\w+)")
        highlightOperators(spannable, text)
    }

    private fun highlightC(spannable: Spannable, text: String) {
        val keywords = arrayOf("int", "float", "double", "char", "void", "if", "else", "for", "while", "return", "include", "stdio", "main", "printf", "scanf")
        highlightKeywords(spannable, text, keywords)
        highlightStrings(spannable, text)
        highlightComments(spannable, text, "//", "/*", "*/")
        highlightNumbers(spannable, text)
        highlightFunctions(spannable, text, "\\w+\\s+(\\w+)\\s*\\(")
        highlightOperators(spannable, text)
    }

    private fun highlightCpp(spannable: Spannable, text: String) {
        val keywords = arrayOf("int", "float", "double", "char", "void", "if", "else", "for", "while", "return", "class", "public", "private", "protected", "namespace", "using", "std", "cout", "cin", "endl")
        highlightKeywords(spannable, text, keywords)
        highlightStrings(spannable, text)
        highlightComments(spannable, text, "//", "/*", "*/")
        highlightNumbers(spannable, text)
        highlightFunctions(spannable, text, "\\w+\\s+(\\w+)\\s*\\(")
        highlightOperators(spannable, text)
    }

    private fun highlightJavaScript(spannable: Spannable, text: String) {
        val keywords = arrayOf("function", "var", "let", "const", "if", "else", "for", "while", "return", "class", "extends", "import", "export", "async", "await", "true", "false", "null", "undefined")
        highlightKeywords(spannable, text, keywords)
        highlightStrings(spannable, text)
        highlightComments(spannable, text, "//", "/*", "*/")
        highlightNumbers(spannable, text)
        highlightFunctions(spannable, text, "function\\s+(\\w+)")
        highlightOperators(spannable, text)
    }

    private fun highlightGeneric(spannable: Spannable, text: String) {
        // Basic highlighting for unknown file types
        highlightStrings(spannable, text)
        highlightNumbers(spannable, text)
    }

    private fun highlightKeywords(spannable: Spannable, text: String, keywords: Array<String>) {
        for (keyword in keywords) {
            val pattern = Pattern.compile("\\b$keyword\\b")
            val matcher = pattern.matcher(text)

            while (matcher.find()) {
                spannable.setSpan(
                    ForegroundColorSpan(keywordColor),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun highlightStrings(spannable: Spannable, text: String) {
        // Triple quotes first (for Python docstrings)
        val tripleQuotePattern = Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''")
        val tripleQuoteMatcher = tripleQuotePattern.matcher(text)
        while (tripleQuoteMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(stringColor),
                tripleQuoteMatcher.start(),
                tripleQuoteMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Double quotes
        val doubleQuotePattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"")
        val doubleQuoteMatcher = doubleQuotePattern.matcher(text)
        while (doubleQuoteMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(stringColor),
                doubleQuoteMatcher.start(),
                doubleQuoteMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Single quotes
        val singleQuotePattern = Pattern.compile("'([^'\\\\]|\\\\.)*'")
        val singleQuoteMatcher = singleQuotePattern.matcher(text)
        while (singleQuoteMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(stringColor),
                singleQuoteMatcher.start(),
                singleQuoteMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun highlightNumbers(spannable: Spannable, text: String) {
        val numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?[fFdD]?\\b")
        val numberMatcher = numberPattern.matcher(text)
        while (numberMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(numberColor),
                numberMatcher.start(),
                numberMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun highlightFunctions(spannable: Spannable, text: String, functionPattern: String) {
        val pattern = Pattern.compile(functionPattern)
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val functionNameGroup = if (matcher.groupCount() >= 1) 1 else 0
            val start = if (functionNameGroup > 0) matcher.start(functionNameGroup) else matcher.start()
            val end = if (functionNameGroup > 0) matcher.end(functionNameGroup) else matcher.end()

            spannable.setSpan(
                ForegroundColorSpan(functionColor),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun highlightOperators(spannable: Spannable, text: String) {
        val operatorPattern = Pattern.compile("[+\\-*/=<>!&|%^~]")
        val operatorMatcher = operatorPattern.matcher(text)
        while (operatorMatcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(operatorColor),
                operatorMatcher.start(),
                operatorMatcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun highlightComments(spannable: Spannable, text: String, vararg commentStyles: String) {
        for (i in commentStyles.indices) {
            when {
                commentStyles[i] == "//" || commentStyles[i] == "#" -> {
                    // Single line comments
                    val pattern = Pattern.compile("${Pattern.quote(commentStyles[i])}.*$", Pattern.MULTILINE)
                    val matcher = pattern.matcher(text)
                    while (matcher.find()) {
                        spannable.setSpan(
                            ForegroundColorSpan(commentColor),
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                commentStyles[i] == "/*" && i + 1 < commentStyles.size && commentStyles[i + 1] == "*/" -> {
                    // Multi-line comments
                    val pattern = Pattern.compile("/\\*[\\s\\S]*?\\*/")
                    val matcher = pattern.matcher(text)
                    while (matcher.find()) {
                        spannable.setSpan(
                            ForegroundColorSpan(commentColor),
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
    }
}

class UndoRedoHelper(private val editText: EditText) { // Undo/Redo helper
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private var isUndoOrRedo = false

    init {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isUndoOrRedo) {
                    undoStack.addFirst(s.toString())  // <-- changed from push()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun undo() { // Undo
        if (undoStack.isNotEmpty()) {
            val text = undoStack.removeFirst()  // <-- changed from pop()
            redoStack.addFirst(editText.text.toString())  // <-- changed from push()
            isUndoOrRedo = true
            editText.setText(text)
            editText.setSelection(text.length)
            isUndoOrRedo = false
        }
    }

    fun redo() { // Redo
        if (redoStack.isNotEmpty()) {
            val text = redoStack.removeFirst()  // <-- changed from pop()
            undoStack.addFirst(editText.text.toString())  // <-- changed from push()
            isUndoOrRedo = true
            editText.setText(text)
            editText.setSelection(text.length)
            isUndoOrRedo = false
        }
    }
}
 //new end


class MainActivity : AppCompatActivity() {

    private lateinit var undoRedoHelper: UndoRedoHelper // for UndoRedo
    private lateinit var wordCountText: TextView // For word count

    private lateinit var editor: EditText
    private lateinit var syntaxHighlighter: SyntaxHighlighter
    private lateinit var fileNameTextView: TextView
    private lateinit var compileButton: ImageButton
    private lateinit var outputSlider: View
    private lateinit var outputText: TextView
    private var isOutputVisible = false
    private val OPEN_REQUEST_CODE = 41
    private val CREATE_REQUEST_CODE = 42
    private var currentUri: Uri? = null
    private var currentLanguage: String = "kotlin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Lock orientation to portrait
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_main)

        editor = findViewById(R.id.editText)
        undoRedoHelper = UndoRedoHelper(editor) //new
        wordCountText = findViewById(R.id.wordCountText) //new
        fileNameTextView = findViewById(R.id.tvFileName)
        compileButton = findViewById(R.id.btnCompile)
        outputSlider = findViewById(R.id.outputSlider)
        outputText = findViewById(R.id.outputText)
        syntaxHighlighter = SyntaxHighlighter(this)

        // Add real-time syntax highlighting
        editor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Remove the text watcher temporarily to avoid infinite loop
                editor.removeTextChangedListener(this)
                val fileName = fileNameTextView.text.toString()
                syntaxHighlighter.applySyntaxHighlighting(editor, fileName)
                editor.addTextChangedListener(this)

                val text = s.toString() //new start (for word count)
                val wordCount = if (text.trim().isEmpty()) 0 else text.trim().split("\\s+".toRegex()).size
                val charCount = text.length
                wordCountText.text = "Words: $wordCount  Characters: $charCount" //new finish

            }
        })

        findViewById<ImageButton>(R.id.btnOpen).setOnClickListener {
            openFile()
        }

        findViewById<ImageButton>(R.id.btnSave).setOnClickListener {
            if (currentUri != null) {
                saveToUri(currentUri!!)
                Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                saveFile()
                // Can't show toast here yet because file is not created
            }
        }


        findViewById<ImageButton>(R.id.btnNew).setOnClickListener {
            editor.setText("")
            currentUri = null
            fileNameTextView.text = "Untitled.txt"
            currentLanguage = "kotlin"
        }

        findViewById<ImageButton>(R.id.btnCompile).setOnClickListener {
            toggleOutputSlider()
        }

        findViewById<ImageButton>(R.id.btnUndo).setOnClickListener { //new Undo
            undoRedoHelper.undo()
        }

        findViewById<ImageButton>(R.id.btnRedo).setOnClickListener { //new Redo
            undoRedoHelper.redo()
        }

        findViewById<ImageButton>(R.id.btnFindReplace).setOnClickListener { //new Find and Replace
            showFindReplaceDialog()
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
        val defaultName = when (currentLanguage) {
            "kotlin" -> "newfile.kt"
            "java" -> "NewFile.java"
            "python" -> "newfile.py"
            else -> "newfile.txt"
        }

        val mimeType = when (currentLanguage) {
            "kotlin" -> "text/x-kotlin"
            "java" -> "text/x-java-source"
            "python" -> "text/x-python"
            else -> "text/plain"
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, defaultName)
            // This helps prevent adding extra extensions
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(intent, CREATE_REQUEST_CODE)
    }

    private fun saveToUri(uri: Uri) {
        contentResolver.openOutputStream(uri)?.use {
            it.write(editor.text.toString().toByteArray())
        }
    }

    private fun detectLanguageFromUri(uri: Uri): String {
        val fileName = uri.lastPathSegment ?: ""
        return when {
            fileName.endsWith(".kt", ignoreCase = true) -> "kotlin"
            fileName.endsWith(".java", ignoreCase = true) -> "java"
            fileName.endsWith(".py", ignoreCase = true) -> "python"
            else -> "kotlin" // default
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                when (requestCode) {
                    OPEN_REQUEST_CODE -> {
                        currentUri = uri
                        currentLanguage = detectLanguageFromUri(uri)
                        val fileName = getFileNameFromUri(uri)
                        fileNameTextView.text = fileName
                        val text = readTextFromUri(uri)

                        // Set text first
                        editor.setText(text)

                        // Apply syntax highlighting after setting text
                        editor.post {
                            syntaxHighlighter.applySyntaxHighlighting(editor, currentLanguage)
                        }
                    }
                    CREATE_REQUEST_CODE -> {
                        currentUri = uri
                        val fileName = getFileNameFromUri(uri)
                        fileNameTextView.text = fileName
                        saveToUri(uri)

                        // Toast
                        Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }


    private fun getFileNameFromUri(uri: Uri): String {
        // Try to get display name first
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    val displayName = it.getString(displayNameIndex)
                    if (!displayName.isNullOrBlank()) {
                        return displayName
                    }
                }
            }
        }

        // Fallback to last path segment
        return uri.lastPathSegment ?: "Untitled.txt"
    }

    private fun toggleOutputSlider() {
        if (isOutputVisible) {
            outputSlider.visibility = View.GONE
            isOutputVisible = false
        } else {
            outputSlider.visibility = View.VISIBLE
            isOutputVisible = true
            compileAndRun()
        }
    }

    private fun compileAndRun() {
        outputText.text = "Compiling..."

        try {
            // Create codes directory if it doesn't exist
            val codesDir = File(getExternalFilesDir(null), "codes")
            if (!codesDir.exists()) {
                codesDir.mkdirs()
            }

            // Get current filename or generate one
            val currentFileName = if (currentUri != null) {
                getFileNameFromUri(currentUri!!)
            } else {
                val extension = when (currentLanguage) {
                    "kotlin" -> ".kt"
                    "java" -> ".java"
                    "python" -> ".py"
                    else -> ".txt"
                }
                "temp$extension"
            }

            // Save current code to file
            val codeFile = File(codesDir, currentFileName)
            codeFile.writeText(editor.text.toString())

            // Create trigger file
            val triggerFile = File(codesDir, "run.txt")
            triggerFile.writeText(currentFileName)

            // Wait for output file
            val nameOnly = currentFileName.substringBeforeLast(".")
            val outputFile = File(codesDir, "$nameOnly.txt")

            // Check for output in background
            Handler(Looper.getMainLooper()).postDelayed({
                checkForOutput(outputFile)
            }, 2000)

        } catch (e: Exception) {
            outputText.text = "Error: ${e.message}"
        }
    }

    private fun checkForOutput(outputFile: File) {
        if (outputFile.exists()) {
            try {
                val output = outputFile.readText()
                outputText.text = output
                outputFile.delete() // Clean up
            } catch (e: Exception) {
                outputText.text = "Error reading output: ${e.message}"
            }
        } else {
            // Keep checking for up to 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (outputFile.exists()) {
                    checkForOutput(outputFile)
                } else {
                    outputText.text = "Compilation timeout. Make sure the Python watcher is running."
                }
            }, 3000)
        }
    }

    private fun showFindReplaceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_find_replace, null)
        val etFind = dialogView.findViewById<EditText>(R.id.etFind)
        val etReplace = dialogView.findViewById<EditText>(R.id.etReplace)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Find & Replace")
            .setView(dialogView)
            .setPositiveButton("Replace All") { _, _ ->
                val findText = etFind.text.toString()
                val replaceText = etReplace.text.toString()
                if (findText.isNotEmpty()) {
                    val updated = editor.text.toString().replace(findText, replaceText)
                    editor.setText(updated)
                }
            }
            .setNeutralButton("Find Next") { _, _ ->
                val findText = etFind.text.toString()
                val start = editor.selectionEnd
                val index = editor.text.toString().indexOf(findText, start, ignoreCase = true)
                if (index != -1) {
                    editor.requestFocus()
                    editor.setSelection(index, index + findText.length)
                } else {
                    Toast.makeText(this, "No more matches", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // 🎨 Make all 3 buttons yellow
        val yellow = ContextCompat.getColor(this, R.color.yellow)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(yellow)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(yellow)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow)
    }



    private fun readTextFromUri(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
    }
}