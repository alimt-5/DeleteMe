# DeleteMe

> **Real-time person removal from a camera feed using hand gestures, on-device computer vision, and a captured background.**

**DeleteMe** is an Android computer-vision application that creates a real-time "disappearing person" effect.

The application captures the current environment as a background, detects a closed fist using **MediaPipe Hand Landmarker**, detects the person using **MediaPipe Object Detector + EfficientDet Lite0**, and replaces the detected person region with the previously captured background.

All vision processing is performed locally on the Android device.

---

# ✨ Features

- 📷 Android camera integration
- 🔄 Front/back camera switching
- 🖼️ Background capture
- ⏱️ Five-second background countdown
- ✊ Closed-fist gesture detection
- 🧠 MediaPipe Hand Landmarker
- 🧍 Person detection
- 🎯 Largest-person selection
- ⚡ Frame-rate limiting
- 🧠 Reduced ML inference resolution
- 🎥 Real-time processing
- 📱 On-device computer vision
- 🧹 Bitmap and ML resource management
- 🛡️ Runtime camera permission handling
- 🧩 Separated Camera / Vision / UI architecture

---

# 🧠 How It Works

The application follows this pipeline:

```text
Camera
  ↓
CameraX ImageAnalysis
  ↓
ImageProxy
  ↓
Bitmap
  ↓
Frame Rate Limiter
  ↓
DeleteMeProcessor
  ↓
Hand Detection
  ↓
Fist Classification
  ↓
Person Detection
  ↓
Bounding Box Mapping
  ↓
Background Replacement
  ↓
Output Bitmap
  ↓
Jetpack Compose
```

---

# ✊ Gesture Detection

The application uses **MediaPipe Hand Landmarker** to obtain hand landmarks.

The landmarks are passed to:

```text
FistClassifier
```

The classifier produces:

```text
OPEN
CLOSED
NOT_DETECTED
```

The deletion state is activated when the hand is classified as:

```text
CLOSED
```

`KeepState` is used to stabilize the gesture state and reduce accidental state changes caused by individual detection errors.

---

# 🧍 Person Detection

Person detection is performed using:

```text
efficientdet_lite0.tflite
```

through MediaPipe Object Detector.

The detector is restricted to:

```text
person
```

detections.

If multiple people are detected, the current implementation selects the largest detected person bounding box.

---

# 🖼️ Background Capture

The application captures a clean background before starting the deletion effect.

Flow:

```text
Live Camera
     ↓
Capture Background
     ↓
5-second Countdown
     ↓
Capture Current Frame
     ↓
Store Background Bitmap
     ↓
Start DeleteMe
```

The captured bitmap becomes the reference image used during person removal.

---

# 🎯 Person Removal

The camera frame remains at its original output resolution.

For ML inference, a smaller copy is generated:

```text
640 × 360
```

The person bounding box is detected on this smaller frame.

The bounding box is then mapped back to the original camera resolution.

Finally:

```text
Background Region
        ↓
Detected Person Region
```

is drawn onto a copy of the current frame.

This produces the disappearing-person effect.

---

# ⚡ Performance

Real-time computer vision can be computationally expensive.

DeleteMe therefore uses several optimizations.

## Frame Rate Limiting

The current analyzer processes approximately:

```text
10 FPS
```

instead of processing every incoming camera frame.

---

## Single-Frame Processing

An `AtomicBoolean` prevents multiple frames from entering the expensive ML pipeline simultaneously.

Conceptually:

```text
Incoming Frames
      │
      ▼
┌───────────────┐
│ Frame Limiter │
└───────┬───────┘
        │
        ▼
┌───────────────────┐
│ One Frame at Time │
└─────────┬─────────┘
          │
          ▼
       ML Pipeline
```

---

## ML Resolution Scaling

The original camera frame is preserved for display.

Only the ML input is resized to:

```text
640 × 360
```

This reduces the amount of image data processed by the vision models.

---

# 📱 Camera Architecture

CameraX is responsible for the camera pipeline.

Current camera package:

```text
camera/
├── BitmapUtils.kt
├── CameraFrameAnalyzer.kt
└── FrameRateLimiter.kt
```

The pipeline is:

```text
CameraX
  ↓
ImageAnalysis
  ↓
ImageProxy
  ↓
Bitmap conversion
  ↓
Rotation
  ↓
DeleteMeProcessor
```

