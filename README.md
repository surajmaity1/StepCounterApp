# StepCounterApp
Step Counter Android App that helps you to count each step and measure the distace you have covered.  This app is developed using Google Fit API.

## CHECK THIS APP - 
[Step Counter App](https://drive.google.com/file/d/1oSbWHO1nxBBiixhqtSGO_1A9o9VmFWby/view?usp=sharing)

## PREREQUISITE STEPS -
**1. Google Account Needed**:
- To use the Google Fit APIs, you need a Google Account. 

**2. Enable Google Play services:**

- Get the latest client library for Google Play services on your development host:

- Open the Android SDK Manager in Android Studio.
- Under SDK Tools, find Google Play services.
- If the status for these packages isn't Installed, select them both and click Install Packages.


**3. Request an OAuth 2.0 client ID in the Google API Console**

Follow these steps to create or modify a project for your app in the Google API Console, enable the Fitness API, and request an OAuth 2.0 client ID.

these steps to enable the Fitness API in the Google API Console and get an OAuth 2.0 client ID.

- Go to the [Google API Console](https://console.cloud.google.com/).
- Select a project, or create a new one. Use the same project for the Android and REST versions of your app.
- Click Continue to enable the Fitness API.
- Click Go to credentials.
- Click New credentials, then select OAuth Client ID.
- Under Application type select Android.
- In the resulting dialog, enter your app's SHA-1 fingerprint and package name. For example:

BB:0D:AC:74:D3:21:E1:43:67:71:9B:62:91:AF:A1:66:6E:44:5D:75

com.example.android.fit-example


- Click Create. Your new Android OAuth 2.0 Client ID and secret appear in the list of IDs for your project. An OAuth 2.0 Client ID is a string of characters, something like this:

780816631155-gbvyo1o7r2pn95qc4ei9d61io4uh48hl.apps.googleusercontent.com


**4. Add test users in Google OAuth:**
- Go to APIs and services in Google API console
- Create API key
- Under OAuth consent screen:
- Enable 'User type' to External
- Add test users


**5. Get google-services.json from Firebase**
- [Add Firebase to your Android project](https://firebase.google.com/docs/android/setup)



## OFFICIAL DOCUMENTATION -
[FIT API for Android](https://developers.google.com/fit/android/get-started)
