package com.example.ankitango

data class WordData(
    val word: String,
    val phonetics: List<Phonetic>,
    val meanings: List<Meaning>
)

data class Phonetic(
    val text: String?,
    val audio: String?
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val definition: String,
    val example: String?
)

data class JishoResponse(
    val data: List<JishoEntry>
)

data class JishoSense(
    val english_definitions: List<String>
)

data class JishoEntry(
    val senses: List<JishoSense>,
    val japanese: List<JishoJapanese>
)

data class JishoJapanese(
    val word: String?
)


data class WordResult(
    val word: String,
    val meaning: String,
    val phonetic: String = "",
    val audioUrl: String = "",
    var selected: Boolean = true
)