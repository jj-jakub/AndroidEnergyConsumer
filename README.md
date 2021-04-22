# AndroidEnergyConsumer

## General description
Application for making load on chosen device modules, its purpose is to simulate real-life usage and after performing experiment (or even while it is running), user can read energy with external tools (such as adb -> batterystats) and get knowledge how given algorithm affects devices battery.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jj-jakub_AndroidEnergyConsumer&metric=alert_status)](https://sonarcloud.io/dashboard?id=jj-jakub_AndroidEnergyConsumer)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=jj-jakub_AndroidEnergyConsumer&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=jj-jakub_AndroidEnergyConsumer)

## Technologies
Dependencies and technologies used in project:
- JUnit5 v5.7.1
- Mockito v3.8.0
- MockitoKotlin2 v2.2.0
- Koin v2.0.1
- Flipper v0.76.0
- Retrofit2 v2.9.0
- Android Navigation v2.3.4
- Kotlin Coroutines v1.4.3
- Kotlin Flow (Coroutines) v1.4.3

## [Latest builds](https://github.com/jj-jakub/AndroidEnergyConsumer/releases)


## Available features:
- Perform CPU load by doing calculations operations with given amount of threads
- CPU load by doing multiplications operations with given amount of threads
- Bluetooth module load by performing continuous scanning in search of other bluetooth devices
- GPS module load by requesting continuous location updates 
- GPS module load by requesting location updates
- Internet module/CPU load by performing continuous (another one after received result of previous one) ping calls to given server
- Internet module/CPU load by performing ping calls with given interval to given server
- Internet module/CPU load by performing continuous data download (restart download if previous one finished) from given server

### Example of use
#### Application launch

On the first launch of application on Android 6 or above you will be provided the following screen. 
Click "Allow" to let application run in background continuously to prevent operating system to terminate application while it is in background as this may strongly affect the measurements.

|Work in background|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/WorkInBackground.webp" width="250"/></p>|

This screenshot shows how application requests permission for accessing storage of device.
Click "Allow" to grant access to device storage, which is needed to save logs from execution to the file. This is useful for debugging purposes but is not necessary for application to work properly.

|Storage permission request|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/StoragePermission.webp" width="250"/></p>|

#### Making CPU load by performing multithreaded calculations

On the main screen of application click "Calculations launcher" button to proceed to screen from where you will be able to launch calculations algorithms.

|Main screen|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/MainScreen.webp" width="250"/></p>|

This is Calculations fragment. You can control the parameters that running algorithm will use, i.e. 
- "Number of threads" - number of threads that will be launched for calculations
- "Factor" - number that will be used for additions/multiplications of variables

You can run addition algorithm or multiplication algorithm by clicking "Perform addition calculations" or "Perform multiplication calculations" respectively.

Calculations status label indicates if any algorithm is currently running or not.
Calculations result label shows the latest information about calculations. It is mainly log from the thread which about finishing some part of operations.

If you want to stop running calculations click "Abort calculations" button.

|Calculations fragment|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/CalculationsFragment.webp" width="250"/></p>|

In this picture you can see parameters fields filled. Now calculations are ready to run. If you will not specify any values in the fields or values will be invalid, then default ones will be used (Number of threads = 4, Factor = 2).

|Calculations fragment with set up parameters|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/CalculationsFragmentBeforeLaunch.webp" width="250"/></p>|

After clicking "Perform addition calculations" or "Perform multiplication calculations" button algorithm will be launched. Calculations status value label will change its color to red and text to "Running" and information about calculations will be printed in Calculations result value label.

|Calculations fragment when calculations are running|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/CalculationsFragmentRunning.webp" width="250"/></p>|

After launching calculations you can observe its status not only from Calculations fragment, but also from notification bar, where CalculationsService notification will be always present when any algorithm will be running. 

|Notification about calculations|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/CalculationsFragmentNotification.webp" width="250"/></p>|

After launching calculations on 32 threads and factor equal to 2 (as in the pictures above) you can read from screenshot below, that these operations are running and have high impact on CPU usage and energy consumption. This picture comes from Android Studio Profiler, which is an easy tool for monitoring resources used by application in real-time.

|Android Studio Profiler readings|
|-----|
|<p align="center"><img src="https://github.com/jj-jakub/AndroidEnergyConsumer/blob/develop/readmeImages/Profiler.webp"/></p>|