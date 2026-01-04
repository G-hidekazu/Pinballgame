# Pinballgame

シンプルな Jetpack Compose 製のピンボール サンプルです。プランジャーと左右のパドル（フリッパー）を備え、タッチでもボタンでも操作できます。

## ビルド方法
1. Android Studio Iguana 以降でこのリポジトリを開きます。
2. プロジェクトの Gradle を同期します。
3. 実機またはエミュレーターにデプロイして起動します。

### Android Studio でリポジトリを開く詳しい手順
- **リポジトリとは?** GitHub 上にあるプロジェクト一式（ソースコードや Gradle 設定、リソースなど）の保管場所です。このリポジトリを PC に clone（または ZIP ダウンロードして展開）してから Android Studio で開きます。
- **開く場所:** clone／展開したフォルダー直下の `Pinballgame` ディレクトリがプロジェクトのルートです。`app/build.gradle` や `settings.gradle` が置かれている階層を選びます。

手順:
1. GitHub から `Pinballgame` リポジトリを clone するか、ZIP をダウンロードして展開する。
2. Android Studio を起動し、Welcome 画面やメニューから **Open**（インポートではなく「Open」を選ぶ）をクリック。
3. ファイルダイアログで、手順1で取得したフォルダーを開き、**`Pinballgame` フォルダーを1回クリックして選択**する。中に入らず、フォルダー自体を選択した状態で **OK / Open** を押す。
   - 目印: このフォルダー直下に `settings.gradle` と `app/build.gradle` が見える階層です。
4. 初回は自動で Gradle 認識が走るので、画面上部に表示される **Sync Project with Gradle Files** を実行して同期を完了。
5. ビルドツールや SDK Platform 34 が不足している場合は案内に従いインストールし、再度同期してください。

## 操作方法
- 画面左側タップ／左パドルボタン: 左フリッパーを跳ね上げ
- 画面右側タップ／右パドルボタン: 右フリッパーを跳ね上げ
- プラスボタンでプランジャーを引き、Launch ボタンで発射

## よくある質問 / トラブルシュート
- アプリを実行しても「Hello Android」だけが表示される場合: Android Studio のテンプレート画面が開いている可能性があります。必ずこのリポジトリを clone / ZIP 展開した **`Pinballgame` フォルダー直下**（`settings.gradle` と `app/build.gradle` がある階層）を Android Studio の **Open** で選んでください。また、既に端末にインストールされている別アプリが表示されている場合があるので、端末側でアプリをアンインストールしてから再ビルドすると確実です。
- **`Plugin [id: 'com.android.application' ...] was not found` と出る場合:** ルートの `settings.gradle` にプラグイン検索用のリポジトリ指定が無いと、Gradle が Android Gradle Plugin を見つけられません。このリポジトリでは以下の設定を入れています。もしローカルの `settings.gradle` が異なっている場合は、同じ内容にしてから **Sync Project with Gradle Files** を実行してください。
  ```kts
  pluginManagement {
      repositories {
          google()
          mavenCentral()
          gradlePluginPortal()
      }
  }

  dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
          google()
          mavenCentral()
      }
  }
  ```
  それでも解決しない場合は、ネットワーク（プロキシ設定を含む）を確認し、`Gradle Settings` でオフラインモードが有効になっていないかも確認してください。

## 仕組みの概要
- `Canvas` 上で盤面、パドル、ボールを描画
- 毎フレーム重力を適用し、壁とパドルで簡易反発
- パドル角度はタップやボタン入力に応じて切り替え
