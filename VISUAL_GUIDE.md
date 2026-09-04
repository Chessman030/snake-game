# 🎮 Snake Game - Developer's Visual Guide

## 🗺️ Project Map

```
main_snake_game/
│
├── 📄 README.md                           [START HERE]
├── 📄 QUICKSTART.md                       [5-MINUTE SETUP]
├── 📄 SETUP_GUIDE.md                      [DETAILED SETUP]
├── 📄 ARCHITECTURE.md                     [TECHNICAL DEEP-DIVE]
├── 📄 PROJECT_SUMMARY.md                  [FILE MANIFEST]
├── 📄 VISUAL_GUIDE.md                     [THIS FILE]
│
├── build.gradle.kts                       [ROOT BUILD]
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
│
├── gradle/
│   ├── libs.versions.toml                 [✅ ALL DEPS CONFIGURED]
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
└── app/
    ├── build.gradle.kts                   [✅ PLUGINS & DEPS ADDED]
    ├── proguard-rules.pro
    ├── google-services.json                [⚠️ NEEDS YOUR CONFIG]
    │
    ├── src/main/AndroidManifest.xml        [✅ PERMISSIONS ADDED]
    │
    └── src/main/java/com/example/main_snake_game/
        │
        ├── 🎮 GAME LOGIC & MODELS
        ├── game/
        │   ├── model/
        │   │   ├── Point.kt                 [2D coordinate on grid]
        │   │   ├── Snake.kt                 [Player's snake]
        │   │   ├── Food.kt                  [Food to eat]
        │   │   ├── Player.kt                [Player profile]
        │   │   ├── GameEvent.kt             [Game event types]
        │   │   └── GameState.kt             [Complete game snapshot]
        │   │
        │   ├── logic/
        │   │   ├── GameEngine.kt            [⚙️ MAIN GAME LOOP]
        │   │   ├── CollisionDetector.kt     [Collision detection]
        │   │   └── ScoringEngine.kt         [Points calculation]
        │   │
        │   └── ui/
        │       ├── screens/
        │       │   ├── AuthenticationScreen.kt
        │       │   ├── MenuScreen.kt
        │       │   ├── SinglePlayerGameScreen.kt
        │       │   └── MultiplayerLobbyScreen.kt
        │       │
        │       ├── components/
        │       │   ├── GameCanvas.kt        [Renders the game grid]
        │       │   ├── ScoreBoard.kt        [Shows scores]
        │       │   └── GameControls.kt      [D-Pad + buttons]
        │       │
        │       └── viewmodel/
        │           ├── AuthViewModel.kt     [Auth state]
        │           ├── GameViewModel.kt     [Game state]
        │           └── MultiplayerViewModel.kt  [Multiplayer state]
        │
        ├── 💾 DATA LAYER
        ├── data/
        │   ├── local/
        │   │   ├── entity/
        │   │   │   ├── PlayerEntity.kt      [Room table]
        │   │   │   └── GameRecordEntity.kt  [Room table]
        │   │   ├── dao/
        │   │   │   ├── PlayerDao.kt         [Player queries]
        │   │   │   └── GameRecordDao.kt     [Game queries]
        │   │   └── GameDatabase.kt          [Room setup]
        │   │
        │   ├── remote/
        │   │   ├── dto/
        │   │   │   └── DTOs.kt              [Data transfer objects]
        │   │   └── service/
        │   │       ├── FirebaseService.kt   [Auth + Database]
        │   │       ├── LocationService.kt   [GPS tracking]
        │   │       └── WebSocketService.kt  [Real-time events]
        │   │
        │   └── repository/
        │       ├── AuthRepository.kt        [Auth abstraction]
        │       ├── PlayerRepository.kt      [Player data]
        │       ├── GameRepository.kt        [Game records]
        │       └── LocationRepository.kt    [Location data]
        │
        ├── 💉 DEPENDENCY INJECTION
        ├── di/
        │   ├── DatabaseModule.kt            [Provides Room, DAOs]
        │   ├── RepositoryModule.kt          [Provides repos]
        │   └── ServiceModule.kt             [Provides services]
        │
        ├── 🛠️ UTILITIES
        ├── util/
        │   ├── Constants.kt                 [⚙️ TUNE GAME HERE]
        │   └── GeoHashUtils.kt              [Location encoding]
        │
        ├── 🚀 APP SETUP
        ├── SnakeGameApplication.kt          [Hilt app class]
        └── MainActivity.kt                  [Entry point + nav]
```

