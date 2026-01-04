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
  - **`Could not find method isMinifyEnabled()` エラーが出る場合:** Android Gradle Plugin 8 以降では `buildTypes { release { minifyEnabled false } }` のようにプロパティを直接指定します。`isMinifyEnabled false` のままだとビルド設定時にメソッドが見つからず失敗するため、`minifyEnabled false` に書き換えた上で Gradle を再同期してください（本リポジトリは修正済み）。
  - **`android.useAndroidX=true` を設定していないとビルドが止まる場合:** Compose や Activity など AndroidX ライブラリを使っているため、ルートの `gradle.properties` に `android.useAndroidX=true` を追加してください（本リポジトリでは既に記載済み）。`checkDebugAarMetadata` タスクで AndroidX 依存を検出して止まる場合は、設定を追加してから Gradle Sync を実行すると解消します。
    1. Android Studio でプロジェクトルートの `gradle.properties` を開き、`android.useAndroidX=true` があるか確認する。
    2. 変更した場合は **File > Sync Project with Gradle Files** を実行し、同期完了まで待つ。
    3. まだ同じエラーが出るときは **Build > Clean Project** → **Build > Rebuild Project** を順に実行してキャッシュをクリアする。
  - **`resource style/Theme.Material3.DayNight.NoActionBar not found` で失敗する場合:** Compose Material3 のテーマリソースは `com.google.android.material:material` 依存に含まれます。`app/build.gradle` の `dependencies` に `implementation "com.google.android.material:material:1.12.0"` が入っているか確認し、入っていなければ追加して Gradle Sync を行ってください（本リポジトリでは追加済み）。
  - **`Your project path contains non-ASCII characters` と出る場合 (Windows):** Gradle の既知制約で、パスに日本語など非 ASCII 文字が含まれているとビルドが止まることがあります。推奨は **ASCII だけのパス（例: `C:\Projects\Pinballgame`）にフォルダーを移動** することです。
  - すぐ移動できない場合の回避策として、リポジトリ直下の `gradle.properties` に `android.overridePathCheck=true` を設定するとビルドを続行できます（このリポジトリでは既に設定済みです）。将来的なトラブルを避けるため、可能なら ASCII パスへ移動してから再同期してください。
  - **`Unable to delete directory ... merged_res_blame_folder` で `mergeDebugResources` が失敗する場合 (Windows):** OneDrive 配下やウイルス対策ソフトによるファイルロックで Gradle のキャッシュ削除に失敗することがあります。以下を順に試してください。
    1. Android Studio を一度閉じ、エクスプローラーやウイルス対策で `Pinballgame\app\build` 以下を開いていないか確認する。
    2. ルートの `gradle.properties` に `org.gradle.vfs.watch=false` を追加（本リポジトリは記載済み）し、**Build > Clean Project** の後に **Rebuild Project** を実行する。
    3. まだ失敗する場合は、`app\build` フォルダーを手動で削除してから再ビルドする。

## 仕組みの概要
- `Canvas` 上で盤面、パドル、ボールを描画
- 毎フレーム重力を適用し、壁とパドルで簡易反発
- パドル角度はタップやボタン入力に応じて切り替え
