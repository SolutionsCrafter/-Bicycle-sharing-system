# 🚲 **Cycle Share**

**Cycle Share** is a mobile application designed to provide an eco-friendly and cost-effective bicycle-sharing solution for underserved or rural areas with limited public transportation options. The app promotes health, reduces emissions, and offers accessible mobility to individuals who may not have private transportation.
![over](https://github.com/user-attachments/assets/04be0d5f-4f57-4e0e-ac60-37a1690967d8)

---

## 📑 **Table of Contents**

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [How It Works](#how-it-works)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
- [License](#license)

---

## 🌟 **Features**

- **👤 User Registration**: Register and securely sign in to the app.
- **🏙 Station Management**: Two stations, located at opposite ends of the service area, act as hubs for the bicycles.
  ![Station Image](assets/station-image.png) <!-- Add the actual path to your image -->
- **🔒 QR Code Integration**: Scan QR codes to unlock bicycles, access stations, and monitor locks.
- **📍 Location Tracking**: Track the bicycle's location during the ride.
- **🌍 Eco-Friendly Transportation**: Promotes sustainable, healthy travel for individuals in rural areas.
- **💸 Low-Cost Mobility Solution**: An affordable alternative to private transportation.

---

## 🛠 **Technologies Used**

![Android](https://img.shields.io/badge/Android-Java-blue)
![Firebase](https://img.shields.io/badge/Firebase-Authentication%20%7C%20Firestore%20%7C%20Realtime%20Database-orange)
![ESP32](https://img.shields.io/badge/Hardware-ESP32-yellow)

- **Android (Kotlin)**: Mobile application development.
- **Firebase**: Authentication, Firestore, and Realtime Database.
- **ESP32**: Hardware control for door locks and station tracking.

---

## 🏗 **Architecture**

The Cycle Share app is designed with a **client-server** architecture:

- **Mobile App**:
  - User interface and functionalities for ride management, QR code scanning, and tracking.
- **Firebase**:
  - Stores user data, ride session details, station information, and bicycle locations.
- **ESP32 Hardware**:
  - Controls door locks and bicycle stations based on QR scans and sensor data.

---

## 🚴‍♂️ **How It Works**

1. **Register & Login**: Users register through the app, creating a unique profile for tracking ride history and payment details.
2. **Station Access**: Users scan QR codes at stations to access the station, unlock bicycles, and start a ride. Each station has an IR sensor for detecting bicycle parking.
3. **Location Tracking**: The app tracks the bicycle's location during the ride.
4. **End of Ride**: At the destination, users scan the QR code to enter the station. The IR sensor detects bicycle parking, and the system locks the bicycle.
5. **Data Syncing & Billing**: All ride data is synced with Firebase to maintain records and update real-time statuses. Once the ride is completed, the system issues a bill based on the duration and distance traveled.

---

## 📐 **Visual Insights**

To give a clearer picture of the app flow and station setup, here are some visual representations:

### 📱 App Wire Diagram
The following wire diagram illustrates the user interface flow of the Cycle Share app, helping users understand the app's key screens and navigation.

 ![wire](https://github.com/user-attachments/assets/64b711bc-17bd-42d5-9234-7d9c72ae68cd)

### 🛠 Station Structure Animation
This animation showcases the station structure and how bicycles are managed, including access points, QR code scanning locations, and the sensor setup for parking detection.



https://github.com/user-attachments/assets/e9e99b21-89d8-40aa-8252-8158e81802dd


---

## 🚀 **Future Enhancements**

- **📍 Additional Stations**: Support for adding more stations across various areas.
- **💳 Ride Payment Integration**: Implement a payment gateway for seamless billing.
- **📊 Analytics Dashboard**: An admin dashboard for tracking station usage and ride statistics.
- **⭐ User Feedback**: Enable users to rate rides and provide feedback.

---