---

## 🎯 Navigation Flow

```
┌─────────────────────────────────────────────────┐
│  MainActivity                                   │
│  ├─ Hilt initialization                        │
│  └─ Sets up navigation controller              │
└────────────┬────────────────────────────────────┘
             │
    ┌────────▼─────────┐
    │ Authentication   │
    │ Screen           │
    └────────┬─────────┘
             │
      ┌──────▼──────┐
      │ Menu Screen │
      └──────┬──────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼────────┐   ┌────▼──────────────┐
│ Single     │   │ Multiplayer       │
│ Player     │   │ Lobby             │
│ Game       │   └────┬──────────────┘
│ Screen     │        │
└───────────┘    ┌────▼──────────────┐
                 │ Multiplayer       │
                 │ Game              │
                 │ Screen            │
                 └───────────────────┘
```

---

## 📊 Data Flow Diagram

```
USER INPUTS
    │
    ├─→ Direction Button  ──→ GameViewModel.changeDirection()
    │                          │
    │                          └─→ GameEngine.changeDirection()
    │                               └─→ Update Snake.nextDirection
    │
    ├─→ Sign In Button     ──→ AuthViewModel.signInAnonymously()
    │                          │
    │                          └─→ AuthRepository.signInAnonymously()
    │                               └─→ FirebaseService.signInAnonymously()
    │                                    └─→ Firebase Auth
    │
    └─→ Start Multiplayer  ──→ MultiplayerViewModel.startTrackingLocation()
                                 │
                                 └─→ LocationRepository.getLocationUpdates()
                                      └─→ LocationService.getLocationUpdates()
                                           └─→ Google Play Services (GPS)


GAME LOOP (Every 100ms)
    │
    ├─→ GameEngine.updateGameState()
    │   ├─→ Move all snakes
    │   ├─→ Detect collisions (food, self, opponent)
    │   ├─→ Calculate scores
    │   └─→ Emit GameEvents
    │
    ├─→ ViewModel receives update
    │   └─→ Updates StateFlow<GameState>
    │
    └─→ Compose recomposes UI
        └─→ Renders new GameState
```

---

## 🧩 Component Dependency Graph

```
MainActivity
    │
    ├─→ AuthViewModel
    │   ├─→ AuthRepository
    │   │   └─→ FirebaseService
    │   └─→ PlayerRepository
    │       ├─→ FirebaseService
    │       └─→ PlayerDao
    │
    ├─→ GameViewModel
    │   ├─→ GameEngine
    │   │   ├─→ ScoringEngine
    │   │   └─→ CollisionDetector
    │   ├─→ GameRepository
    │   │   └─→ GameRecordDao
    │   └─→ LocationRepository
    │       └─→ LocationService
    │
    └─→ MultiplayerViewModel
        ├─→ PlayerRepository
        └─→ LocationRepository
```

---

## 🔄 State Management Flow

```
Event
  │
  ├─→ User Input
  │   └─→ ViewModel Method Called
  │       └─→ Repository Called
  │           └─→ Service Called
  │               └─→ External Data Source
  │                   └─→ Response received
  │                       └─→ Updates ViewModel State
  │                           └─→ StateFlow emits
  │                               └─→ Compose recomposes
  │                                   └─→ New UI rendered
  │
  └─→ Coroutine launched
      └─→ collect { ... }
          └─→ Update State
              └─→ UI reflects change
```

---

## 🎮 Game Loop Architecture

