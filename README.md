# Password Manager

A secure, user-friendly Android password manager application designed by OPH Security. This app helps you safely store and manage your passwords with strong encryption and a clean, modern interface.

![App Icon](app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png)

## Features

- 🔐 **Secure Password Storage**: All passwords are encrypted using industry-standard encryption
- 👥 **Multi-User Support**: Each user has their own separate encrypted database
- 🎨 **Modern Material Design**: Clean and intuitive user interface
- 🔍 **Easy Password Management**: Quick access to all your stored passwords
- 🔄 **Password Generator**: Create strong, unique passwords
- 🔒 **Master Password Protection**: All data is secured behind a master password
- 📱 **Adaptive Icon Support**: Modern Android icon with monochrome support

## Security Features

- AES-256 encryption for all stored passwords
- Secure password hashing using modern algorithms
- Separate encrypted databases for each user
- Auto-lock functionality for added security
- No cloud storage - all data stays on your device

## Installation

1. Download the latest APK from the releases section
2. Enable "Install from Unknown Sources" in your Android settings
3. Open the APK file to install
4. Create your account with a strong master password

## Usage

1. **First Launch**
   - Create a new account with a username and master password
   - Your master password cannot be recovered, so remember it!

2. **Adding Passwords**
   - Tap the + button to add a new password
   - Fill in the title, username, and password
   - Save to encrypt and store

3. **Viewing Passwords**
   - All your saved passwords appear on the main screen
   - Tap any entry to view or edit
   - Use the search function to find specific passwords

4. **Security**
   - The app automatically locks after inactivity
   - Your master password is required to view any stored passwords
   - Each user's data is completely separate and secure

## Technical Details

- **Minimum Android Version**: 7.0 (API Level 24)
- **Target Android Version**: 14 (API Level 34)
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room (SQLite)
- **Encryption**: AndroidX Security Crypto
- **UI**: Material Design 3 Components

## Development

Built with modern Android development tools and practices:
- Kotlin
- Coroutines for asynchronous operations
- Room Database
- ViewModel and LiveData
- DataStore for preferences
- Material Design 3
- AndroidX Security

## Privacy

This app:
- Does not collect any user data
- Does not require internet permission
- Stores all data locally on your device
- Uses strong encryption for all sensitive data

## License

Copyright © 2024 OPH Security. All rights reserved.

## Credits

Designed and developed by OPH Security.

## Support

For support, bug reports, or feature requests, please create an issue in the repository or contact OPH Security support. 
