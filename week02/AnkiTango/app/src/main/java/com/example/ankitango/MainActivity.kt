package com.example.ankitango

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ankitango.ui.theme.AnkiTangoTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnkiTangoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VocabScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AnkiTangoTheme {
        Greeting("Android")
    }
}

@Composable
fun VocabScreen(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("en") }
    val wordResults = remember { mutableStateListOf<WordResult>() }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Button(
            onClick = {
                wordResults.clear()
                val wordList = text.split("\n")
                wordList.forEach { word ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val apiUrl =
                            "https://api.dictionaryapi.dev/api/v2/entries/en/${word.trim()}"
                        try {
                            val json = URL(apiUrl).readText()
                            val listType = object : TypeToken<List<WordData>>() {}.type
                            val wordData = Gson().fromJson<List<WordData>>(json, listType)

                            val definitions = wordData
                                .firstOrNull()?.meanings
                                ?.flatMap { it.definitions }
                                ?.mapNotNull { it.definition }
                                ?.distinct()
                                ?: listOf("意味なし")

                            val translatedDefinitions = definitions.map { def ->
                                translateWithDeepL(def, apiKey)
                            }

                            val combined = translatedDefinitions.joinToString(" / ")

                            val phonetic =
                                wordData.firstOrNull()?.phonetics?.firstOrNull()?.text ?: ""
                            val audioUrl =
                                wordData.firstOrNull()?.phonetics?.firstOrNull()?.audio ?: ""

                            withContext(Dispatchers.Main) {
                                wordResults.add(
                                    WordResult(
                                        word = word,
                                        meaning = combined,
                                        phonetic = phonetic,
                                        audioUrl = audioUrl,
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                wordResults.add(
                                    WordResult(
                                        word = word,
                                        meaning = "取得失敗",
                                        phonetic = "",
                                        audioUrl = "",
                                        selected = false
                                    )
                                )
                            }

                        }
                    }
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("意味を取得")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(wordResults) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = item.selected,
                        onCheckedChange = { item.selected = it }
                    )
                    Column {
                        Text("${item.word}: ${item.meaning}")
                        if (item.phonetic.isNotEmpty()) Text("発音: ${item.phonetic}")
                        if (item.audioUrl.isNotEmpty()) Text("音声: ${item.audioUrl}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val selectedItems = wordResults.filter { it.selected }
                saveAsCsv(context, selectedItems)
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("CSVとしてエクスポート")
        }
    }
}

fun saveAsCsv(context: Context, selectedItems: List<WordResult>) {
    try {
        val filename = "anki_vocab_export.csv"
        val file = File(context.getExternalFilesDirs(null)[0], filename)
        file.printWriter().use { out ->
            out.println("Word,Meaning,Phonetic,Audio")
            selectedItems.forEach { item ->
                val safeWord = item.word.replace(",", " ")
                val safeMeaning = item.meaning.replace(",", " ")
                val safePhonetic = item.phonetic.replace(",", " ")
                val safeAudio = item.audioUrl.replace(",", " ")

                out.println("$safeWord,$safeMeaning,$safePhonetic,$safeAudio")
            }
        }
        Toast.makeText(context, "CSV保存完了！\n${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "保存に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

fun translateWithDeepL(text: String, apiKey: String): String {
    val url = URL("https://api-free.deepl.com/v2/translate")
    val params = "auth_key=$apiKey&text=${URLEncoder.encode(text, "UTF-8")}&target_lang=JA"

    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

    connection.outputStream.use {
        it.write(params.toByteArray())
    }

    val response = connection.inputStream.bufferedReader().readText()
    val json = JSONObject(response)
    return json.getJSONArray("translations")
        .getJSONObject(0)
        .getString("text")
}

val apiKey = BuildConfig.DEEPL_API_KEY