# StudyBank

**StudyBank** は、ストップウォッチとタイマーを使って勉強時間を記録し、
それを「貯金のように」積み上げて可視化できる JavaFX デスクトップアプリです。  
日別グラフや統計情報で、モチベーション維持にも役立ちます。

---

## 主な機能

- ストップウォッチとカウントダウンタイマー
- 勉強時間を日付ごとにJSONでローカル保存
- 日別グラフ (BarChart)で移り変わりを可視化
- 今日/今週/今月/今年の合計を統計表示
- 平均時間をグラフに横線で表示
- タイマー終了時に音声通知
- シンプルで現代的な積み上げUI

---

## 使い方

1. Java (JDK 17以降)と JavaFX SDK をインストール
2. Eclipse または CLI で `MainApp.java` を実行
3. 各画面 (ストップウォッチ / タイマー / グラフ / 統計) をボタンで切り替え
4. 終了時に `study-log.json`に自動記録
5. グラフと統計で日々の積み上げを確認

---

## データ保存場所

- `study-log.json` ... 日付ごとに秘隠的に記録された勉強経過時間 (sec)

---

## 注意事項

- 外部APIやクラウドとは繋がらず、**完全にローカル動作**します
- `.exe`化して配布する場合は `jpackage`を使用

---

## 開発環境 / 技術

- Java 17+
- JavaFX 21+
  - `javafx.controls`, `javafx.media` 使用
- Eclipse (Runnable Jar Export)
- JSON (ローカル保存)
- `digital-7` フォント
- `alarm.mp3` (タイマー通知音)

---

## 実行方法

1. JDK 17以降 + JavaFX SDK をインストール
2. 実行時 VMオプションを付ける:

```bash
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.media
```

3. `MainApp.java`を実行 (Eclipse)

---

## 作成者

このアプリは「1週間に1個アプリを作る」チャレンジの week04 として開発されました。  
GitHub リポジトリ: [One-App-per-Week/week04](https://github.com/yourname/One-App-per-Week/tree/main/week04)


