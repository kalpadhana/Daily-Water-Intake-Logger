# 💧 Water_Logger — Daily Hydration Tracker
**Module: ICT3214 — Mobile Application Development Project Idea: Hydration & Wellness Tracker**

---

## 📖 Table of Contents
1. [Project Description](#project-description)
2. [Features](#features)
3. [Screens & UI](#screens--ui)
4. [Technologies Used](#technologies-used)
5. [Database Design](#database-design)
6. [Project Structure](#project-structure)
7. [Setup & Installation](#setup--installation)
8. [Team Members](#team-members)
9. [GitHub Contribution Guidelines](#github-contribution-guidelines)

---

## 📝 Project Description
**Water_Logger** is a modern Android application designed to help users build healthier hydration habits through daily water intake tracking. The app provides a clean, data-driven environment where users can set personal goals, monitor their progress with intuitive visualisations, and stay on track with a smart, personalised reminder system.

The app's design philosophy is rooted in **Clarity, Fluidity, and Motivation** — using wave animations, smooth gradients, and glassmorphism effects to turn a daily health routine into an engaging habit-building experience.

Users can register a personal account, log in securely, record their water intake, set custom hydration targets, and review their history over time — all stored locally using a SQLite database.

---

## ✨ Features

### 🔐 Authentication & Multi-User Support
- **User Registration** — Name, Email, Phone, Password with real-time validation
- **Secure Login** — Email + Password authenticated against the local SQLite database
- **BCrypt Hashing** — Industry-standard one-way password protection; passwords are NEVER stored in plain text
- **Session-Aware** — Logged-in users are auto-redirected to the Dashboard; each user sees only their own data, targets, and history
- **Duplicate Detection** — Email uniqueness enforced at the database level

### 💧 Hydration Management
- **Interactive Wave Progress** — A custom-drawn `WaterLevelView` with animated sine-waves representing real-time intake against the daily target
- **Dynamic Goal Setting** — Per-user daily targets stored in the database and adjustable via a modern dialog; summary data updates instantly
- **Quick-Add Cards** — One-tap logging for common volumes (150 ml, 250 ml, 300 ml) directly from the Dashboard
- **Custom Intake Logging** — A dedicated "Add Drink" screen for precise control over consumption entries
- **Achievement System** — Automated daily summary archiving; once the target is reached the day is marked as completed and further entries are locked

### ⏰ Smart Reminders
- **Personalised Schedule** — User-specific reminders (e.g. "After Wake-up", "Before Lunch") stored in a dedicated database table with a label, time, and enabled toggle
- **M3 Switch Toggles** — Individual reminders can be enabled or disabled without deleting them
- **Real-Time Countdown** — A background `Handler` loop updates the "Time Until Next Reminder" display every minute on the Dashboard
- **Intelligent Rollover** — Automatically calculates the next active reminder for the current day, or rolls over to the first enabled reminder the following morning

### 📊 Data Visualisation
- **Daily Analytics** — Hourly breakdown of water intake using a `BarChart` for the selected day
- **Weekly Overview** — 7-day progress tracking to monitor long-term hydration trends, displayed in a `ViewPager2` with tab navigation
- **Glassmorphism UI** — Modern "Me" profile page featuring user stats and secure session management

---

## 📱 Screens & UI

| Screen | Description | Status |
| :--- | :--- | :--- |
| **Welcome** | Animated splash screen with instant session-check redirection | ✅ Complete |
| **Get Started** | Onboarding screen with a premium hero illustration and smooth entry flow | ✅ Complete |
| **Login** | Secure entry point with BCrypt password verification | ✅ Complete |
| **Register** | User onboarding with unique constraint enforcement | ✅ Complete |
| **Dashboard** | Hero screen with Wave Animation, Info Cards, and Real-time Countdown | ✅ Complete |
| **Add Drink** | Precise input screen for logging custom water amounts with visual feedback | ✅ Complete |
| **Reminders** | Settings page with toggleable reminder schedules and Material 3 Switches | ✅ Complete |
| **History** | Interactive daily and weekly intake charts using ViewPager2 | ✅ Complete |
| **Me (Profile)** | Glassmorphism profile card with user stats and logout confirmation | ✅ Complete |

### Dashboard Features ✅
- **Wave Progress View** — Animated sine-wave fills to reflect current intake vs. daily target in real time
- **Info Cards** — Total intake today, remaining amount, and current target displayed at a glance
- **Real-Time Countdown** — Updates every minute to show time remaining until the next enabled reminder
- **Quick-Add Buttons** — One-tap cards (150 ml, 250 ml, 300 ml) for fast logging without navigating away
- **Session Management** — Auto-redirects to Login if no active session; logout available from the profile screen

### Reminders Features ✅
- **Named Reminders** — Each reminder has a descriptive label (e.g. "Morning", "Before Lunch") and a scheduled time in HH:mm format
- **Individual Toggles** — Material 3 Switch per reminder; disabled reminders are skipped by the countdown logic
- **Add & Delete** — Users can create custom reminders or remove existing ones
- **Smart Next-Reminder Logic** — Finds the earliest upcoming enabled reminder; if none remain today, rolls over to the first one tomorrow

### 📊 History Page Features ✅
- **Dual View Toggle** — Seamlessly switch between Daily and Weekly analytics using a TabLayout and ViewPager2 interface
- **Daily Hourly Breakdown** — Interactive bar charts showing exactly when you hydrated throughout the day
- **Weekly Trend Analysis** — A 7-day overview to help identify patterns and stay consistent over time
- **Date Navigation** — Easy "Previous" and "Next" controls to review past performance
- **User-Isolated Data** — Securely displays only the history belonging to the currently logged-in user

### 💧 Add Drink Page Features ✅
- **Custom Volume Input** — Precise logging for any amount of water beyond the dashboard presets
- **Visual Confirmation** — Engaging UI that confirms the amount added before saving to the database
- **Real-Time Synchronisation** — Instantly updates the dashboard progress and wave animation upon returning from the screen
- **Snapshot Recording** — Automatically captures the time and the user's current goal at the moment of logging

### 👤 Me (Profile) Page Features ✅
- **Identity Header** — Premium display of the registered Full Name and Email Address
- **Glassmorphism Design** — A modern, semi-transparent profile card with soft shadows and a rounded avatar
- **Today's Progress Summary** — A quick snapshot of current intake vs. target (e.g. "1200 / 2000 ml")
- **Secure Session Control** — A dedicated "Log Out" action with a confirmation dialog to prevent accidental exits
- **Data Privacy** — Automatically clears user credentials and session tokens upon logout

### 🎯 Target Card Features ✅
- **Dynamic Goal Display** — Shows your personalised daily water target (e.g. "2000 ml")
- **Visual Vertical Indicator** — A modern blue accent bar that provides instant visual context
- **Live Progress Percentage** — Displays exactly how much of your goal is reached in real time (e.g. "37% done")
- **Interactive Goal Setting** — Tap the card to open a modern dialog where you can instantly update your daily target
- **Database Sync** — Any changes made are immediately saved to your user profile in the database

---

## 🛠️ Technologies Used

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 11 | Primary programming language |
| **Android SDK** | API 36 (Android 16) | Target platform |
| **Min SDK** | API 24 (Android 7.0) | Minimum supported device |
| **SQLite** | 3.x | Local persistent storage via `SQLiteOpenHelper` |
| **jBCrypt** | 0.4 | Secure one-way password hashing |
| **MPAndroidChart** | 3.1.0 | High-performance bar chart visualisations |
| **Material Design 3** | 1.13.0 | Modern UI components (Switches, Dialogs, BottomNav) |
| **AndroidX AppCompat** | 1.7.x | Backwards-compatible Activity support |
| **ViewPager2** | — | Tab-based Daily / Weekly chart navigation in History |
| **Gradle** | — | Build system with version catalogs (.toml) |

---

## 🗄️ Database Design
**Database Name:** `WaterApp.db` | **Current Version:** 14

### Table: `users`
Stores user profiles, credentials, and per-user hydration targets.

| Column | Type | Constraint |
| :--- | :--- | :--- |
| `id` | INTEGER | Primary Key, Auto-increment |
| `name` | TEXT | Not Null |
| `email` | TEXT | UNIQUE, Not Null |
| `phone` | TEXT | — |
| `password` | TEXT | BCrypt Hash, Not Null |
| `target_ml` | INTEGER | Default: 2000 |

### Table: `water_records`
Logs every individual drink entry for historical analysis and chart rendering.

| Column | Type | Constraint |
| :--- | :--- | :--- |
| `id` | INTEGER | Primary Key, Auto-increment |
| `user_id` | INTEGER | Foreign Key → users.id |
| `amount` | INTEGER | Intake in ml |
| `date` | TEXT | yyyy-MM-dd (indexed) |
| `timestamp` | INTEGER | Exact Unix time of entry |
| `target` | INTEGER | Snapshot of user target at time of entry |

### Table: `daily_summary`
Aggregated data per day for fast history rendering and completion tracking.

| Column | Type | Constraint |
| :--- | :--- | :--- |
| `id` | INTEGER | Primary Key, Auto-increment |
| `user_id` | INTEGER | Foreign Key → users.id |
| `date` | TEXT | UNIQUE per user (yyyy-MM-dd) |
| `total` | INTEGER | Total ml logged that day |
| `remaining` | INTEGER | ml remaining to hit target |
| `is_completed` | INTEGER | Boolean 0/1 |

### Table: `reminders`
Stores personalised notification schedules for each user.

| Column | Type | Constraint |
| :--- | :--- | :--- |
| `id` | INTEGER | Primary Key, Auto-increment |
| `user_id` | INTEGER | Foreign Key → users.id |
| `name` | TEXT | Reminder label (e.g. "Before Lunch") |
| `time` | TEXT | HH:mm format |
| `is_enabled` | INTEGER | Boolean 0/1 (Default: 1) |

### Key Database Operations

| Method | Purpose |
| :--- | :--- |
| `insertUser()` | Register a new user |
| `getUserId()` | Authenticate login (email + BCrypt check) |
| `getUserTarget()` / `setUserTarget()` | Read / update the user's daily goal |
| `addWaterRecord()` | Log a drink entry (blocked if day is completed) |
| `getTodayTotalIntake()` | Sum today's logged intake for a user |
| `getTotalForDate()` | Sum logged intake for any given date |
| `getHourlyIntakeForDay()` | Return 24-slot int array for daily bar chart |
| `getDailyIntakeForWeek()` | Return 7-slot int array for weekly bar chart |
| `markDayAsCompleted()` | Archive today's summary and lock further entries |
| `isDayCompleted()` | Check whether today's target has been met |
| `ensureDailySummaryForDate()` | Upsert a summary row for any past date |
| `archiveYesterdayIfMissing()` | Called on app start to backfill yesterday's summary |
| `addReminder()` | Insert a new named reminder for a user |
| `deleteReminder()` | Remove a reminder by ID |
| `updateReminderEnabled()` | Toggle a reminder on or off |
| `getReminders()` | Fetch all reminders for a user ordered by time |
| `getNextReminder()` | Return the next upcoming enabled reminder |

---

## 📁 Project Structure

```text
Daily-Water-Intake-Logger/
├── app/
│   └── src/main/java/com/example/water_logger/
│       ├── database/
│       │   └── DatabaseHelper.java          ← SQLiteOpenHelper, all DAO methods & BCrypt verification
│       ├── models/
│       │   └── Reminder.java                ← Data model (id, name, time, isEnabled)
│       ├── adapters/
│       │   ├── ReminderAdapter.java         ← RecyclerView adapter for Reminders screen
│       │   └── HistoryViewPagerAdapter.java ← ViewPager2 adapter for History charts
│       ├── fragments/
│       │   ├── HistoryDayFragment.java      ← Daily hourly bar chart logic
│       │   └── HistoryWeekFragment.java     ← Weekly daily bar chart logic
│       ├── ui/
│       │   ├── WelcomeActivity.java         ← Splash + session-check redirect
│       │   ├── GetStartedActivity.java      ← Onboarding UI
│       │   ├── LoginActivity.java           ← Auth controller
│       │   ├── RegisterActivity.java        ← User registration
│       │   ├── DashboardActivity.java       ← Main screen, wave view & countdown
│       │   ├── DrinkActivity.java           ← Custom intake logging interface
│       │   ├── ReminderActivity.java        ← Reminder schedule management
│       │   ├── HistoryActivity.java         ← Analytics host activity (ViewPager2)
│       │   └── MeActivity.java             ← Profile & session management
│       ├── views/
│       │   └── WaterLevelView.java          ← Custom Canvas sine-wave drawing
│       └── utils/
│           ├── TargetUpdateBus.java         ← In-app event bus for live target changes
│           └── RoundedBarChartRenderer.java ← MPAndroidChart bar corner customisation
│
└── app/src/main/res/
    ├── layout/                              ← Material 3 XML screen layouts
    ├── drawable/                            ← Glassmorphism backgrounds, gradients & SVG icons
    ├── anim/                                ← Transition & feedback animations
    ├── menu/                                ← Bottom navigation menu definitions
    ├── font/                                ← Typography (Poppins & Inter)
    └── values/                              ← Colors, Strings (localised), Themes, Dimens
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android device or emulator running API 24+

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/RifkyNazar/Daily-Water-Intake-Logger.git
   ```

2. **Open in Android Studio**
   - Select **File → Open**
   - Navigate to the cloned folder and open it

3. **Gradle Sync**
   - Android Studio will auto-sync dependencies (MPAndroidChart, jBCrypt, etc.)
   - If not, click **"Sync Now"** in the notification bar

4. **Run the App**
   - Select a device or emulator (Min SDK 24)
   - Click ▶ **Run** or press **Shift + F10**

5. **First Use**
   - Tap **"Get Started"** then **"Register"** to create an account
   - Log in with your registered email and password
   - Set your daily hydration target from the Dashboard

> **Note:** The database is created automatically on first launch. No manual setup is required.

---

## 👥 Team Members

| Role | Name | Responsibilities |
| :--- | :--- | :--- |
| **Team Lead & Main Developer** | Kalpa Dhananjaya | Architecture, SQLite database, BCrypt logic, Dashboard & session management |
| **Login & Register and Database Creation** | Rifky Nazar] | Page creation, wave animation, validations, database handle |
| **Logic Specialist** | [Name] | Reminder system, chart integration, history fragments, event bus |

---

## 📋 GitHub Contribution Guidelines

Each team member must follow these rules for commits:

### ✅ Good commit messages
```
Security: Implement BCrypt password hashing for registration
DB: Add reminders table with name, time, and is_enabled columns
UI: Build Dashboard wave view with real-time intake progress
Feature: Implement smart next-reminder countdown logic
Fix: Correct daily summary rollover on app resume
Chart: Add hourly bar chart to HistoryDayFragment
UI: Design Reminders screen with Material 3 Switch toggles
Fix: Resolve Reminder constructor mismatch in DatabaseHelper
```

---

## 📄 Module Details
- **Module:** ICT3214 — Mobile Application Development
- **Project:** Daily Water Intake Loggerr (Idea #4)
- **Academic Year:** 2021/2022

---

📄 License
This project is developed for academic purposes as part of the ICT3214 — Mobile Application Development module. 🌊
