# ChestSafe-Converter
このプラグインはLWCのデータベースをChestSafe用に変換したり、ChestSafeのデータベース形式を変換するためのプラグインです。

このプラグインを使用する前に必ず**関係するすべてのデータベースをバックアップ**してください。  
(LWC、ChestSafe(SQLite)、ChestSafe(MySQL)……)

## 動作確認済みの組み合わせ
以下の組み合わせで動作確認済みです。

- Spigot 1.13.2
- ChestSafe 1.1.0
- ModernLWC 2.1.2

(実装上はオリジナルのLWCでも動きますが、オリジナルのLWCはもはやSpigot 1.13.2では動きません)

# 変換
作業は`/chestsafe-convert`コマンドを使用します。

設定中にリセットしたくなったら`/chestsafe-convert reset`で最初からやり直せます。

**必ずバックアップを取得してから作業してください。**

わたし、警告しましたからね？

## LWC -> ChestSafe
ChestSafeのデータ形式は問いません。事前にChestSafeを設定しておいてください。

実行時には`LWC`、`ChestSafe`、`ChestSafe-Converter`の**3つとも**必要です。

1. `/chestsafe-convert lwc`を実行する
1. `/chestsafe-convert [速度]`を実行する  
   速度は数値で指定、ゆっくりめ(100前後)を推奨  
   速すぎるとウォッチドッグタイマーに引っかかってサーバーごと死ぬ
1. `/chestsafe-convert confirm`を実行する  
   完了するまで**大人しく**待ちましょう。

`Donation`と`Password`には対応していません。  
これらの保護に遭遇した場合はプライベートチェストに変換されます。

その他、ChestSafe側でサポートされていないフラグに遭遇した場合は無視されます。

## ChestSafe(SQLite) -> ChestSafe(MySQL)
SQLiteからMySQLに変換するには、ChestSafeがMySQLを使用するように設定してください。  
(要するに`database.type`を`mysql`に設定)

実行時には`ChestSafe`、`ChestSafe-Converter`が必要です。

1. `/chestsafe-convert chestsafe`を実行する
1. `/chestsafe-convert sqlite`を実行する
1. `/chestsafe-convert [sqliteファイルの場所]`を実行する  
   指定しなければ設定から取得を試みる
1. `/chestsafe-convert [速度]`を実行する  
   速度は数値で指定、ゆっくりめ(100前後)を推奨  
   速すぎるとウォッチドッグタイマーに引っかかってサーバーごと死ぬ
1. `/chestsafe-convert confirm`を実行する  
   完了するまで**大人しく**待ちましょう。

## ChestSafe(MySQL) -> ChestSafe(SQLite)
MySQLからSQLiteに変換するには、ChestSafeがSQLiteを使用するように設定してください。  
(要するに`database.type`を`sqlite`に設定)

実行時には`ChestSafe`、`ChestSafe-Converter`が必要です。

1. `/chestsafe-convert chestsafe`を実行する
1. `/chestsafe-convert mysql`を実行する
1. `/chestsafe-convert [MySQLのホスト名]`を実行する  
   指定しなければ設定から取得を試みる
1. `/chestsafe-convert [MySQLのデータベース名]`を実行する  
   指定しなければ設定から取得を試みる
1. `/chestsafe-convert [MySQLのユーザー名]`を実行する  
   指定しなければ設定から取得を試みる
1. `/chestsafe-convert [MySQLのパスワード]`を実行する  
   指定しなければ設定から取得を試みる
1. `/chestsafe-convert [速度]`を実行する  
   速度は数値で指定、ゆっくりめ(100前後)を推奨  
   速すぎるとウォッチドッグタイマーに引っかかってサーバーごと死ぬ
1. `/chestsafe-convert confirm`を実行する  
   完了するまで**大人しく**待ちましょう。
