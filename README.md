# VaultAI Android starter

This project packages the supplied Kotlin/Jetpack Compose starter into an Android app and uses the VaultAI logo as its launcher icon. It is currently a starter screen, not a complete offline AI assistant.

## Build APK with GitHub Actions
1. Create a GitHub repository named `VaultAI`.
2. Upload the contents of this folder to the repository root.
3. Open the **Actions** tab and enable workflows if GitHub asks.
4. Select **Build VaultAI APK** and press **Run workflow**, or push to `main`/`master`.
5. Open the completed workflow run, scroll to **Artifacts**, and download `VaultAI-debug-apk`.
6. Extract the ZIP artifact to find `app-debug.apk`.

The APK is a debug build for testing. It is not signed for Play Store release.