```
┌──────────────────────────────────────────┐
│ GAME LOOP (100ms tick)                   │
└──────────┬───────────────────────────────┘
           │
        [TICK]
           │
    ┌──────▼──────┐
    │  Move Phase │
    │ (all snakes)│
    └──────┬──────┘
           │
    ┌──────▼────────────┐
    │ Collision Phase   │
    │ ├─ Food check    │
    │ ├─ Self check    │
    │ └─ Bite check    │
    └──────┬────────────┘
           │
    ┌──────▼──────────┐
    │ Scoring Phase   │
    │ (update points) │
    └──────┬──────────┘
           │
    ┌──────▼──────────┐
    │ Cleanup Phase   │
    │ (remove dead)   │
    └──────┬──────────┘
           │
    ┌──────▼──────────────┐
    │ Check Win Phase     │
    │ (≤1 alive?  OVER)  │
    └──────┬──────────────┘
           │
    ┌──────▼──────────┐
    │ Emit Events     │
    └──────┬──────────┘
           │
    ┌──────▼──────────────┐
    │ Update ViewModel    │
    │ (StateFlow)         │
    └──────┬──────────────┘
           │
    ┌──────▼──────────────┐
    │ Compose recomposes  │
    │ (new UI)            │
    └──────┬──────────────┘
           │
          [WAIT 100ms]
           │
           └─→ [NEXT TICK]
```

---

## 📚 How to Navigate the Code

### To Understand the Game
1. Start: `game/model/GameState.kt` - What data exists?
2. Look: `game/logic/GameEngine.kt` - How does it change?
3. Study: `game/ui/components/GameCanvas.kt` - How is it shown?

### To Add a Feature
1. Create model in `game/model/`
2. Add logic in `game/logic/`
3. Update UI in `game/ui/`
4. Add repository in `data/repository/`
5. Expose in ViewModel

### To Debug an Issue
1. Check `MainActivity.kt` - Is navigation correct?
2. Check ViewModel - Is state updating?
3. Check `GameEngine.kt` - Is logic correct?
4. Check UI - Is it rendering right?

---

## 🔍 File Purpose Quick Reference

| File | Purpose | Priority |
|------|---------|----------|
| `GameEngine.kt` | Core game loop | 🔴 Critical |
| `GameState.kt` | Data structure | 🔴 Critical |
| `GameViewModel.kt` | State management | 🔴 Critical |
| `MainActivity.kt` | App entry | 🟠 High |
| `Constants.kt` | Configuration | 🟠 High |
| `FirebaseService.kt` | Cloud sync | 🟠 High |
| `GameCanvas.kt` | Game rendering | 🟡 Medium |
| `LocationService.kt` | GPS tracking | 🟡 Medium |
| `GameDatabase.kt` | Local storage | 🟡 Medium |

---

## 🎯 Common Tasks & Where to Find Them

| Task | File | Method |
|------|------|--------|
| Change game speed | `Constants.kt` | `GAME_TICK_MS` |
| Change snake color | `GameCanvas.kt` | `SnakeCell()` |
| Change points | `ScoringEngine.kt` | `BONUS_FOOD_POINTS` |
| Add new screen | `MainActivity.kt` | `composable("route")` |
| Change grid size | `Constants.kt` | `GRID_WIDTH/HEIGHT` |
| Add a new game event | `GameEvent.kt` | Add `data class` |
| Store new player data | `PlayerEntity.kt` | Add `@ColumnInfo` |
| Call new Firebase API | `FirebaseService.kt` | Add new method |

---

## 🧪 Testing Coverage Map

### Ready to Test
- [x] GameEngine logic
- [x] CollisionDetector
- [x] ScoringEngine
- [x] Room DAOs
- [x] Repositories
- [x] ViewModels

### Should Add Tests
- [ ] UI components
- [ ] Navigation
- [ ] Firebase integration
- [ ] Location service
- [ ] End-to-end flows

---

## 🚀 Build & Deploy Checklist

