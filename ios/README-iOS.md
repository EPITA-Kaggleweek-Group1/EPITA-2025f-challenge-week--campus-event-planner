# Campus Event Planner — iOS

## Setup

1. Open Xcode and create a new **App** project:
   - Product Name: `CampusEventPlanner`
   - Interface: **Storyboard**
   - Language: **Swift**

2. Copy the source files from this directory into the Xcode project:
   - `Models/Event.swift`
   - `Services/APIClient.swift`
   - `Views/EventListViewController.swift`
   - `Views/EventDetailViewController.swift`

3. In your `SceneDelegate.swift` (or `AppDelegate.swift` for older templates),
   set the root view controller:

   ```swift
   let nav = UINavigationController(
       rootViewController: EventListViewController(style: .plain)
   )
   window?.rootViewController = nav
   window?.makeKeyAndVisible()
   ```

4. Replace the generated `Info.plist` with the one provided (or merge the
   `NSAppTransportSecurity` key) to allow HTTP connections to localhost.

5. Make sure the Flask backend is running on `localhost:5000` before launching
   the simulator.

## What works

- Event list loads from `GET /events` and displays in a table view.
- Tapping an event navigates to the detail screen (but data is not loaded yet).

## Student TODOs

- Load event details in `EventDetailViewController`
- Build the registration form and wire it to `POST /events/<id>/register`
- Show remaining spots on the detail screen
- Implement search with `UISearchController`
- Add a favorites feature with local storage (`UserDefaults`)
