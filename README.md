# Accent Recognition Android App

This Android app is designed to recognize three different accents American, British and Indian. It includes features such as recording, accessing a history of recordings, and listening to accent examples.
## Authors
Leah Hold leahho@edu.hac.ac.il 

Abigail Blackman avigailbl@edu.hac.ac.il
## Table of Contents
- [Prerequisites](#prerequisites)
- [App Structure](#app-structure)
- [Usage Instructions](#usage-instructions)
- [Main Classes](#main-classes)

---


---

### Prerequisites
- **Android Studio**: To open, edit, and run the project.
- **Java**: The project is written in Java, so ensure Java SDK is set up in Android Studio.

---

### App Structure

1. **MainActivity.java**:
    - Serves as the app's main screen with buttons to navigate to different functionalities:
        - **Start Activity** for recording.
        - **History Activity** for viewing and managing past recordings.
        - **Accents Examples** for listening to accent samples.

2. **StartActivity.java**:
    - Allows the user to record audio, upload it to a server, and display the detected country based on the accent.

3. **HistoryActivity.java**:
    - Displays saved recordings.
    - Provides playback and deletion options for each recording.
    - Includes a "Stop" button to stop audio playback.

4. **AccentsExamples.java**:
    - Enables the user to listen to predefined accent examples (e.g., India, US, England).
    - A "Stop" button stops any currently playing audio.

5. **BaseActivity.java**:
    - Acts as a base activity class for setting up a common toolbar across all screens.

6. **FirstFragment.java** and **SecondFragment.java**:
    - Used to navigate between fragments in the app.

---

### Usage Instructions

1. **Setup**:
    - Clone or download the project and open it in Android Studio.
    - Build the project to download all required dependencies.

2. **Running the App**:
    - Select an emulator or physical device.
    - Run the app to open the MainActivity screen.

3. **App Features**:
    - **Recording and Uploading**:
        - Navigate to **Start Activity** to record audio.
    - **History**:
        - Access **History Activity** to manage recordings, play or delete them.
    - **Accent Examples**:
        - Use the **Accents Examples** to listen to predefined accents.

---

### Main Classes

- **MainActivity**: Main menu for navigation.
- **StartActivity**: Records and uploads audio.
- **HistoryActivity**: Manages recording history with playback and deletion.
- **AccentsExamples**: Plays sample accent files.
- **BaseActivity**: Sets a common toolbar across screens.
- **Fragments (FirstFragment, SecondFragment)**: For app navigation.

---

### License
This project is licensed under the MIT License.
