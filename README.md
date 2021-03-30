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
- Perform CPU load by doing calculations operations with given amount of handlers (threads)
- CPU load by doing multiplications operations with given amount of handlers (threads)
- Bluetooth module load by performing continuous scanning in search of other bluetooth devices
- GPS module load by requesting continuous location updates 
- GPS module load by requesting location updates
- Internet module/CPU load by performing continuous (another one after received result of previous one) ping calls to given server
- Internet module/CPU load by performing ping calls with given interval to given server
- Internet module/CPU load by performing continuous data download (restart download if previous one finished) from given server
