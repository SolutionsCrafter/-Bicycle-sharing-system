#include <WiFi.h>
#include <FirebaseESP32.h>
#include <ESP32Servo.h>

#define WIFI_SSID "Hiru"
#define WIFI_PASSWORD "1234hiru"

// Define GPIO pins
#define lock1Pin 18
#define lock2Pin 19
#define door1Pin 21
#define door2Pin 22
#define ir1Pin 23
#define ir2Pin 25

FirebaseData firebaseData;
FirebaseConfig firebaseConfig;
FirebaseAuth firebaseAuth;

// Servo objects for doors and locks
Servo lock1Servo;
Servo lock2Servo;
Servo door1Servo;
Servo door2Servo;

void setup() {
  Serial.begin(9600);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("Connected to Wi-Fi");

  firebaseConfig.host = "cycle-share-mobile-app-default-rtdb.firebaseio.com";
  firebaseConfig.signer.tokens.legacy_token = "8KHPh5KWP7MXV3Ow1BMBxHkt3QaxXjLxHSwTqAuC";

  Firebase.begin(&firebaseConfig, &firebaseAuth);
  Firebase.reconnectWiFi(true);

  // Set servo pins
  lock1Servo.attach(lock1Pin);
  lock2Servo.attach(lock2Pin);
  door1Servo.attach(door1Pin);
  door2Servo.attach(door2Pin);

  // Set IR sensor pins
  pinMode(ir1Pin, INPUT);
  pinMode(ir2Pin, INPUT);

  // Set servos to initial positions (closed)
  lock1Servo.write(0); // Closed
  lock2Servo.write(180); // Open
  door1Servo.write(0);
  door2Servo.write(0);

  // Set initial station status to 0 (unlocked)
  Firebase.setInt(firebaseData, "Station1", 1);
  Firebase.setInt(firebaseData, "Station2", 0);
}

void loop() {
  // Check IR sensor for Door1
  int ir1State = digitalRead(ir1Pin);
  if (ir1State == LOW) {
    // Open Door1 when IR sensor detects an object
    Serial.println("Opening Door1 due to IR sensor trigger");
    door1Servo.write(180); // Open door
    delay(5000);           // Keep door open for 5 seconds
    door1Servo.write(0);   // Close door
  }

  // Check IR sensor for Door2
  int ir2State = digitalRead(ir2Pin);
  if (ir2State == LOW) {
    // Open Door2 when IR sensor detects an object
    Serial.println("Opening Door2 due to IR sensor trigger");
    door2Servo.write(180); // Open door
    delay(5000);           // Keep door open for 5 seconds
    door2Servo.write(0);   // Close door
  }

  // Handle Door1 logic from Firebase
  if (Firebase.getInt(firebaseData, "Door1")) {
    int doorStatus = firebaseData.intData();
    if (doorStatus == 0) {
      // Open Door1 via Firebase command
      Serial.println("Opening Door1 via Firebase");
      door1Servo.write(180); // Open door
      delay(5000);           // Keep door open for 5 seconds
      door1Servo.write(0);   // Close door
      Firebase.setInt(firebaseData, "Door1", 1); // Update Firebase to close door
    }
  }

  // Handle Door2 logic from Firebase
  if (Firebase.getInt(firebaseData, "Door2")) {
    int doorStatus = firebaseData.intData();
    if (doorStatus == 0) {
      // Open Door2 via Firebase command
      Serial.println("Opening Door2 via Firebase");
      door2Servo.write(180); // Open door
      delay(5000);           // Keep door open for 5 seconds
      door2Servo.write(0);   // Close door
      Firebase.setInt(firebaseData, "Door2", 1); // Update Firebase to close door
    }
  }

  // Handle Station1 lock logic
  if (Firebase.getInt(firebaseData, "Station1")) {
    int stationStatus = firebaseData.intData();

    // Open lock if Station1 == 0
    if (stationStatus == 0) {
      Serial.println("Opening Lock1 at Station1");
      lock1Servo.write(180); // Unlock
      //Firebase.setInt(firebaseData, "Station1", 1); // Update Firebase to lock
    } else { // Station1 == 1
      Serial.println("Closing Lock1 at Station1");
      lock1Servo.write(0); // Lock
    }
  }

  // Handle Station2 lock logic
  if (Firebase.getInt(firebaseData, "Station2")) {
    int stationStatus = firebaseData.intData();

    // Open lock if Station2 == 0
    if (stationStatus == 0) {
      Serial.println("Opening Lock2 at Station2");
      lock2Servo.write(180); // Unlock
      //Firebase.setInt(firebaseData, "Station2", 1); // Update Firebase to lock
    } else { // Station2 == 1
      Serial.println("Closing Lock2 at Station2");
      lock2Servo.write(0); // Lock
    }
  }
}
