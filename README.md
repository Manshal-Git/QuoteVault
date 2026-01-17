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
- **Notification Preferences**: Configurable quote notifications

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with MVI elements
- **Dependency Injection**: Hilt
- **Backend**: Supabase (Auth + Database)
- **Networking**: Ktor
- **Local Storage**: DataStore
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: 26 (Android 8)
- **Target SDK**: 36 (Android 16)

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

### 2. Build the Project Using Android Studio

#### Command Line (Option 1)

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

#### Android Studio (Option 2)
For Windows press `Shift` + `F10`

For MacOS press `Command` + `R`


## Project Structure

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

## AI approach and workflow

Divide and Conquer was my approach.

I leveraged multiple free options to complete small and independant tasks simultaneously.

I used AI to plan, design core UI, scaffold ViewModels, generate SQL schemas, refactor Compose UI, and debug Supabase.


## AI tools used
- ChatGPT ([Development Planning](https://chatgpt.com/s/t_696bb74dbc088191a7fc2a9172629de5))
- Gemini ([Seed data](https://gemini.google.com/share/0e918ebd822b))
- Claude ([Project setup + core UI and theming](https://claude.ai/share/dbc97ad4-f0f7-48d7-b179-bd7359c76b26))
- Kiro Agentic IDE (AI driven development)

## Design prototyping
[Stitch](https://stitch.withgoogle.com/projects/5889418937889202092) with
[Figma](https://www.figma.com/design/YNXthm7swVzVi6nlZ8HtPc/QuoteVault?node-id=0-1&t=MeGGjvv6SU5Zon5b-1)



## Incomplete feature status

- ✅ Home feed displaying quotes
- ✅ Browse quotes by category (minimum 5: Motivation, Love, Success, Wisdom, Humor)
- ✅ Search quotes by keyword
- ✅ Search/filter by author
- ✅ Pull-to-refresh functionality
- ✅ Loading states and empty states handled gracefully
- ❌ Pagination

###  Quote of the Day
- ✅ Displays a featured daily quote prominently on the home screen
- ✅ Shows current date for daily context
- ✅ Allows sharing and saving the daily quote to collections
- ✅ Home screen widget displays the current quote of the day
- ❌ Daily push notification delivery

### Collections
- ✅ Create custom collections to organize saved quotes
- ✅ Add and remove quotes from collections
- ✅ Default **Favorites** collection available
- ❌ Dedicated quote listing screen for viewing quotes within a specific collection



###  Theme & Personalization
- ✅ Multiple color themes supported (Indigo, Emerald, Rose)
- ✅ Light and Dark mode support
- ✅ Adjustable font sizes for improved readability
- ❌ Theme and personalization settings synced to the server profile


---

**Happy coding! 🚀**
