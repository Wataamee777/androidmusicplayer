# Android Music Player

Android 13 (API 33) をメインターゲットにしたローカル音声ファイル専用プレイヤーです。

## 構成

- `app/src/main/java/com/example/androidmusicplayer/playback`: Media3 `MediaSessionService` と通知カスタムアクション
- `app/src/main/java/com/example/androidmusicplayer/util`: メタデータ抽出とフォルダ再帰スキャン
- `app/src/main/java/com/example/androidmusicplayer/ui`: Jetpack Compose のプレイヤー、フォルダブラウザ、キュー、履歴、イコライザー
- `app/src/main/java/com/example/androidmusicplayer/data`: Room の再生履歴DB
- `.github/workflows/android.yml`: GitHub Actions の Android ビルド

## フォルダキュー規則

曲を選択すると、その曲の親フォルダをルートとして再帰的に音声ファイルを収集します。親フォルダより上位の階層や兄弟フォルダは含めず、同一フォルダ内を名前順に並べてから下層サブフォルダを深さ優先で結合します。
