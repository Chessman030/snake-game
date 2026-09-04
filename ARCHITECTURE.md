# Snake Game - Architecture & API Reference

## System Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│          Jetpack Compose UI Layer                   │
│  (Screens, Components, Navigation)                  │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│         ViewModel Layer (StateFlow)                 │
│  (AuthVM, GameVM, MultiplayerVM)                    │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│      Repository Pattern + Business Logic            │
│  (AuthRepo, GameRepo, PlayerRepo, LocationRepo)    │
│         + Game Engines                              │
└────────────────┬────────────────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼──────────────┐  ┌───────▼──────────────┐
│   Local Layer    │  │  Remote Layer       │
│  (Room Database) │  │  (Firebase, GPS)    │
└──────────────────┘  └─────────────────────┘
```

## Module Breakdown

### 1. Model Layer (`game/model/`)

#### Point.kt
Represents a coordinate on the game grid.

```kotlin
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(...)
    operator fun minus(other: Point) = Point(...)
    fun distance(other: Point): Float = ...
}
```

**Usage**:
```kotlin
val head = Point(15, 15)
val newPosition = head + Point(1, 0)
```

#### Snake.kt
Represents a player's snake.

```kotlin
data class Snake(
    val playerId: String,
    val segments: List<Point>,        // Head is first
    val direction: Direction,         // Current direction
    val nextDirection: Direction,     // Next direction (input buffered)
    val isAlive: Boolean,
    val playerName: String
)
```

**Key Methods**:
- `head()`: Returns first segment (head position)
- `tail()`: Returns last segment (tail position)
- `length()`: Returns number of segments

#### Food.kt
Represents food on the grid.

```kotlin
data class Food(
    val position: Point,
    val type: FoodType = REGULAR  // REGULAR or BONUS
)
```

#### Player.kt
Represents a player's profile and stats.

```kotlin
data class Player(
    val id: String,
    val name: String,
    val score: Int = 0,
    val foodEaten: Int = 0,
    val snakeBites: Int = 0,
    val isOnline: Boolean = false,
    val isAlive: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = ""
)
```

#### GameEvent.kt
Sealed class for all game events.

```kotlin
sealed class GameEvent {
    data class FoodEaten(val playerId: String, val points: Int, ...)
    data class SnakeBit(val biterPlayerId: String, val points: Int, ...)
    data class PlayerJoined(val playerId: String, ...)
    data class PlayerLeft(val playerId: String, ...)
    data class GameOver(val winners: List<String>, ...)
    data class SnakeStateUpdate(val snake: Snake, ...)
}
```

#### GameState.kt
Complete snapshot of game at any moment.

```kotlin
data class GameState(
    val gameId: String,
    val mode: GameMode,           // SINGLE_PLAYER or MULTI_PLAYER
    val snakes: Map<String, Snake>,
    val food: Food?,
    val allPlayers: Map<String, Player>,
    val isGameRunning: Boolean,
    val gridWidth: Int,
    val gridHeight: Int,
    val currentTick: Long,
    val gameStartTime: Long,
    val winner: Player?
)
```

---

### 2. Game Logic Layer (`game/logic/`)

#### GameEngine.kt
Main game loop and state management.

```kotlin
class GameEngine(
    gridWidth: Int = 30,
    gridHeight: Int = 30,
    scoringEngine: ScoringEngine,
    collisionDetector: CollisionDetector
)
```

**Key Methods**:

```kotlin
fun updateGameState(gameState: GameState): Pair<GameState, List<GameEvent>>
```
- Called once per game tick (100ms default)
- Returns updated state and list of events that occurred
- Handles movement, collisions, scoring

```kotlin
fun changeDirection(playerId: String, gameState: GameState, newDirection: Direction): GameState
```
- Updates snake's next direction
- Prevents invalid direction changes (180° turns)
- Returns updated game state

**Algorithm**:
1. Move all snakes based on `nextDirection`
2. Process collisions (food, self, other snakes)
3. Remove dead snakes
4. Check win condition
5. Generate new food if eaten
6. Return updated state + events

#### CollisionDetector.kt
Detects collisions between game objects.

```kotlin
class CollisionDetector
```

**Methods**:
```kotlin
fun checkFoodCollision(snakeHead: Point, food: Food): Boolean
fun checkSelfCollision(snake: Snake): Boolean
fun checkSnakeCollision(head: Point, otherSnake: Snake): Boolean
fun checkWallCollision(head: Point, gridWidth: Int, gridHeight: Int): Boolean
```

#### ScoringEngine.kt
Calculates points for game events.

```kotlin
class ScoringEngine {
    companion object {
        const val REGULAR_FOOD_POINTS = 10
        const val BONUS_FOOD_POINTS = 50
        const val BASE_BITE_POINTS = 50
        const val BITE_LENGTH_MULTIPLIER = 5
    }
}
```

**Scoring Formula**:
```
Bite Points = 50 + (opponent_length - 3) * 5
Example: Bite 5-segment snake = 50 + (5-3)*5 = 60 points
```

---

### 3. Data Layer

#### Local Storage (`data/local/`)

**Entities**:
```kotlin
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val totalScore: Int,
    val totalFoodEaten: Int,
    val totalBites: Int,
    val gamesPlayed: Int,
    val gameWins: Int,
    val latitude: Double,
    val longitude: Double,
    val geohash: String,
    val lastUpdated: Long
)

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val gameId: String,
    val playerId: String,
    val playerName: String,
    val score: Int,
    val foodEaten: Int,
    val snakeBites: Int,
    val gameMode: String,
    val duration: Long,
    val isWinner: Boolean,
    val timestamp: Long
)
```

**DAOs**:
```kotlin
@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    @Query("SELECT * FROM players ORDER BY totalScore DESC LIMIT :limit")
    fun getTopPlayers(limit: Int): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: String): PlayerEntity?
}

