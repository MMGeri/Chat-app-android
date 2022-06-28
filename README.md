# Chat project made in Android studio + Java

Websockets were implemented by firebase api

---

### Notes:

- Repository pattern for the database has not been implemented yet.
- Instead of using FireBase cloud functions for notifying the user, I used a "Job Service" which reminds the app to recreate the observer for any changes in the Firestore database, which lets be honest is `NOT OPTIMAL`. For money reasons.
- The code isn't very "`Clean`", this is my first android app, at the time i was very proud of it. please spare me

- The app requires a min android sdk of `26`

You can the app here [download](https://github.com/MMGeri/Chat-app-android/blob/master/Chat/app/release/app-release.apk) 

If that doesnt work then here is an older version which uses Foreground service instead [download older](https://github.com/MMGeri/Chat-app-android/blob/master/Chat/app/release-old/app-release.apk)

## Some pics
<p float="left">
  <img src="https://github.com/MMGeri/Chat-app-android/blob/master/pics/main-screen.jpg" width="300">
  <img src="https://github.com/MMGeri/Chat-app-android/blob/master/pics/friends-list.jpg" width="300">
  <img src="https://github.com/MMGeri/Chat-app-android/blob/master/pics/add-friends.jpg" width="300">
  <img src="https://github.com/MMGeri/Chat-app-android/blob/master/pics/delete-friends.jpg" width="300">
  <img src="https://github.com/MMGeri/Chat-app-android/blob/master/pics/drawer-menu.jpg" width="300">
  <img src="https://github.com/MMGeri/Chat-app-android/blob/master/pics/profile.jpg" width="300">
</p>
