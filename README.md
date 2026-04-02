# 🚀 Gravity Flip App – DevOps CI/CD Project

## 📌 Project Overview

This project demonstrates a **real-world DevOps pipeline** for an Android application.

The goal is to:

* Build an Android app
* Automate APK generation
* Deploy using CI/CD
* Distribute builds to testers

---

## 🧠 Architecture

```
Developer → GitHub → GitHub Actions → Build APK → Firebase → Tester Device
```

---

## 🛠️ Tech Stack

* Android (Kotlin)
* Gradle
* Git & GitHub
* GitHub Actions (CI/CD)
* Firebase App Distribution

---

## 📱 Application

**App Name:** Gravity Flip
**Package:** com.nazeef.gravityflip

Features:

* Gravity-based UI interaction
* Tilt-based movement
* Simple Android activity

---

## ⚙️ CI/CD Pipeline

### 🔄 Workflow

1. Developer pushes code to GitHub
2. GitHub Actions triggers automatically
3. APK is built using Gradle
4. APK is uploaded to Firebase
5. Testers receive install link via email

---

## 📂 Project Structure

```
.
├── app/
├── gradle/
├── .github/workflows/android.yml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔐 GitHub Secrets

The following secrets are required:

### 1. FIREBASE_SERVICE_ACCOUNT

* Firebase service account JSON content

### 2. FIREBASE_APP_ID

* Firebase App ID

---

## ⚡ GitHub Actions Workflow

Location:

```
.github/workflows/android.yml
```

Pipeline includes:

* Checkout code
* Setup Java (JDK 17)
* Setup Android SDK
* Build APK
* Upload to Firebase

---

## ▶️ How to Trigger Pipeline

```bash
git commit --allow-empty -m "Trigger pipeline"
git push
```

---

## 📦 APK Build Location

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## ☁️ Firebase Setup

Steps:

1. Create Firebase project
2. Add Android app
3. Enable App Distribution
4. Add testers
5. Download service account JSON

---

## 📲 Deployment

* APK is automatically uploaded to Firebase
* Testers receive email
* App can be installed on real devices

---

## 🐞 Issues Faced & Fixes

### ❌ APK missing icon

✔ Fixed by adding `ic_launcher` in mipmap folders

### ❌ Gradle version mismatch

✔ Updated Gradle wrapper

### ❌ Kotlin compatibility error

✔ Matched Kotlin version with Compose

### ❌ GitHub Actions failure

✔ Fixed workflow configuration

---

## 🎯 Learning Outcomes

* Built Android CI/CD pipeline
* Integrated GitHub Actions with Firebase
* Debugged real build errors
* Understood DevOps workflow in mobile apps

---

## 💼 Interview Summary

> Built a mobile CI/CD pipeline using GitHub Actions integrated with Firebase App Distribution for automated APK build and deployment.

---

## 🚀 Future Improvements

* Build AAB (Play Store)
* Auto versioning
* Multi-environment setup
* Deploy to Play Store
* Add automated testing

---

## 👨‍💻 Author

Muhammed Nazeef

---

