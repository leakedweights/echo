# Echo: AI Audio Transcriptions and Queries

## Introduction

Echo is an Android application for information capturing and retrieval. Users can record voice memos which are then stored in textual format and vectorized for storage in a vector database.

![Echo Application Cover](assets/Cover.png)

## Key Features

- **User Authentication** - Google OAuth

- **Voice Recording**

- **Audio Transcription**
- 
- **Knowledge Base**: Transcribed texts are stored and indexed in a vector database.

- **Query Interface**: Users can interact with the knowledge base through text queries.

# Application Documentation

## Overview

The Echo application allows users to naturally record voice notes via mobile devices, subsequently engaging in dialogues about the content of these notes through a sophisticated language model (GPT-3.5-turbo).

## Execution Environment and Permissions

Echo is compatible with Android-based operating systems on mobile devices, requiring a minimum SDK version of 26. Additionally, devices must be equipped with a microphone for audio recording.

Users must possess a Google account for identification purposes and to associate recorded notes.

The application necessitates permission for internet access (`android.permission.INTERNET`) and audio recording (`android.permission.RECORD_AUDIO`).

## Limitations

Running the application on an emulator may impede audio recording capabilities due to limitations of `android.speech` and emulator microphone configurations. Although the Speech to Text window is accessible, the absence of audio input prevents speech-to-text conversion.

To test functionality on an emulator, substitute the `mockText` variable in `HomeActivity.kt` with the desired text content.

```kotlin
// HomeActivity.kt

private var speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val recognizedText = results?.get(0) ?: ""
        saveTranscription(recognizedText)
        Log.d("MainActivity", recognizedText)
    } else {
        Log.d("MainActivity", "Google Speech recognizer could not pick up speech, inserting mock context.")
        val mockText = "Sample text to save"

        saveTranscription(mockText)
    }
}
```

## User Guide

### Login

Upon launching Echo, users are prompted to log in using their Google account credentials. Clicking the `login with google` button directs users to the authentication page.

![Login View](assets/1-login.png)

### Overview Screen

Following successful login, users are presented with an overview screen displaying the transcription of the most recent voice memo and the total count of recorded words.

![Overview Screen](assets/2-overview.png)

The `Ask questions` button navigates users to the query interface.

### Recording Notes

Users can start a new recording by clicking the `Start Recording` button, activating Google's Speech to Text module for English voice note recording. Echo then automatically converts the audio to text.

![Recording Interface](assets/3-recording.png)

### Query Interface

Clicking `Ask questions` opens the chat interface, where users can type messages and receive responses based on the stored voice note content.

![Query Interface](assets/4-chat.png)

### Logout

Users can log out by clicking the exit icon, returning to the initial login screen for re-authentication.

