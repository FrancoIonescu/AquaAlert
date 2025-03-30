# AquaAlert

## Objective

AquaAlert is an Android mobile application developed in Android Studio using Java. Its primary function is to send scheduled notifications to users, reminding them to drink water and thereby encouraging healthier hydration habits.

---

## Functionalities

### Hydration Reminders

- Users can set a **custom interval** for notifications.
- Notifications remind users to **drink water** at the specified times.

## Technologies

- **Development Environment**:
  - **Android Studio** for building and testing the app.
  - **Java** as the primary programming language.
- **Android Features**:
  - **Foreground and background notifications** using AlarmManager.
  - **SharedPreferences** for storing user preferences.
  - **Material UI components** for an intuitive user experience.

---

## Architecture

### Data Flow

1. **User Input**:

   - Users configure their hydration schedule.

2. **Background Process**:

   - The app schedules periodic notifications using **AlarmManager**.

3. **Notification System**:

   - Notifications are triggered at specified intervals.

---

## Running Instructions

1. Clone the repository containing the project.
2. Open the project in **Android Studio**.
3. Connect a physical **Android device** or use an **emulator**.
4. Click **Run** to build and deploy the application.
5. Grant **notification permissions** when prompted.
6. Set your desired **reminder interval** and start tracking your hydration!
