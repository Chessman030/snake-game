# 🔥 Firebase Setup - Step by Step

## 📋 What This Guide Covers

- Creating a Firebase project
- Downloading configuration file
- Setting up Authentication
- Enabling Realtime Database
- Configuring security rules

**Time Required**: 10-15 minutes

---

## 🚀 Step-by-Step Instructions

### Step 1: Go to Firebase Console

1. Open https://console.firebase.google.com/ in your browser
2. Sign in with your Google account (create one if needed)
3. Click "Add Project" button

![Firebase Console](https://console.firebase.google.com/project)

---

### Step 2: Create Project

1. **Project Name**: Enter `snake-game-multiplayer`
2. Click "Continue"

```
Project Name: snake-game-multiplayer
Analytics:   [ ] Enable Google Analytics (optional)
```

3. Click "Create project"
4. Wait 1-2 minutes for project initialization

---

### Step 3: Add Android App to Project

1. Click the "Add app" button (or click Android icon)
2. Select "Android"

```
Your Android package name: com.example.main_snake_game
Your app nickname:          Snake Game
SHA-1 certificate hash:     [Leave empty for now]
```

3. Click "Register app"

---

### Step 4: Download google-services.json

1. After clicking "Register app", you'll see a download prompt
2. Click "Download google-services.json"
3. Save the file to your computer

```
File location: ~/Downloads/google-services.json
```

4. Move to your project:
```bash
cp ~/Downloads/google-services.json ~/path/to/main_snake_game/app/
```

5. In Android Studio, you'll see a prompt "Gradle files have changed"
   - Click "Sync Now"

---

### Step 5: Enable Anonymous Authentication

1. Go to "Authentication" section (left sidebar)
2. Click "Get started"
3. Look for "Anonymous" sign-in method
4. Click on it
5. Toggle "Enable" to ON
6. Click "Save"

```
Status: Enabled ✓
```

---

### Step 6: Enable Email/Password Authentication

1. Still in Authentication section
2. Click "Email/Password" in the list
3. Toggle "Enable" to ON
4. Toggle "Email link (passwordless sign-in)" to OFF (not needed)
5. Click "Save"

```
Status: Enabled ✓
```

---

### Step 7: Create Realtime Database

1. Go to "Realtime Database" section (left sidebar)
2. Click "Create Database"
3. Choose location closest to you (e.g., "us-central1")
4. Click "Next"
5. Select "Start in test mode" (for development)
6. Click "Enable"

```
Database URL: https://snake-game-multiplayer.firebaseio.com/
Status:       Test mode (⚠️ FOR DEV ONLY)
```

Wait for initialization (30 seconds)

---

### Step 8: Set Security Rules

1. Go to "Realtime Database"
2. Click "Rules" tab at the top
3. Clear existing rules and paste:

```json
{
  "rules": {
    "players": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid || !root.child('players').child($uid).exists()"
      }
    },
    "games": {
      "$gameId": {
        ".read": true,
        ".write": true
      }
    },
    ".read": false,
    ".write": false
  }
}
```

4. Click "Publish"

```
Status: Published ✓
```

---

### Step 9: Verify in Android Studio

1. Open Android Studio
2. Open `app/build.gradle.kts`
3. Should show no errors about Firebase
4. Sync Gradle if needed

```bash
./gradlew sync
```

---

### Step 10: Test Connection

1. Build the project:
```bash
./gradlew build
```

2. Run on emulator/device:
```bash
./gradlew installDebug
```

3. In the app:
   - Click "Play as Guest"
   - You should be authenticated
   - Check Firebase Console > Authentication > Users
   - You should see your session!

---

## ✅ Verification Checklist

- [ ] Project created: `snake-game-multiplayer`
- [ ] google-services.json downloaded and placed in `app/`
- [ ] Anonymous authentication enabled
- [ ] Email/Password authentication enabled
- [ ] Realtime Database created
- [ ] Security rules published
- [ ] App builds without errors
- [ ] App installs successfully
- [ ] Can sign in as guest
- [ ] User appears in Firebase Console

---

## 🐛 Troubleshooting

### Issue: "Failed to connect to Firebase"
**Solution**:
```
1. Verify google-services.json is in app/ folder
2. Verify package name matches: com.example.main_snake_game
3. Check internet connection
4. Rebuild project: ./gradlew clean build
```

### Issue: "google-services.json not found"
**Solution**:
```bash
# Verify file location
ls -la app/google-services.json

# Should output:
# -rw-r--r-- app/google-services.json
```

### Issue: "Authentication disabled"
**Solution**:
```
1. Go to Firebase Console
2. Authentication > Sign-in method
3. Make sure "Anonymous" is Enabled (toggle ON)
4. Click Save
```

### Issue: "Database permission denied"
**Solution**:
```
1. Go to Realtime Database
2. Click Rules tab
3. Verify rules are published (green checkmark)
4. Test in Security Rules Simulator
```

### Issue: "Invalid package name"
**Solution**:
```
Your registered package: com.example.main_snake_game
In your code: android/app/build.gradle.kts
    namespace = "com.example.main_snake_game"
    applicationId = "com.example.main_snake_game"

Make sure they match exactly!
```

---

## 🔐 Security Notes

### Development (Test Mode - Current)
```json
{
  ".read": true,
  ".write": true
}
```
- ✅ Good for development
- ❌ Not secure for production

### Production Rules Example
```json
{
  "players": {
    "$uid": {
      ".read": true,
      ".write": "$uid === auth.uid"
    }
  }
}
```

**IMPORTANT**: Before deploying to Play Store, update security rules!

---

## 📊 Firebase Console Quick Reference

| Feature | Location | Status |
|---------|----------|--------|
| Authentication | Left sidebar | ✅ Configured |
| Realtime Database | Left sidebar | ✅ Configured |
| Users | Auth > Users tab | Will show your sessions |
| Database Data | Realtime DB > Data tab | Will show game data |
| Security Rules | Realtime DB > Rules | ✅ Published |

---

## 🔄 Firebase Data Structure

When you play the game, Firebase will automatically create:

```json
{
  "players": {
    "user_id_123": {
      "name": "Player_ABC",
      "score": 150,
      "foodEaten": 15,
      "snakeBites": 2,
      "isOnline": true,
      "latitude": 40.7128,
      "longitude": -74.0060,
      "geohash": "dr5regw",
      "lastUpdated": 1704067200000
    }
  },
  "games": {
    "game_id_456": {
      "mode": "SINGLE_PLAYER",
      "players": {
        "user_id_123": "Player_ABC"
      },
      "startTime": 1704067200000,
      "endTime": 1704067500000
    }
  }
}
```

---

## 📱 Test Your Setup

### Test 1: Guest Sign In
```
1. Run app
2. Click "Play as Guest"
3. Check Firebase Console > Auth > Users
4. You should see your session
✅ Passed
```

### Test 2: Database Write
```
1. Play single-player game
2. Eat some food (get points)
3. Check Firebase Console > Database
4. You should see your player data
✅ Passed
```

### Test 3: Sign Out & Back In
```
1. Play game
2. Close app
3. Reopen app
4. Same user should appear
✅ Passed
```

---

## 🎓 Understanding the Setup

### Why Firebase?
- **Authentication**: Easy user login (email, anonymous, social)
- **Database**: Real-time data sync across devices
- **Integration**: Works seamlessly with Android
- **Scalable**: Grows with your app

### Why Realtime Database?
- **Real-time**: Updates instantly across devices
- **Simple**: No backend server needed
- **Scalable**: Handles millions of users
- **Free tier**: Good for development

### Security Rules Breakdown
```json
"players": {
  "$uid": {                    // Each player has their own folder
    ".read": true,             // Anyone can read
    ".write": "$uid === auth.uid"  // Only owner can write
  }
}
```

---

## 🚀 Next Steps

After completing this guide:

1. ✅ Your Firebase project is ready
2. ✅ Your app can connect to Firebase
3. ✅ You can build and run the app
4. ✅ You can test single-player
5. ✅ You can test multiplayer lobby

**Proceed to**: `QUICKSTART.md` for building the app

---

## 📞 Firebase Support

- Official Docs: https://firebase.google.com/docs/android/setup
- Console: https://console.firebase.google.com/
- Support: https://firebase.google.com/support

---

## 🎉 Firebase Setup Complete!

You now have a fully configured Firebase backend ready for your Snake Game!

Next: Build and run the app → See `QUICKSTART.md`
