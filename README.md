# Snake Game - Multiplayer Edition

A fully-featured Android Snake game built with Kotlin, Jetpack Compose, and Firebase. Play solo or compete with others in real-time multiplayer mode with GPS location tracking.

## Features

### Core Gameplay
- **Single-Player Mode**: Classic snake game with increasing difficulty
- **Multiplayer Mode**: Real-time competitive snake battles with other players
- **Scoring System**:
  - Regular food: +10 points
  - Bonus food (20% spawn rate): +50 points
  - Biting opponent snake: +50 base points + 5 points per extra segment
  - Player loses when all snake segments are consumed

### Multiplayer Features
- **GPS Location Tracking**: Players must enable GPS to play multiplayer
- **Geohash-based Ranking**: Location-based player rankings and proximity calculations
- **Online Status Notifications**: Real-time notifications when players come online
- **Player Rankings**: Track top players by score and location
- **Game History**: Local and cloud-based game records

### Technical Stack
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Backend Services**: Firebase (Auth + Realtime Database)
- **Location Services**: Google Play Services
- **Serialization**: kotlinx.serialization

## Project Structure

```
app/src/main/java/com/example/main_snake_game/
├── game/
│   ├── model/                          # Data models
│   │   ├── Point.kt
│   │   ├── Snake.kt
│   │   ├── Food.kt
│   │   ├── Player.kt
│   │   ├── GameEvent.kt
│   │   └── GameState.kt
│   ├── logic/                          # Game logic
│   │   ├── GameEngine.kt              # Main game loop & updates
│   │   ├── CollisionDetector.kt       # Collision detection
│   │   └── ScoringEngine.kt           # Score calculations
│   └── ui/
│       ├── screens/                    # Screen composables
│       │   ├── AuthenticationScreen.kt
│       │   ├── MenuScreen.kt
│       │   ├── SinglePlayerGameScreen.kt
│       │   └── MultiplayerLobbyScreen.kt
│       ├── components/                 # Reusable UI components
│       │   ├── GameCanvas.kt
│       │   ├── ScoreBoard.kt
│       │   └── GameControls.kt
│       └── viewmodel/                  # ViewModels
│           ├── AuthViewModel.kt
│           ├── GameViewModel.kt
│           └── MultiplayerViewModel.kt
├── data/
│   ├── local/
│   │   ├── entity/                    # Room entities
│   │   │   ├── PlayerEntity.kt
│   │   │   └── GameRecordEntity.kt
│   │   ├── dao/                       # Data Access Objects
│   │   │   ├── PlayerDao.kt
│   │   │   └── GameRecordDao.kt
│   │   └── GameDatabase.kt
│   ├── remote/
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   └── DTOs.kt
│   │   └── service/                   # Remote services
│   │       ├── FirebaseService.kt
│   │       ├── LocationService.kt
│   │       └── WebSocketService.kt
│   └── repository/                    # Repository pattern
│       ├── AuthRepository.kt
│       ├── PlayerRepository.kt
│       ├── GameRepository.kt
│       └── LocationRepository.kt
├── di/                                # Dependency Injection
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── ServiceModule.kt
├── util/                              # Utilities
│   ├── Constants.kt
│   └── GeoHashUtils.kt
├── SnakeGameApplication.kt            # Application class
└── MainActivity.kt                    # Main activity with navigation
```

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 (API 24) or higher
- Google Play Services enabled
- Firebase project

### 1. Clone the Repository
```bash
git clone <repository-url>
cd main_snake_game
```

