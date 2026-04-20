# Student Housing Android (Kotlin)

Native Android app for the Theme 3 capstone, using:
- Kotlin + XML Views
- MVVM + Repository
- Retrofit + OkHttp
- Room (offline cache)
- GPS (distance-to-campus sorting)

## Open in Android Studio
1. Open Android Studio.
2. Choose **Open** and select this `android` folder.
3. Let Gradle sync.
4. Replace `API_BASE_URL` in `app/build.gradle.kts` with your Render URL:
   - `https://<your-render-service>.onrender.com/api/`
5. Run on emulator/device.

## Feature mapping
- Login (`/auth/login`)
- Properties list (`/properties`) with RecyclerView
- Property detail and booking creation (`/bookings`)
- My bookings list (`/bookings/my-bookings`)
- Offline cache for properties + bookings using Room
- Near campus toggle using GPS and configured campus coordinates

## Files to update before demo
- `app/build.gradle.kts`
  - `API_BASE_URL`
  - `CAMPUS_LAT`, `CAMPUS_LNG`

## Quick demo flow
1. Login with a student account.
2. Open properties list (online).
3. Turn on airplane mode and reopen list (loads cache from Room).
4. Toggle **Near campus** to show location-based sorting.
5. Open a property and tap **Book now**.
6. Open **Bookings** to show booking data.