---

# 🔄 Camera Switching

The application supports switching between:

```text
Front Camera
```

and:

```text
Back Camera
```

using CameraX `CameraSelector`.

When the selected lens changes, the current camera binding is recreated using the new lens-facing configuration.

---

# 🧩 Project Structure

```text
DeleteMe/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       │   ├── efficientdet_lite0.tflite
│   │       │   └── hand_landmarker.task
│   │       │
│   │       ├── java/
│   │       │   └── com/example/deleteme/
│   │       │       ├── camera/
│   │       │       ├── ui/
│   │       │       ├── vision/
│   │       │       └── MainActivity.kt
│   │       │
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

---

# 🧠 Vision Components

### `DeleteMeProcessor.kt`

Main computer-vision pipeline.

Responsibilities:

```text
Frame
→ ML resize
→ Hand detection
→ Gesture classification
→ State stabilization
→ Person detection
→ Bounding-box mapping
→ Background replacement
```

---

### `HandLandmarkerHelper.kt`

Loads and executes:

```text
hand_landmarker.task
```

and converts MediaPipe results into the project's internal hand-detection structures.

---

### `FistClassifier.kt`

Classifies hand landmarks into open/closed hand states.

---

### `KeepState.kt`

Stabilizes the detected gesture state.

---

### `PersonDetector.kt`

Loads:

```text
efficientdet_lite0.tflite
```

and detects people using MediaPipe Object Detector.

---

### `PersonBoundingBox.kt`

Represents the detected person's bounding rectangle.

---

### `MlFrameResizer.kt`

Resizes camera frames before ML inference.

Current ML resolution:

```text
640 × 360
```

---

# 📦 Model Assets

The project currently includes two model files:

```text
app/src/main/assets/efficientdet_lite0.tflite
app/src/main/assets/hand_landmarker.task
```

Both are included in the repository.

---

# 🛠️ Technology Stack

| Technology | Role |
|---|---|
| Kotlin | Application language |
| Jetpack Compose | UI framework |
| Material 3 | UI components |
| CameraX | Camera and image analysis |
| MediaPipe Tasks Vision | Computer vision APIs |
| Hand Landmarker | Hand landmark detection |
| Object Detector | Person detection |
| EfficientDet Lite0 | Object detection model |
| Android Bitmap | Image processing |
| Gradle Kotlin DSL | Build configuration |

---

# ⚙️ Requirements

Recommended development environment:

- Android Studio
- Android SDK
- Gradle Wrapper
- Android device with a camera
- Camera permission
- A device capable of running the required ML inference

For realistic real-time performance testing, a physical Android device is recommended.

---

# 🚀 Installation

Clone the repository:

```bash
git clone https://github.com/alimt-5/DeleteMe.git
```

Enter the project:

```bash
cd DeleteMe
```

Open the project in Android Studio and allow Gradle synchronization to complete.

Build:

```bash
./gradlew assembleDebug
```

On Windows:

```bash
gradlew.bat assembleDebug
```

Then run the application on an Android device.

---

# 🔐 Camera Permission

DeleteMe requires:

```text
android.permission.CAMERA
```

The application requests camera permission at runtime using Android's Activity Result API.

---

# ▶️ Usage

### 1. Grant Camera Permission

Allow camera access when requested.

### 2. Select Camera

Choose either the front or rear camera.

### 3. Capture Background

Point the camera at the environment and capture a clean background.

### 4. Wait for Countdown

The application waits five seconds before capturing the background frame.

### 5. Start DeleteMe

Press:

```text
Start DeleteMe
```

### 6. Make a Fist

Show your hand and close your fist.

### 7. Move

Move in front of the camera.

The detected person region will be replaced with the captured background.

---

# 🔬 Full Processing Pipeline

```text
CameraX ImageAnalysis
        │
        ▼
     ImageProxy
        │
        ▼
     Bitmap
        │
        ▼
     Rotation
        │
        ▼
