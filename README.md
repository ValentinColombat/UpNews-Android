# UpNews — One positive story a day.

> In a world of endless bad news, UpNews gives you one carefully selected positive story each day — nothing more, nothing less.

---

## What is UpNews?

**UpNews** is a mobile app that cuts through the noise of the 24/7 news cycle. Every day, one uplifting story. Read it, listen to it, and move on with your day feeling good.

No infinite scroll. No outrage bait. No algorithm designed to keep you hooked.

Just one story — told beautifully.

---

## Features

### One Story a Day
A single, hand-picked positive news story delivered every morning. Short enough to read in under 3 minutes, meaningful enough to stay with you all day.

### Audio Playback
Can't read right now? Listen. Every story comes with a full audio version so you can catch up on your commute, during a walk, or while making coffee.

### Daily Streaks & Companions
Stay consistent with a streak system that rewards your daily reading habit. Unlock unique animal companions as you build your streak — each one a little character with its own personality.

### Smart Notifications
Get a gentle nudge at the right time. UpNews sends a single daily notification when your story is ready — and never more than that.

### Premium Membership
Go further with UpNews Premium. Access the full article library, unlock all companions, and support independent, feel-good journalism.

- Monthly: €3.99/month
- Annual: €39.99/year *(save 17%)*
- 14-day free trial, cancel anytime

---

## Design Philosophy

UpNews is built around calm. The interface is soft, minimal, and intentional — pastel colors, gentle animations, and a layout that never overwhelms. Every design decision serves one goal: make you feel good the moment you open the app.

---

## Tech Stack

Built natively for Android using modern best practices.

| Layer | Technology |
|---|---|
| UI | Jetpack Compose |
| Architecture | MVVM + StateFlow |
| Backend | Supabase |
| Audio | Media3 / ExoPlayer |
| Auth | Google Sign-In |
| Payments | Google Play Billing |
| Language | Kotlin |

**Minimum Android version:** Android 8.0 (API 26)

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 21
- Android SDK API 36

### Setup

1. Clone the repository
   ```bash
   git clone https://github.com/valentincolombat/UpNews.git
   ```

2. Copy the secrets template files and fill in your credentials
   ```bash
   cp SupabaseSecrets.example.kt app/src/main/java/com/valentincolombat/upnews/data/remote/SupabaseSecrets.kt
   cp GoogleSecrets.example.kt app/src/main/java/com/valentincolombat/upnews/service/GoogleSecrets.kt
   ```

3. Open the project in Android Studio and sync Gradle

4. Run on your device or emulator

> **Note:** The app requires valid Supabase and Google credentials to function. See the `.example` files for the expected format.

---

## Project Structure

```
app/src/main/java/com/valentincolombat/upnews/
├── data/
│   ├── model/          # Domain models
│   ├── remote/         # Supabase client & config
│   └── repository/     # Data layer (Auth, Articles, Streaks…)
├── service/            # Audio, notifications, background tasks
├── ui/
│   ├── auth/           # Login & onboarding
│   ├── home/           # Main feed
│   ├── article/        # Story detail & audio player
│   ├── companions/     # Companion gallery
│   ├── freemium/       # Subscription screens
│   └── theme/          # Colors, typography, design tokens
└── MainActivity.kt
```

---

## License

This project is proprietary software. All rights reserved.

© 2026 Valentin Colombat
