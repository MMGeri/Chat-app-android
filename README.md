# Chat project made in Android studio + Java

Websockets were implemented by firebase api

---

### Notes:

- Repository pattern for the database has not been implemented yet.
- Instead of using FireBase cloud functions for notifying the user, I used a "Job Service" which reminds the app to recreate the observer for any changes in the Firestore database, which lets be honest is `NOT OPTIMAL`. For money reasons.
- The code isn't very "`Clean`", this is my first android app, at the time i was very proud of it. please spare me

you can [download](https://github.com/MMGeri/Chat-app-android/blob/master/Chat/app/release/app-release.apk) the app here