@Dao
interface GameRecordDao {
    @Insert
    suspend fun insertGameRecord(record: GameRecordEntity)
    
    @Query("SELECT * FROM game_records WHERE playerId = :playerId")
    fun getPlayerGameRecords(playerId: String): Flow<List<GameRecordEntity>>
}
```

#### Remote Services (`data/remote/service/`)

**FirebaseService.kt**:
```kotlin
class FirebaseService(
    firebaseAuth: FirebaseAuth,
    firebaseDatabase: FirebaseDatabase
)
```

Methods:
- `signInAnonymously()`: Returns userId or null
- `signInWithEmail(email, password)`: Returns userId or null
- `createUserWithEmail(email, password)`: Returns userId or null
- `updatePlayerOnlineStatus(playerId, isOnline)`: Sets online status
- `updatePlayerLocation(playerId, lat, lon, geohash)`: Updates location
- `getCurrentUserId()`: Returns current user ID
- `logout()`: Signs out current user

**LocationService.kt**:
```kotlin
class LocationService(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
)
```

Methods:
- `getLocationUpdates()`: Flow<Location> - continuous location updates
- `getLastKnownLocation()`: Location? - cached location

**WebSocketService.kt**:
```kotlin
class WebSocketService
```

Methods:
- `observeGameEvents()`: Flow<GameEvent>
- `sendGameEvent(event: GameEvent)`: Broadcasts event

---

### 4. Repository Layer (`data/repository/`)

#### AuthRepository
```kotlin
class AuthRepository(firebaseService: FirebaseService)

suspend fun signInAnonymously(): String?
suspend fun signInWithEmail(email: String, password: String): String?
suspend fun createUserWithEmail(email: String, password: String): String?
fun getCurrentUserId(): String?
fun logout()
```

#### PlayerRepository
```kotlin
class PlayerRepository(playerDao: PlayerDao, firebaseService: FirebaseService)

fun getTopPlayers(limit: Int): Flow<List<PlayerEntity>>
fun getAllPlayers(): Flow<List<PlayerEntity>>
suspend fun savePlayer(player: PlayerEntity)
suspend fun updatePlayer(player: PlayerEntity)
suspend fun getPlayerById(playerId: String): PlayerEntity?
suspend fun updatePlayerOnlineStatus(playerId: String, isOnline: Boolean)
suspend fun updatePlayerLocation(playerId, lat, lon, geohash)
```

#### GameRepository
```kotlin
class GameRepository(gameRecordDao: GameRecordDao)

