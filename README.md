# QuoteVault 📱✨

QuoteVault is a modern Android application for discovering, collecting, and sharing inspirational quotes. Built with Jetpack Compose and Material 3 design, it offers a personalized quote experience with user authentication, collections management, and daily quote features.

## Features

- **Quote Discovery**: Browse and discover inspirational quotes with search and category filtering
- **User Authentication**: Secure login/signup with Supabase backend
- **Collections**: Organize quotes into custom collections
- **Favorites**: Save preferred quotes for quick access
- **Daily Quote Widget**: Android home screen widget with daily inspiration
- **Quote of the Day**: Featured daily quote with overlay presentation
- **Sharing**: Share quotes as images with customizable styles
- **Profile Management**: User preferences, themes, and account settings
- **Notifications**: Configurable quote notifications

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with MVI elements
- **Dependency Injection**: Hilt
- **Backend**: Supabase (Auth + Database)
- **Networking**: Ktor
- **Local Storage**: DataStore
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 36

## Prerequisites

Before setting up the project, ensure you have:

1. **Android Studio** (latest stable version)
2. **JDK 11** or higher
3. **Android SDK** with API level 26+
4. **Git** for version control

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Manshal-Git/QuoteVault.git
cd QuoteVault
```

### 2. Android Studio Setup

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned QuoteVault directory
4. Click "OK" to open the project

### 3. SDK and Dependencies

Android Studio should automatically:
- Download required SDK versions
- Install necessary build tools
- Sync Gradle dependencies

If not automatic, you can manually:
- Go to **Tools > SDK Manager**
- Ensure Android API 26+ is installed
- Sync project with Gradle files

### 4. Build the Project

## Command Line

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Android Studio
**Windows** Shift + F10
**MacOS** Command + R

### Project Structure

```
QuoteVault/
├── app/
│   ├── src/main/java/com/example/quotevault/
│   │   ├── core/           # Core infrastructure
│   │   ├── data/           # Data layer
│   │   ├── di/             # Dependency injection
│   │   ├── navigation/     # Navigation setup
│   │   ├── ui/             # UI layer (screens & components)
│   │   └── utils/          # Utility classes
│   └── src/main/res/       # Resources (layouts, strings, etc.)
├── gradle/                 # Gradle wrapper and version catalog
└── build.gradle.kts        # Root build configuration
```

## Architecture

The app follows Clean Architecture principles with:

- **UI Layer**: Jetpack Compose screens and components
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Repositories and data sources
- **Dependency Injection**: Hilt for managing dependencies

### Key Patterns

- **MVVM**: Model-View-ViewModel architecture
- **MVI**: Model-View-Intent for state management
- **Repository Pattern**: Data layer abstraction
- **Single Activity**: One activity with Compose navigation

## Features in Detail

### Daily Quote Widget
- Home screen widget displaying daily inspirational quotes
- Automatic updates with new quotes
- Tap to open the main app

### Quote of the Day Overlay
- Featured daily quote with prominent display
- Date information for context
- Save to collections or share functionality

### Collections System
- Create custom collections to organize quotes
- Add/remove quotes from multiple collections
- Default favorites collection

### Theme Support
- Multiple color themes (Indigo, Emerald, Rose)
- Dark/Light mode support
- Customizable font sizes

---

**Happy coding! 🚀**