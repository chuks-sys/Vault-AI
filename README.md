# VaultAI

VaultAI is an Android personal-assistant project designed to grow into
a futuristic assistant with offline AI, voice interaction, and customizable
wake-word activation.

## Current features

- Dark futuristic-style interface
- Customizable assistant name
- Persistent assistant settings
- Text input
- Android speech-recognition integration
- Text-to-speech responses
- Android APK build configuration

## Planned features

- Local AI model integration
- Offline question answering
- Custom wake-word detection
- Hands-free assistant activation
- Display wake-up interface
- AI model download manager
- Privacy and microphone controls

## Requirements

- Android 8.0 or later
- Android SDK 35 for building
- Java 17
- Gradle compatible with the Android Gradle Plugin

## Build

Use the configured GitHub Actions workflow to build a debug APK.

## Important

The current application does not yet contain an integrated AI model or
a continuous custom wake-word detection engine. Speech recognition
currently uses Android's recognition interface.

A compatible local AI model and wake-word engine must be integrated
before offline AI responses and hands-free wake-word activation will work.
