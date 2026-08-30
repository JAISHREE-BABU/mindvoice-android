# MindVoice Android

Native Android client for MindVoice — an AI-powered language learning app, built with Kotlin and Jetpack Compose.

## Tech Stack
- Kotlin + Jetpack Compose
- Retrofit for networking
- DataStore for local token storage
- Android's built-in TextToSpeech and MediaRecorder APIs

## Features
- Register/Login with JWT-based auth, auto-login on relaunch
- Text and voice conversation practice with an AI tutor, with live grammar corrections and XP
- AI replies read aloud automatically via text-to-speech
- Structured lessons with vocabulary and XP rewards
- Progress screen (level, XP, proficiency)
- Leaderboard showing top learners

## Screens
Login, Register, Home, Chat, Progress, Lessons, Lesson Detail, Leaderboard

## Setup
1. Open in Android Studio
2. Update `BASE_URL` in `RetrofitClient.kt` to point to your backend
3. Run on an emulator or physical device (min SDK 26)

## Backend
Talks to the [mindvoice-backend](https://github.com/JAISHREE-BABU/mindvoice-backend) API, deployed at `https://mindvoice-backend-k398.onrender.com`.
