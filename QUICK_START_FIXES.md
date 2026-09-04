# Quick Start: Using the Fixes

## For Testing on USB Phone

### 1. Single-Player Game - No Changes Needed
Simply build and run - snake should now move smoothly with direction buttons.

### 2. Multiplayer with Real GPS
- Ensure location permissions are granted on phone
- Go to Settings → Location → Turn ON GPS
- Open app → Multiplayer → Wait 10 seconds max for GPS signal
- If GPS fails, see error message with retry count

### 3. Multiplayer without GPS (DEBUG MODE)
If you want to test multiplayer without needing GPS signal:

**Option A: Via MainActivity (Recommended for Testing)**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // ADD THIS LINE to enable debug mode for testing
    LocationRepository.DEBUG_MODE = true
    
    requestLocationPermissions()
    // ... rest of onCreate
}
```

**Option B: Via Dependency Injection**
If using Hilt to inject LocationRepository, enable debug mode in your ViewModel or before starting multiplayer:
```kotlin
// In MultiplayerViewModel or before navigation
private fun setupDebugMode() {
    locationRepository.enableDebugMode(
        latitude = 37.7749,
        longitude = -122.4194
    )
}
```

## Expected Behavior After Fixes

### Single Player
```
START → Snake spawns at (15,15) → Player taps UP → Snake head moves up 
→ Rest of snake follows → Food spawns → Snake eats food → Grows → Score +10
```

### Multiplayer (Real GPS)
```
START MULTIPLAYER → "Waiting for GPS..." → [0-10s] 
→ GPS signal acquired → isLocationReady = true 
→ Online players list appears (refreshes every 2s) 
→ Can start game
```

### Multiplayer (Debug Mode)
```
START MULTIPLAYER → "Waiting for GPS..." → [<1s, instant] 
→ GPS signal acquired (mock) → isLocationReady = true 
→ Online players list appears 
→ Can start game (all with mock location)
```

## Error Messages You Might See

| Message | Meaning | What To Do |
|---------|---------|-----------|
| "Location timeout - Retry attempt 1/3" | GPS taking too long | Wait, it will retry |
| "Unable to acquire GPS signal after 3 attempts" | GPS failed after retries | Check Settings → Location → GPS ON |
| "Failed to load players: ..." | Network/database error | Check internet connection |
| "Cannot send event: Not connected to WebSocket" | Multiplayer connection lost | Reconnection happening automatically |

## Debug Logging (Optional Enhancement)

To add logging for debugging, you can add this to GameViewModel:
```kotlin
private fun startGameLoop() {
    gameLoopJob?.cancel()
    gameLoopJob = viewModelScope.launch {
        var tickCount = 0
        while (_isGameRunning.value) {
            val currentState = _gameState.value ?: break
            
            // Optional: Log every 10 ticks
            if (++tickCount % 10 == 0) {
                Log.d("GameLoop", "Tick: $tickCount, Snake head: ${currentState.snakes.values.firstOrNull()?.head()}")
            }
            
            val (updatedState, events) = gameEngine.updateGameState(currentState)
            _gameState.value = updatedState
            _gameEvents.value = events

            if (!updatedState.isGameRunning) {
                _isGameRunning.value = false
                endGame(updatedState)
            }

            delay(Constants.GAME_TICK_MS)
        }
    }
}
```

## Monitoring Multiplayer Connection

To see WebSocket connection state in UI (optional):
```kotlin
// In your composable
val connectionState by webSocketService.connectionState.collectAsState()
val connectionError by webSocketService.connectionError.collectAsState()

if (connectionState != WebSocketService.ConnectionState.CONNECTED) {
    Text("Connection: $connectionState", color = Color.Yellow)
    connectionError?.let { 
        Text("Error: $it", color = Color.Red)
    }
}
```

## Troubleshooting

### Snake Still Not Moving?
1. Check that `changeDirection()` is being called (add breakpoint)
2. Verify `gameState.snakes` is not empty
3. Ensure `_isGameRunning.value = true`
4. Check that `GAME_TICK_MS` constant is not 0

### Multiplayer Still Showing "Waiting for GPS"?
1. Enable DEBUG_MODE for testing
2. Check Android Studio Logcat for error messages
3. Verify location permissions in phone Settings
4. Ensure GPS is turned ON in phone Settings

### Players Not Appearing in Lobby?
1. Ensure at least one other player is also in the lobby
2. Check database/Firebase has player location data
3. Enable debug mode to bypass GPS issues
4. Check `loadOnlinePlayers()` error messages

## Performance Tips

- Game loop runs at `GAME_TICK_MS = 100L` (10 FPS) - adjust if needed
- Player list refreshes at `2000L` ms - increase for less network load
- Debug mode emits updates every 1000ms - adjust `delay()` value
- Use debug mode for development, disable for production

---

**All fixes are backward compatible. No existing code needs changes.**