suspend fun saveGameRecord(record: GameRecordEntity)
fun getPlayerGameRecords(playerId: String): Flow<List<GameRecordEntity>>
fun getGameRecords(gameId: String): Flow<List<GameRecordEntity>>
fun getRecentGames(limit: Int): Flow<List<GameRecordEntity>>
```

#### LocationRepository
```kotlin
class LocationRepository(locationService: LocationService)

fun getLocationUpdates(): Flow<Pair<Double, String>>  // (latitude, geohash)
suspend fun getLastKnownLocation(): Pair<Double, Double>?
```

---

### 5. ViewModel Layer (`game/ui/viewmodel/`)

#### AuthViewModel
Manages authentication flow and user session.

```kotlin
class AuthViewModel(
    authRepository: AuthRepository,
    playerRepository: PlayerRepository
) : ViewModel()
```

**State**:
- `isLoading`: Boolean - sign-in progress
- `currentUserId`: String? - authenticated user ID
- `errorMessage`: String? - authentication error

**Methods**:
```kotlin
fun signInAnonymously()
fun signInWithEmail(email: String, password: String)
fun logout()
fun clearError()
```

#### GameViewModel
Manages single-player game state and logic.

```kotlin
class GameViewModel(
    gameEngine: GameEngine,
    gameRepository: GameRepository,
    playerRepository: PlayerRepository,
    locationRepository: LocationRepository
) : ViewModel()
```

**State**:
- `gameState`: GameState? - current game snapshot
- `gameEvents`: List<GameEvent> - events from last tick
- `isGameRunning`: Boolean - game active status

**Methods**:
```kotlin
fun startSinglePlayerGame(playerId: String, playerName: String)
fun changeDirection(direction: Direction)
fun pauseGame()
fun resumeGame()
fun stopGame()
```

**Game Loop**:
- Runs on background coroutine
- Updates every 100ms
- Collects events from GameEngine
- Updates StateFlow with new game state
- Auto-stops when winner determined

#### MultiplayerViewModel
Manages multiplayer lobby and player discovery.

```kotlin
class MultiplayerViewModel(
    playerRepository: PlayerRepository,
    locationRepository: LocationRepository
) : ViewModel()
```

**State**:
- `onlinePlayers`: List<PlayerEntity> - available players
- `currentPlayerLocation`: Pair<Double, Double>? - GPS coordinates
- `isLocationReady`: Boolean - location acquired

**Methods**:
```kotlin
fun startTrackingLocation(playerId: String)
fun loadOnlinePlayers()
```

---

### 6. UI Layer (`game/ui/`)

#### Screens

**AuthenticationScreen**
- Email/password input
- Anonymous sign-in
- Loading & error states
- Account creation flow

**MenuScreen**
- Single player button
- Multiplayer button
- Game title

**SinglePlayerGameScreen**
- GameCanvas component
- ScoreBoard component
- GameControls component
- Game state display

**MultiplayerLobbyScreen**
- Online players list
- GPS connection status
- Start game button
- Back to menu button

#### Components

**GameCanvas**
```kotlin
@Composable
fun GameCanvas(gameState: GameState, cellSize: Float = 12f)
```
- Renders game grid
- Draws snakes with different colors
- Renders food items

**ScoreBoard**
```kotlin
@Composable
fun ScoreBoard(gameState: GameState?)
```
- Shows all players
- Displays scores, food eaten, bites

**GameControls**
```kotlin
@Composable
fun GameControls(
    onDirectionChange: (Direction) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    isPaused: Boolean
)
```
- D-Pad controls
- Pause/Resume buttons
- Stop button

---

### 7. Dependency Injection (`di/`)

#### DatabaseModule
Provides Room database, DAOs

#### RepositoryModule
Provides all repository instances

#### ServiceModule
Provides:
- Firebase (Auth, Database)
- Location Services
- WebSocket Service
- Game logic engines

---

## Data Flow Diagrams

### Single Player Game Flow
```
User Input (Direction)
        ↓
  GameViewModel.changeDirection()
        ↓
  GameEngine.changeDirection()
        ↓
  Updates Snake.nextDirection
        ↓
  Next tick: GameEngine.updateGameState()
        ↓
  Processes collisions, scoring
        ↓
  Returns (updatedState, events)
        ↓
  ViewModel updates StateFlow
        ↓
  Compose recomposes with new state
        ↓
  Screen renders new game state
