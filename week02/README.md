# AnkiTango

AnkiTango は、英単語を入力するだけで意味・発音・音声リンクを取得し、  
さらに DeepL API で日本語に翻訳した意味を表示する Android アプリです。 

## 主な機能

- 英単語を複数行で入力し、まとめて処理
- Free Dictionary API で英語の意味・発音・音声リンクを取得
- DeepL API で日本語に自動翻訳（複数定義に対応）

## 使い方

1. 英単語を改行で区切って入力  
2. 「意味を取得」ボタンを押す  
3. 日本語訳付きで一覧表示される  

### 注意

このリポジトリにはセキュリティ上の理由から **DeepL APIキーは含まれていません**。  
そのため、**翻訳機能はそのままでは動作しません。**

実際にアプリを動作させるには、上記のように `local.properties` にご自身の DeepL APIキーを設定する必要があります。

## 技術スタック

- Kotlin + Jetpack Compose
- Free Dictionary API（[dictionaryapi.dev](https://dictionaryapi.dev/)）
- DeepL Translation API
- Gson / Coroutine / HttpURLConnection
