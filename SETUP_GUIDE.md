# Snake Game - Complete Setup & Development Guide

## Quick Start (5 Minutes)

### Step 1: Firebase Configuration
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a new project"
3. Name it "snake-game-multiplayer"
4. Continue with default settings
5. Download `google-services.json`
6. Place it in `app/` folder

### Step 2: Build the Project
```bash
cd main_snake_game
./gradlew clean build
```

### Step 3: Run on Device/Emulator
```bash
./gradlew installDebug
```

## Detailed Setup

### Prerequisites
- Android Studio 2022.1.1 or later
- Java JDK 11+
- Android SDK API 34
- Google Play Services
- Kotlin 1.9.0+

### Project Configuration Files

#### 1. gradle/libs.versions.toml
Already configured with all dependencies:
- Firebase Auth, Database, Analytics
- Hilt, Room
- Google Play Services Location
- Jetpack Compose & Navigation

#### 2. AndroidManifest.xml
Already includes permissions:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

#### 3. app/build.gradle.kts
Configured with:
- Hilt & kapt for annotation processing
- Room database setup
- Firebase BOM
- Kotlin serialization plugin
- Google Services plugin

### Firebase Setup (Detailed)

#### Create Firebase Project
1. Visit [Firebase Console](https://console.firebase.google.com/)
2. Click "Create project"
3. Enter project name: "snake-game-multiplayer"
4. Accept defaults and create
5. Wait for project initialization (2-3 minutes)

#### Enable Authentication
1. Go to "Authentication" section
2. Click "Get started"
3. Enable sign-in methods:
   - Anonymous
   - Email/Password

#### Enable Realtime Database
1. Go to "Realtime Database"
2. Click "Create Database"
3. Select location closest to you
4. Start in test mode (for development)
5. Configure security rules:

```json
{
  "rules": {
    "players": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid"
      }
    },
    "games": {
      "$gameId": {
        ".read": true,
        ".write": true
      }
    }
  }
}
```

#### Download Configuration
1. Go to Project Settings (gear icon)
2. Select "Your apps" section
3. Create Android app (if not exists)
4. Follow setup wizard
5. Download `google-services.json`
6. Place in `app/` directory

### Local Development Setup

#### 1. Clone Repository
```bash
git clone <repo-url>
cd main_snake_game
```

#### 2. Add google-services.json
```
main_snake_game/
├── app/
│   └── google-services.json    ← Place here
├── build.gradle.kts
└── ...
```

#### 3. Sync Gradle
In Android Studio:
- File → Sync Now
- Or press Ctrl+Shift+I (Windows/Linux) or Cmd+Shift+I (Mac)

#### 4. Run on Emulator
```bash
./gradlew installDebug
```

Or use Android Studio:
- Tools → AVD Manager
- Select device and click Play
- Android Studio will detect and install

### Testing the Application

#### Manual Testing Checklist

**Authentication Flow**
- [ ] Start app → Authentication screen appears
- [ ] Click "Play as Guest" → Anonymous sign-in works
- [ ] Navigate to Menu screen
- [ ] Click "Email Sign-In" → Email entry screen shown
- [ ] Logout works properly

**Single Player Game**
- [ ] Menu → Single Player button navigates to game
- [ ] Snake appears on grid with initial length
- [ ] Direction buttons control snake movement
- [ ] Snake grows when eating food
- [ ] Score updates correctly
- [ ] Pause/Resume buttons work
- [ ] Stop button returns to menu
- [ ] Game ends when snake hits self

**Multiplayer Lobby**
- [ ] GPS permission request shown
- [ ] "Waiting for GPS connection..." displays if location not available
- [ ] Online players list shows with scores
- [ ] Start Game button enabled after location ready
- [ ] Back to Menu button works

**Game Data Persistence**
- [ ] Close and reopen app
- [ ] Player data persists in local database
- [ ] Game history displayed in Room database

### Debugging

#### Enable Debug Logs
Add to `MainActivity.kt`:
```kotlin
import android.util.Log

val TAG = "SnakeGame"
Log.d(TAG, "Game started")
```

#### Firebase Console Monitoring
1. Open Firebase Console
2. Navigate to Realtime Database
3. View live data updates
4. Check Authentication logs

#### Android Studio Debugger
```
Run → Debug app
Set breakpoints by clicking line numbers
Step through code with F6/F7 keys
```

### Performance Optimization

#### For Low-End Devices
1. Reduce grid size in `Constants.kt`:
```kotlin
const val GRID_WIDTH = 20
const val GRID_HEIGHT = 20
```

2. Increase game tick interval:
```kotlin
const val GAME_TICK_MS = 150L  // From 100L
```

3. Disable animations:
```kotlin
// In GameCanvas.kt
// Remove transition effects
```

#### For High-End Devices
1. Enable HD grid:
```kotlin
const val GRID_WIDTH = 50
const val GRID_HEIGHT = 50
```

2. Decrease tick interval for faster gameplay:
```kotlin
const val GAME_TICK_MS = 50L
```

### Troubleshooting

#### Issue: "Hilt annotation processing failed"
**Solution**:
```bash
./gradlew clean build
# Or in Android Studio: Build → Clean Project
```

#### Issue: "Firebase connection timeout"
**Solution**:
- Verify internet connection
- Check `google-services.json` is present
- Verify Firebase project is active

#### Issue: "Location permission denied"
**Solution**:
```
Device Settings → Apps → Snake Game → Permissions → Enable Location
Or: Emulator → Extended Controls → Location → Set coordinates
```

#### Issue: "Room database migration failed"
**Solution**:
```kotlin
// In DatabaseModule.kt, change to:
.fallbackToDestructiveMigration()
```

#### Issue: "Navigation destination not found"
**Solution**:
- Ensure all Screen composables are created
- Verify route names match in NavHost
- Check compose navigation imports

### Database Inspection

#### Using Android Studio
1. View → Tool Windows → Database Inspector
2. Connect to emulator/device
3. Browse Room database tables
4. View player records and game history

#### Using Firebase Console
1. Go to Realtime Database
2. View raw JSON data
3. Monitor real-time updates
4. Test security rules

### Building Release APK

```bash
./gradlew assembleRelease

# Output located at:
# app/build/outputs/apk/release/app-release.apk
```

### Installing Release Build
```bash
./gradlew installRelease
```

### Common Code Patterns

#### Starting a Game
```kotlin
gameViewModel.startSinglePlayerGame(
    playerId = userId,
    playerName = "Player_XYZ"
)
```

#### Changing Snake Direction
```kotlin
gameViewModel.changeDirection(Direction.UP)
```

#### Tracking Player Location
```kotlin
multiplayerViewModel.startTrackingLocation(userId)
```

#### Saving Game Record
```kotlin
val record = GameRecordEntity(
    gameId = gameId,
    playerId = userId,
    playerName = "Player",
    score = 100,
    foodEaten = 10,
    snakeBites = 2,
    gameMode = "SINGLE_PLAYER",
    duration = 300000L,
    isWinner = true
)
gameRepository.saveGameRecord(record)
```

## Advanced Configuration

### Custom Game Settings
Edit `util/Constants.kt`:
```kotlin
const val GRID_WIDTH = 30          // Game grid width
const val GRID_HEIGHT = 30         // Game grid height
const val GAME_TICK_MS = 100L      // Milliseconds per game tick
const val REGULAR_FOOD_POINTS = 10 // Points for regular food
const val BONUS_FOOD_POINTS = 50   // Points for bonus food
```

### Geohash Precision
In `GeoHashUtils.kt`:
```kotlin
// Current: 6 (±1.2 km precision)
// Options: 
// 4 = ±20 km
// 5 = ±2.4 km
// 6 = ±1.2 km
// 7 = ±150 m
GeoHashUtils.encode(lat, lon, precision = 6)
```

### Firebase Security Rules
Update in Firebase Console:
```json
{
  "rules": {
    "players": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid",
        ".validate": "newData.hasChildren(['name', 'score'])"
      }
    }
  }
}
```

## Deployment Checklist

- [ ] Firebase project created and configured
- [ ] `google-services.json` added to `app/` folder
- [ ] All permissions declared in AndroidManifest.xml
- [ ] Location services enabled (dev device)
- [ ] Firebase Realtime Database initialized
- [ ] Authentication methods enabled (Anonymous, Email)
- [ ] Room database schema verified
- [ ] Hilt components properly annotated
- [ ] Navigation routes all defined
- [ ] App tested on minimum Android API 24
- [ ] All screens tested (Auth, Menu, Game, Lobby)
- [ ] Single-player game tested end-to-end
- [ ] Multiplayer flow tested
- [ ] GPS location tracking verified
- [ ] Score calculations verified
- [ ] Data persistence tested

## Support & Documentation

- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Firebase**: https://firebase.google.com/docs
- **Hilt**: https://developer.android.com/training/dependency-injection/hilt-android
- **Room Database**: https://developer.android.com/training/data-storage/room
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html

---

**Happy Coding! 🐍**