```

### Authentication Flow
```
User clicks "Sign In"
        ↓
  AuthViewModel.signInWithEmail()
        ↓
  AuthRepository.signInWithEmail()
        ↓
  FirebaseService.signInWithEmail()
        ↓
  Firebase returns userId
        ↓
  PlayerRepository.savePlayer()
        ↓
  Room DB stores PlayerEntity
        ↓
  ViewModel updates currentUserId StateFlow
        ↓
  Navigation triggers to MenuScreen
```

### Location Tracking Flow
```
User selects Multiplayer
        ↓
  MultiplayerViewModel.startTrackingLocation()
        ↓
  LocationRepository.getLocationUpdates()
        ↓
  LocationService flow emits Location
        ↓
  GeoHashUtils.encode(lat, lon)
        ↓
  PlayerRepository.updatePlayerLocation()
        ↓
  FirebaseService updates Firebase
        ↓
  ViewModel sets isLocationReady = true
        ↓
  UI enables Start Game button
```

---

## Firebase Realtime Database Structure

```json
{
  "players": {
    "user_id_1": {
      "name": "Player_A",
      "score": 250,
      "foodEaten": 25,
      "snakeBites": 3,
      "isOnline": true,
      "latitude": 28.6139,
      "longitude": 77.2090,
      "geohash": "ttxemxv",
      "lastUpdated": 1704067200000
    },
    "user_id_2": {
      "name": "Player_B",
      "score": 180,
      "isOnline": false,
      ...
    }
  },
  "games": {
    "game_id_1": {
      "mode": "SINGLE_PLAYER",
      "players": {
        "user_id_1": "Player_A"
      },
      "startTime": 1704067200000,
      "endTime": 1704067500000,
      "winner": "user_id_1",
      "finalScores": {
        "user_id_1": 250
      }
    }
  }
}
```

---

## API Constants

**Game Settings** (`util/Constants.kt`):
```kotlin
const val GRID_WIDTH = 30
const val GRID_HEIGHT = 30
const val GAME_TICK_MS = 100L
const val REGULAR_FOOD_POINTS = 10
const val BONUS_FOOD_POINTS = 50
const val BASE_BITE_POINTS = 50
const val BITE_LENGTH_MULTIPLIER = 5
const val INITIAL_SNAKE_LENGTH = 3
const val DATABASE_NAME = "snake_game_db"
```

---

## Testing Strategy

### Unit Tests
- GameEngine logic
- CollisionDetector
- ScoringEngine
- GeoHashUtils

### Integration Tests
- Room DAO operations
- Firebase service calls
- ViewModel state management

### UI Tests
- Compose component rendering
- Navigation flow
- User interactions

---

## Performance Considerations

| Component | Optimization |
|-----------|--------------|
| GameEngine | Efficient collision detection algorithm |
| GameState | Immutable data for safe multi-threading |
| Room DB | Indexed queries on playerId, timestamp |
| Firebase | Realtime listeners with local caching |
| Compose | Recomposition scoped to affected state |
| Location | Batched updates, 1-second interval |

---

## Future Extensibility

1. **MultiplayerEngine**: Extend GameEngine for p2p state sync
2. **PowerUpSystem**: Add new game objects inheriting from Food
3. **ReplaySystem**: Save and replay game recordings
4. **LeaderboardSystem**: Tiered rankings by location/time
5. **MatchmakingSystem**: ELO-based player pairing

---

**Architecture designed for scalability, maintainability, and extensibility** 🐍
