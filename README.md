# CashCaddy

Local-first personal spend tracker for Android. Native **Kotlin + Jetpack Compose + Material 3**, with a Room database and manual DI (`AppContainer`). Package: `com.cashcaddy.app`.

The UI follows the CashCaddy Material 3 design (dark and light): Home logs, Insights charts, Add keypad, Budgets grid, Settings, and the supporting sheets.

## Screens & flows

1. **Home** — search, category filter, tappable period chip, net spent, comparison vs last period, day-grouped logs. Tap a row to edit.
2. **Insights** — period summary, income/expense cards, spend-over-time bars with average dashed line, category breakdown.
3. **Add** — expense/income segment, large amount, note, date + category, numeric keypad, save. Tab bar stays visible.
4. **Period sheet** — Today, This week, This month, This year, All time (from Home and Insights).
5. **Category picker** — list with checkmark and **Edit**.
6. **Category edit** — Your categories + Suggestions, sticky Expense/Income + **New**.
7. **New category** — type, name, emoji grid, colour dots, add.
8. **Date picker** — Material 3 date picker from the Add date chip.
9. **Budgets** — two-column cards with days left, % spent, amount left, progress + per-day pace marker. Tap to edit; **+** to add.
10. **Settings** — Currency, Appearance, Accent color, Categories.
11. **Appearance sheet** — System / Light / Dark.
12. **Edit transaction** — amount, title, category, delete, save.

Custom 5-item tab bar: Home, Insights, elevated centre **+**, Budgets, Settings.

## First launch

On first launch, starter categories are created so Add works immediately: Food & Drink, Transport, Shopping, Groceries, Bills, Health, plus Travel / Fuel / Rent suggestions, and Salary / Freelance for income. A small set of demo transactions and two sample budgets is also seeded (recent dates, INR amounts) so Home, Insights, and Budgets are not empty. Currency defaults to **INR (₹)**.

## Build

Requires **JDK 17+** and **Android SDK 35**.

```bash
# optional: point at your SDK
echo "sdk.dir=/path/to/Android/sdk" > local.properties

./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`.

Android Studio: **File → Open** this folder, sync Gradle, run the `app` configuration on a device or emulator (API 26+).

### If the SDK is not installed

1. Install [Android Studio](https://developer.android.com/studio) or command-line tools.
2. Install platform `android-35` and Build-Tools 35.x via SDK Manager.
3. Set `ANDROID_HOME` (or `sdk.dir` in `local.properties`) and run the Gradle command above.

This cloud workspace may not include the Android SDK; sources are complete and the project is a standard Gradle app module.

## Architecture

- **Single `:app` module**
- **Room** — `Category`, `Transaction` (amount in minor units), `Budget` (category-linked monthly limit)
- **DataStore** — currency, appearance, accent
- **AppContainer** — database, repositories, seeder; constructed in `CashCaddyApplication`
- **MainViewModel** — observes Room flows and performs writes

## Theme

Appearance follows **System / Light / Dark**. Accent defaults to blue (Material 3) and can be switched in Settings. Surfaces, tab bar, and cards are tuned to the design PDF.