### 2. Firebase Setup
1. Create a Firebase project at [firebase.google.com](https://firebase.google.com)
2. Enable Authentication (Anonymous and Email/Password)
3. Enable Realtime Database
4. Download `google-services.json` from Firebase Console
5. Place it in the `app/` directory

### 3. Update Configuration
Edit `app/google-services.json` with your Firebase credentials.

### 4. Build and Run
```bash
./gradlew build
./gradlew installDebug
```

Or use Android Studio:
- Open the project in Android Studio
- Click "Run" or press Shift + F10

## Game Controls

### Single Player / Multiplayer Game Screen

**Direction Controls** (D-Pad):
- ↑ (Up): Move snake up
- ↓ (Down): Move snake down
- ← (Left): Move snake left
- → (Right): Move snake right

**Game Controls**:
- **Pause**: Pauses the current game
- **Resume**: Resumes a paused game
- **Stop**: Ends the game and returns to menu

## Game Rules

### Single Player
1. Guide your snake to eat food on the grid
2. Each food consumed increases your score and snake length
3. Avoid running into yourself (game over)
4. Try to maximize your score!

### Multiplayer
1. Connect with GPS location before starting
2. Players are matched with others in the lobby
3. Eat food to grow your snake and gain points
4. Bite other players' snakes to earn bonus points
5. Last player alive wins the round
6. Scores are tracked by location-based ranking

## Scoring Details

| Action | Points |
|--------|--------|
| Eat regular food | +10 |
| Eat bonus food | +50 |
| Bite opponent (base) | +50 |
| Bonus per segment consumed | +5 |
| Win multiplayer game | Variable (based on opponents) |

## API Endpoints / Firebase Structure

### Realtime Database Structure
```
/players
  /{userId}
    - name: String
    - score: Int
    - foodEaten: Int
    - snakeBites: Int
    - isOnline: Boolean
    - latitude: Double
    - longitude: Double
    - geohash: String
    - lastUpdated: Long

/games
  /{gameId}
    - mode: String (SINGLE_PLAYER, MULTI_PLAYER)
    - players: Map<String, String>
    - startTime: Long
    - endTime: Long
    - winner: String
```

## Architecture Details

### MVVM Pattern
- **View**: Jetpack Compose screens and components
- **ViewModel**: Manages UI state and business logic
- **Model**: Data classes representing game entities

### Repository Pattern
- Single source of truth for data
- Abstracts data sources (local DB, Firebase, API)
- Enables easy testing and switching implementations

### Dependency Injection (Hilt)
- Automatic dependency management
- Singleton services (GameEngine, Repositories)
- Scope-aware component lifecycle

## Game Loop
```
1. Update tick counter
2. Move all snakes based on input direction
3. Check collisions:
   - Food collision → grow snake, spawn new food
   - Self collision → mark snake as dead
   - Snake-to-snake collision → award points
4. Remove dead snakes
5. Check win condition (≤1 alive snake)
6. Broadcast game state update
7. Wait for next tick (100ms default)
```

## Offline vs Online

### Offline (Single Player)
- Game runs locally on device
- No internet required
- Game state stored in Room database
- Local high scores maintained

### Online (Multiplayer)
- Requires internet connection
- GPS location must be enabled
- Player data synced to Firebase
- Real-time game updates via shared state
- Player online status tracked

## Known Limitations
- Current version uses in-memory event flow for multiplayer sync
- WebSocket implementation is placeholder (can be enhanced with Socket.io or OkHttp WebSocket)
- Location precision set to 6-character geohash (±1.2 km)
- Game tick rate fixed at 100ms

## Future Enhancements
- [ ] Implement actual WebSocket communication for better multiplayer sync
- [ ] Add power-ups and special items
- [ ] Implement proper matchmaking system
- [ ] Add sound effects and music
- [ ] Create tournament/league system
- [ ] Add social features (friends, teams)
- [ ] Implement bot opponents
- [ ] Add more game modes (survival, capture the flag, etc.)
- [ ] Mobile optimization (variable grid sizes)
- [ ] Leaderboard with global rankings

## Troubleshooting

### Build Errors
**Issue**: Hilt annotation processing errors
**Solution**: Run `./gradlew clean build` and rebuild

**Issue**: Google Play Services location not found
**Solution**: Ensure you have Google Play Services installed via SDK Manager

### Runtime Errors
**Issue**: Firebase connection fails
**Solution**: Verify `google-services.json` is properly configured and in `app/` directory

**Issue**: Location permission denied
**Solution**: Grant location permissions in app settings (Settings > Apps > Snake Game > Permissions)

### Game Issues
**Issue**: Snake doesn't move
**Solution**: Ensure game is not paused; check if direction input is registering

**Issue**: Multiplayer players not visible
**Solution**: Ensure GPS is enabled and location services are active; wait for location sync

## Performance Tips
- Reduce grid size for smoother gameplay on low-end devices
- Disable animations on old devices
- Clear game records periodically to free up database space
- Use WiFi for multiplayer for better latency

## Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Contact
For questions or issues, please open an issue on GitHub.

---

**Built with ❤️ using Kotlin & Jetpack Compose**