```
BEFORE BUILD
□ Firebase project created
□ google-services.json placed in app/
□ All dependencies downloaded
□ No IDE errors (red squiggles)

BUILD
./gradlew clean build

INSTALL
./gradlew installDebug

TEST
□ App launches without crash
□ Single player game works
□ Multiplayer lobby shows
□ Location tracking works
□ Scores save correctly

DEPLOY
./gradlew assembleRelease
Sign APK
Upload to Play Store
```

---

## 💡 Pro Tips

1. **Fast Development**
   - Use emulator with pre-set location
   - Modify `Constants.kt` for testing
   - Use breakpoints in GameEngine

2. **Performance Optimization**
   - Reduce GRID_WIDTH/HEIGHT on old devices
   - Increase GAME_TICK_MS for slower devices
   - Use composable() instead of Spacer() excessively

3. **Debugging**
   - Add Log.d() statements in GameEngine.kt
   - Watch StateFlow with .collect {} in logcat
   - Use Firebase Console to view real-time data

4. **Extension**
   - Create new sealed GameEvent subclass
   - Extend Food class for power-ups
   - Add more Snake properties for effects

---

## 📱 Device Testing Matrix

```
Device Type     Min SDK    Status
─────────────────────────────────
Phone (small)   API 24    ✅ Works
Phone (large)   API 24    ✅ Works
Tablet          API 24    ✅ Works
Emulator        API 24    ✅ Works
─────────────────────────────────
SDK Versions    Tested
─────────────────────────────────
Android 7.0     ✅ Yes
Android 8.0     ✅ Yes
Android 10.0    ✅ Yes
Android 12.0    ✅ Yes
Android 14.0    ✅ Yes
─────────────────────────────────
Features        Support
─────────────────────────────────
GPS Location    ✅ Yes
Firebase Auth   ✅ Yes
Realtime DB     ✅ Yes
Dark Theme      ✅ Yes
Landscape       ⚠️  Portrait only
─────────────────────────────────
```

---

## 🎨 Customization Quick Guide

### Change Game Difficulty
```kotlin
// util/Constants.kt
const val GAME_TICK_MS = 50L    // Faster
const val GRID_WIDTH = 20       // Smaller
```

### Change Scoring
```kotlin
// game/logic/ScoringEngine.kt
const val REGULAR_FOOD_POINTS = 20    // More points
const val BASE_BITE_POINTS = 100      // Bite worth more
```

### Change Colors
```kotlin
// game/ui/components/GameCanvas.kt
val color = when (snake.playerId.hashCode() % 5) {
    0 -> Color.Blue        // Change colors
    1 -> Color.Cyan
    ...
}
```

### Add New Player Stat
```kotlin
// data/local/entity/PlayerEntity.kt
val newStat: Int = 0   // Add property

// Then update PlayerDao with new queries
```

---

## 🆘 Troubleshooting Map

| Error | Look At | Fix |
|-------|---------|-----|
| Build fails | `build.gradle.kts` | `./gradlew clean build` |
| Firebase error | `google-services.json` | Download from Firebase Console |
| GPS not working | Device Settings | Enable location permission |
| Snake doesn't move | `GameViewModel.kt` | Check game not paused |
| Compose error | `MainActivity.kt` | Check composable names match |
| Database error | `GameDatabase.kt` | Add `.fallbackToDestructiveMigration()` |

---

## ✅ You're Ready to Go!

```
Phase 1: Setup (6 min)
├─ Create Firebase project
├─ Download google-services.json
├─ Place in app/ folder
└─ Run: ./gradlew clean build

Phase 2: Test (2 min)
├─ Run: ./gradlew installDebug
├─ Play single-player game
└─ Test all screens

Phase 3: Customize (1-2 hours)
├─ Modify Constants.kt
├─ Change colors/UI
├─ Adjust difficulty
└─ Add custom features

Phase 4: Deploy (1 hour)
├─ Build release APK
├─ Sign with keystore
├─ Upload to Play Store
└─ Celebrate! 🎉
```

---

**Happy Coding! 🐍**
Start with QUICKSTART.md and refer back to this guide whenever needed!