CameraFrameAnalyzer
        │
        ├── FPS Limiter
        │
        └── Single Processing Lock
                    │
                    ▼
             DeleteMeProcessor
                    │
                    ▼
             MlFrameResizer
                    │
                    ▼
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
    Hand Landmarker     Person Detector
          │                   │
          ▼                   │
    FistClassifier            │
          │                   │
          ▼                   │
      KeepState               │
          │                   │
          └─────────┬─────────┘
                    │
                    ▼
          Person Bounding Box
                    │
                    ▼
          Coordinate Mapping
                    │
                    ▼
          Background Replacement
                    │
                    ▼
              Output Bitmap
                    │
                    ▼
              Compose UI
```

---

# 🏗️ Architecture

The application is organized into three main layers:

```text
Camera
   ↓
Vision
   ↓
UI
```

### Camera

Handles:

- CameraX
- ImageAnalysis
- ImageProxy
- Bitmap conversion
- Rotation
- Frame-rate limiting

### Vision

Handles:

- Hand detection
- Fist classification
- State stabilization
- Person detection
- Bounding boxes
- ML resizing
- Background replacement

### UI

Handles:

- Camera display
- Permission UI
- Background capture
- Countdown
- Background preview
- Start button
- Camera switching
- Result display

---

# 📊 Current Configuration

```text
compileSdk = 35
minSdk     = 28
targetSdk  = 34
```

ML resolution:

```text
640 × 360
```

Processing target:

```text
10 FPS
```

Gesture stabilization:

```text
0.2 seconds
```

---

# ⚠️ Limitations

### Person Detection

The quality of the removal effect depends on the Object Detector's ability to correctly identify the person.

### Fast Movement

Very fast movement can cause the detected bounding box to change significantly between processed frames.

### Lighting

Poor or extreme lighting can reduce hand and person detection accuracy.

### Background Consistency

The captured background should remain as close as possible to the environment during the deletion effect.

### Occlusion

Objects covering the person can reduce detection quality.

### Device Performance

Real-time ML inference performance depends on the Android device's CPU/GPU capabilities.

---

# 🔒 Privacy

The primary image-processing pipeline runs locally on the Android device.

The camera frames used for the computer-vision pipeline are not uploaded to an external server for inference.

MediaPipe Tasks supports on-device processing for its task APIs.

Applications using MediaPipe Tasks should nevertheless review applicable privacy, consent, and telemetry requirements.

---

# 📁 Main Components

| File | Responsibility |
|---|---|
| `MainActivity.kt` | Application entry point |
| `DeleteMeScreen.kt` | Main application UI |
| `BackgroundCaptureScreen.kt` | Background capture flow |
| `CameraFrameAnalyzer.kt` | Camera frame processing |
| `FrameRateLimiter.kt` | Frame-rate control |
| `BitmapUtils.kt` | Bitmap utilities |
| `DeleteMeProcessor.kt` | Main vision pipeline |
| `HandLandmarkerHelper.kt` | Hand detection |
| `FistClassifier.kt` | Gesture classification |
| `HandDetectionResult.kt` | Detection result |
| `HandLandmarkPoint.kt` | Landmark representation |
| `HandState.kt` | Hand state |
| `KeepState.kt` | Gesture stabilization |
| `MlFrameResizer.kt` | ML input resizing |
| `PersonDetector.kt` | Person detection |
| `PersonBoundingBox.kt` | Person bounding box |

---

# 🧪 Development

The recommended development order is:

```text
Camera
  ↓
Bitmap Pipeline
  ↓
Hand Detection
  ↓
Gesture Classification
  ↓
Person Detection
  ↓
Background Replacement
  ↓
Performance Optimization
  ↓
UI Improvements
```

Keeping the vision pipeline modular makes debugging and future improvements easier.

---

# 📌 Project Status

Current implementation:

```text
Camera                  ✅
Camera Permission       ✅
Front Camera            ✅
Back Camera             ✅
Camera Switching        ✅
Background Capture      ✅
Countdown               ✅
Background Preview      ✅
Hand Detection          ✅
Fist Detection          ✅
Person Detection        ✅
Background Replacement  ✅
ML Resolution Scaling   ✅
FPS Limiting            ✅
Resource Cleanup        ✅
Jetpack Compose UI      ✅
```


# 🙏 Acknowledgements

DeleteMe uses technologies from:

- Android / Jetpack
- CameraX
- MediaPipe Tasks
- EfficientDet
- TensorFlow Lite ecosystem

MediaPipe is an open-source project maintained by Google and the open-source community.

---

# 🔗 Repository

**GitHub:**

https://github.com/alimt-5/DeleteMe
