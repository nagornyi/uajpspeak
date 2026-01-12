# SayUA - Ukrainian Phrasebook App

A comprehensive Ukrainian phrasebook app designed for English, German, and Japanese speakers. This Android application helps users learn essential Ukrainian phrases with audio pronunciation, phonetic transliteration, and an intuitive categorized interface.

## Features

### 🗣️ Multi-Language Support
- **Three source languages**: English, German (Deutsch), and Japanese (日本語)
- Automatic language detection based on device locale on first launch
- Easy language switching through the settings menu
- All UI elements (menus, navigation, buttons) fully localized

### 📚 Comprehensive Phrase Categories
- **Greetings & Basic Phrases**: Essential everyday expressions
- **Signs & Common Words**: Important vocabulary for navigation
- **Troubleshooting**: Phrases for when you need help
- **Transportation**: Getting around Ukraine
- **Directions**: Finding your way
- **Hotel**: Accommodation-related phrases
- **Numbers, Time, Weekdays, Months**: Essential temporal vocabulary
- **Colors**: Color vocabulary
- **Restaurant**: Dining and food-related phrases
- **Love**: Romantic expressions
- **Shopping & Clothing**: Retail and fashion vocabulary
- **Drugstore**: Health and pharmacy phrases
- **Driving**: Transportation vocabulary
- **Bank**: Financial transactions

### 🎯 Key Functionality
- **Text-to-Speech**: Native Ukrainian pronunciation using Android TTS
- **Phonetic Transliteration**: See how to pronounce Ukrainian words
- **Search**: Quickly find specific phrases across all categories
- **Ukrainian Alphabet Guide**: Learn the Ukrainian alphabet with pronunciation help
- **Gender-Specific Forms**: Toggle between masculine and feminine phrase variants where applicable
- **Detailed View**: Tap any phrase to see enlarged text with country flags and phonetic guides
- **Offline Access**: No internet connection required after installation

### 🎨 User Interface
- **Material Design**: Clean, modern Android interface
- **Dark Theme**: Comfortable navigation drawer with themed styling
- **Smooth Vector Icons**: All icons (flags, alphabet, speaker, eye) are crisp vector drawables
- **Wave Pattern Placeholders**: Professional loading states for ad banners
- **Country Flags**: Visual indicators showing source language (🇬🇧 🇩🇪 🇯🇵) and Ukrainian (🇺🇦)

### 📱 Technical Features
- **Adaptive Icons**: Modern launcher icons with Ukrainian flag colors (blue and yellow)
- **AdMob Integration**: Banner ads on main screen and detailed phrase view
- **Locale Persistence**: Remembers your language preference across app restarts
- **Back Navigation**: Intuitive navigation with proper back stack handling
- **Search Highlighting**: Easy-to-use search with clear visual feedback

## Usage
1. **Select Your Language**: On first launch, the app detects your device language (English, German, or Japanese)
2. **Browse Categories**: Use the navigation drawer to explore different phrase categories
3. **Listen to Pronunciation**: Tap any phrase to hear it spoken in Ukrainian
4. **View Details**: Tap phrases to see enlarged text with phonetic transliteration
5. **Search**: Use the search icon to find specific phrases quickly
6. **Learn the Alphabet**: Access the alphabet guide from the menu

## Building from Source
```bash
# Clone the repository
git clone <repository-url>
cd uajpspeak

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Requirements
- Android 5.0 (Lollipop) or higher
- Ukrainian TTS data (downloaded automatically or via device settings)
- ~10 MB storage space

## Technology Stack
- **Language**: Kotlin
- **UI Framework**: Android SDK with Material Design components
- **Text-to-Speech**: Android TTS Engine
- **Ads**: Google AdMob
- **Build System**: Gradle
- **Vector Graphics**: Android VectorDrawable (XML)

## Assets Attribution
Icons and flags are sourced from Wikimedia Commons (public domain or permissive license).

## License
See LICENSE file for details.

## Support
For issues or questions, please open an issue on the GitHub repository.

---

**Made with ❤️ for Ukrainian language learners and tourists**
