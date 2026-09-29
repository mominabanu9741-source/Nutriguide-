# Testing Summary — NutriGuide

| Test Case | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| User Signup/Login | Enter email/password, tap Login | Navigates to Dashboard or Profile Setup | Worked as expected | Pass |
| Profile Save | Fill age, height, weight, gender, conditions, allergies | Data saved to Firestore, dashboard updates | Confirmed via Firestore console | Pass |
| BMI Calculation | Save profile with height/weight | BMI displayed correctly on dashboard | BMI: 25.8 shown correctly | Pass |
| BMR Calculation | Save profile with age/gender/height/weight | BMR displayed correctly on dashboard | BMR: 1292 kcal/day shown correctly | Pass |
| Food Search | Search "banana" | Returns nutrient data from API | Correct nutrient data displayed | Pass |
| Allergy Detection | Search food containing user's allergen | Warning shown | "No matched allergens" shown correctly for safe items | Pass |
| Recommendation Engine | Search high-sugar/high-carb food with PCOS profile | Flags food as not recommended with reason | Suitable/not suitable verdict shown correctly | Pass |
| Barcode Scan | Scan a real barcode (tested via manual barcode input) | Returns product info + allergy/recommendation check | Coca-Cola data + checks displayed correctly | Pass |
| Meal Logging | Log a meal (e.g. "Apple", 50 kcal) | Saved to Firestore, shown in list, total updates | "Today's intake: 50 kcal" confirmed | Pass |
| Water Reminder | Wait for scheduled interval | Notification appears | Notification delivered successfully | Pass |
| Healthy Suggestions | View dashboard with PCOS condition | Shows fiber/protein suggestions | Correct suggestions displayed | Pass |

## Known Issues / Notes
- Firestore composite indexes were required for meal log queries (userId + timestamp) — created manually via Firebase Console.
- Emulator occasionally required cold boot due to system-level crashes unrelated to app code.
- Barcode scanning tested via direct barcode lookup call (emulator camera limitation) rather than live camera scan.