🚲 Cycle Share

Cycle Share is a mobile application designed to provide an eco-friendly and cost-effective bicycle-sharing solution for underserved or rural areas with limited public transportation options. The app promotes health, reduces emissions, and offers accessible mobility to individuals who may not have private transportation.

📑 Table of Contents
Features
Technologies Used
Architecture
How It Works
Future Enhancements
Contributing
License
🌟 Features
👤 User Registration: Register and securely sign in to the app.
🏙 Station Management: Two stations, located at opposite ends of the service area, act as hubs for the bicycles.
🔒 QR Code Integration: Scan QR codes to unlock bicycles, access stations, and monitor locks.
📍 Location Tracking: Track the bicycle's location during the ride.
🌍 Eco-Friendly Transportation: Promotes sustainable, healthy travel for individuals in rural areas.
💸 Low-Cost Mobility Solution: An affordable alternative to private transportation.
🛠 Technologies Used

Android (Kotlin): Mobile application development.
Firebase: Authentication, Firestore, and Realtime Database.
ESP32: Hardware control for door locks and station tracking.
🏗 Architecture
The Cycle Share app is designed with a client-server architecture:

Mobile App:
User interface and functionalities for ride management, QR code scanning, and tracking.
Firebase:
Stores user data, ride session details, station information, and bicycle locations.
ESP32 Hardware:
Controls door locks and bicycle stations based on QR scans and sensor data.
🚴‍♂️ How It Works
Register & Login: Users register through the app, creating a unique profile for tracking ride history and payment details.
Station Access: Users scan QR codes at stations to access the station, unlock bicycles, and start a ride. Each station has an IR sensor for detecting bicycle parking.
Location Tracking: The app tracks the bicycle's location during the ride.
End of Ride: At the destination, users scan the QR code to enter the station. The IR sensor detects bicycle parking, and the system locks the bicycle.
Data Syncing & Billing: All ride data is synced with Firebase to maintain records and update real-time statuses. Once the ride is completed, the system issues a bill based on the duration and distance traveled.
🚀 Future Enhancements
📍 Additional Stations: Support for adding more stations across various areas.
💳 Ride Payment Integration: Implement a payment gateway for seamless billing.
📊 Analytics Dashboard: An admin dashboard for tracking station usage and ride statistics.
⭐ User Feedback: Enable users to rate rides and provide feedback.
