# NutriGuide

An Android application that provides personalized nutrition guidance based on a user's health conditions and allergies — going beyond simple calorie tracking.

## Problem Statement
People with health conditions like diabetes, PCOS, hypertension, and obesity struggle to know if a food is safe or suitable for them. Existing nutrition apps only track calories with no personalization. NutriGuide solves this by checking foods against a user's specific health profile.

## Features
- Firebase Authentication (Sign up / Login)
- Health profile setup (age, gender, height, weight, conditions, allergies)
- Food search via nutrition API (Open Food Facts)
- Barcode scanning (Google ML Kit + CameraX)
- Allergy detection engine (flags foods containing user's allergens)
- Rule-based recommendation engine (flags foods unsuitable for diabetes/PCOS/hypertension/obesity)
- BMI & BMR calculation (Mifflin-St Jeor formula)
- Meal logging with daily calorie totals
- Healthy food category suggestions
- Water reminder notifications (WorkManager)
- Nutrition dashboard with live stats

## Tech Stack
- **Language:** Java
- **UI:** Android XML, Material Components
- **Backend:** Firebase Authentication, Firebase Firestore
- **API:** Open Food Facts (nutrition data)
- **Barcode Scanning:** Google ML Kit
- **Background Tasks:** Android WorkManager

## Architecture
1. **UI Layer** – Activities and XML layouts
2. **Logic Layer** – AllergyChecker, RecommendationEngine, SuggestionHelper (pure Java classes)
3. **Data Layer** – Firebase Auth + Firestore, Open Food Facts API

## Setup Instructions
1. Clone the repo
2. Open in Android Studio
3. Add your own `google-services.json` (Firebase config) to the `app/` folder
4. Sync Gradle
5. Run on an emulator or physical device (API 26+)

## Screens
- Login / Sign Up
- Health Profile Setup
- Dashboard (BMI, BMR, suggestions, today's calories)
- Food Search
- Barcode Scanner
- Meal Log

## Author
Momina — B.Tech Information Technology, CMR University