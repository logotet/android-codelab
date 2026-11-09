# android-codelab
Android project that serves as a base for code challenges implemented by applicants.
The base is written in kotlin. 

(!)If you have the NDK plugin installed, please disable it for the project, as errors may occur.

# Android Coding Challenges
Coding challenges are useful when the applicant does not provide a github repository or any work samples. Even if a github repository has been provided it is generally a good idea to give the applicant a task to solve and have him present his solution in a separate session. 

## General Instructions
The following instructions/conditions are valid independently of the actual coding challenge

- The code base has been tested with Android Studio Narwhal Feature Drop which is the recommended version, however feel free to try a higher version and adjust the configuration as needed
- The task should be implemented in kotlin
- Approach this task as if it was a real-world implementation - i.e. exactly how you would approach the task if you were working for a company
- 3rd party libraries may be used
- 3rd party libraries must be wrapped: They should be abstracted out, so any other library could be plugged into the solution
- The base project for this task will be provided by us
- Once completed, please send us your solution and presents it to us, followed by a discussion about the implementation and design decisions made
- The solution can be sent as a zip file or as a publicly accessible github/gitlub etc project link
- The solution sent to us must be complete, i.e. can be opened directly via Android Studio without additional configuration

## Location Based Notifications
In this challenge the applicant has to implement location-based notifications/reminders, the following conditions are given:

- When creating a new memo, the user provides a location by selecting a point on a map (for instance: google maps or open street maps)
- The memo is then saved
- Once the user physically reaches that location, a notification should be displayed in the phone's status bar, that contains the title and the first 140 characters of the note text
- "Reaching the location" is defined as follows: The user is within 200 meters of the location he initially selected during the memo creation
- The notification should also contain an icon (the icon choice is up to you)
- Some form of location tracking will be required to achieve the desired result, i.e. to know when a user is close to the given location of a memo
- The feature must also work, when the app is running in the background (or possibly not running at all)

# Solution

## What it does
- **Create memo**: next to title and text, the user picks a location ("Select on map"). The location is required.
  The map is OpenStreetMap through osmdroid, so no API key is needed and the project opens and runs without extra setup.
- **Reminder**: once the user is within 200 m of the location, a notification shows the memo title and the first
  140 characters of its text, with a location pin icon. Tapping it opens the memo.
- **Background / app not running**: the Google Play services Geofencing API tracks the position. The system starts the
  app for the geofence broadcast, so reminders also arrive when the app is closed. Monitored locations are restored
  after a reboot, an app update and on every app start.
- **Done memos** no longer remind the user.

## How it is built
The existing structure is unchanged. New code:

| Package | Contents |
|---|---|
| `model` | `GeoLocation`: the app's own location type, so no library type leaks out |
| `location` | `GeofenceMonitor` interface, the 200 m radius, `BootReceiver` |
| `location.playservices` | `PlayServicesGeofenceMonitor`, `GeofenceBroadcastReceiver` (the only Play services code) |
| `map` | `MapFactory` / `LocationPickerMap` interfaces |
| `map.osmdroid` | osmdroid implementation (the only osmdroid code) |
| `notification` | `ReminderNotifier` interface, `AndroidReminderNotifier`, `ReminderText` (140 character rule) |
| `domain` | Use cases: save, complete, restore |
| `permission` | Permission checks and the step-by-step permission requests |
| `view.pick` | Full-screen map to pick a location |

**Swapping a library**: `Providers` is the only place that chooses an implementation. Using e.g. Google Maps
means writing a `GoogleMapFactory` that implements `MapFactory` and changing one line in `Providers.initialize`.
The same goes for a different location tracking approach (e.g. a foreground service) behind `GeofenceMonitor`.

**No DI framework**: the scope is small, and a DI framework would itself be a library that can't be wrapped. New
classes take their dependencies as constructor parameters with production defaults, so unit tests pass fakes.

## Permissions
| Permission | Why |
|---|---|
| Precise location | Geofencing needs it; asked when opening the map (it also centers the map on the user) |
| Background location ("Allow all the time") | Monitoring while the app is not in the foreground; asked when saving, after explaining why |
| Notifications (Android 13+) | To show the reminder |
| Boot completed | To restore monitored locations after a reboot |
| Internet | Map tiles |

If the user declines, the memo is still saved and a message offers the system settings. Once the permission is
granted there, the memo locations are monitored from the next app start.

## Trying it out
Use an emulator **with Google Play services** (a "Google APIs" or "Google Play" image) or a real device.
1. Create a memo, pick a location on the map, save, and allow location "all the time" and notifications.
2. **Open Google Maps on the emulator and leave it open.** Geofencing tracks the position in a low-power mode that,
   on an emulator, only sees the emulated location while some app requests the precise location.
3. Move the emulator's location to a point about 1 km away, wait half a minute, then move to the memo location:
   *Extended controls → Location*, or `adb emu geo fix <longitude> <latitude>`. (Starting just outside the 200 m
   doesn't count as arriving: geofencing allows for location inaccuracy near the edge.) The reminder shows within
   seconds; on real devices the system may take longer to save battery.
4. App not running: swipe the app away or run `adb shell am kill com.sap.codelab`, then move out and in again. (Don't use
   *Force stop*: Android then delivers no broadcasts to the app at all until it is opened again.)
5. Reboot: `adb reboot`, open Google Maps again, then move out and in again.

Unit tests: `gradlew testDebugUnitTest`.

## Known limits and decisions
- **Reminders fire on arrival**: each time the user enters a memo's radius from outside, until the memo is marked as
  done. Already being within the radius when saving a memo doesn't count as arriving.
- **At most 100 locations** can be monitored at once (Google Play services limit).
- **Delays**: the system may delay geofence alerts to save battery (Doze, and some manufacturers' battery savers).
- **Play services**: geofencing needs Google Play services. Devices without it would need another `GeofenceMonitor`.
- **Database**: the base project stored the coordinates as `Long`. They are now `Double`; the database version
  changed with a destructive migration, since no version has been released yet.
- **Left as they were**: the base project's global `Repository` object, the list refresh right after saving
  (it can race with the save), and the blocking database calls.

## Tools
Claude Code (Anthropic's AI coding assistant) was used as an assistant during the implementation.
