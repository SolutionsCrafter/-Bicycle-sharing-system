#include <WiFi.h>
#include <FirebaseESP32.h>

// Replace with your network credentials
const char* ssid = "YOUR_SSID";
const char* password = "dt$$4%5fy";

// Firebase project credentials
#define FIREBASE_HOST "cycle-share-mobile-app"
#define FIREBASE_AUTH "8KHPh5KWP7MXV3Ow1BMBxHkt3QaxXjLxHSwTqAuC" // Use the Database Secret

// Define Firebase Data object
FirebaseData firebaseData;

const int station1LockPin = 5; // GPIO pin connected to station 1 lock
const int station2LockPin = 18; // GPIO pin connected to station 2 lock

const int station1TrigPin = 12; // GPIO pin for Station 1 ultrasonic sensor trigger
const int station1EchoPin = 14; // GPIO pin for Station 1 ultrasonic sensor echo
const int station2TrigPin = 27; // GPIO pin for Station 2 ultrasonic sensor trigger
const int station2EchoPin = 26; // GPIO pin for Station 2 ultrasonic sensor echo

long readUltrasonicDistance(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  long duration = pulseIn(echoPin, HIGH);
  long distance = (duration / 2) / 29.1; // Convert to cm
  return distance;
}

void setup() {
  Serial.begin(115200);
  
  // Initialize WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("Connected to Wi-Fi");
  
  // Initialize Firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  // Set up the lock pins as output
  pinMode(station1LockPin, OUTPUT);
  pinMode(station2LockPin, OUTPUT);

  // Set up the ultrasonic sensor pins as output/input
  pinMode(station1TrigPin, OUTPUT);
  pinMode(station1EchoPin, INPUT);
  pinMode(station2TrigPin, OUTPUT);
  pinMode(station2EchoPin, INPUT);

  // Initially lock both stations
  digitalWrite(station1LockPin, HIGH); // Lock Station 1
  digitalWrite(station2LockPin, HIGH); // Lock Station 2
}

void loop() {
  // Read the value of station1
  if (Firebase.getInt(firebaseData, "/station1")) {
    int station1Status = firebaseData.intData();
    Serial.print("Station 1 status: ");
    Serial.println(station1Status);
    if (station1Status == 0) {
      digitalWrite(station1LockPin, LOW); // Unlock Station 1
    } else {
      digitalWrite(station1LockPin, HIGH); // Lock Station 1
    }
  } else {
    Serial.println("Failed to get /station1 value, reason: " + firebaseData.errorReason());
  }

  // Read the value of station2
  if (Firebase.getInt(firebaseData, "/station2")) {
    int station2Status = firebaseData.intData();
    Serial.print("Station 2 status: ");
    Serial.println(station2Status);
    if (station2Status == 0) {
      digitalWrite(station2LockPin, LOW); // Unlock Station 2
    } else {
      digitalWrite(station2LockPin, HIGH); // Lock Station 2
    }
  } else {
    Serial.println("Failed to get /station2 value, reason: " + firebaseData.errorReason());
  }

  // Check if a bicycle is parked at Station 1
  long distance1 = readUltrasonicDistance(station1TrigPin, station1EchoPin);
  if (distance1 < 10) { // Assuming <10 cm means a bicycle is parked
    digitalWrite(station1LockPin, HIGH); // Lock Station 1
    Firebase.setInt(firebaseData, "/station1", 1); // Update Firebase
    Serial.println("Bicycle detected at Station 1, locking and updating Firebase.");
  }

  // Check if a bicycle is parked at Station 2
  long distance2 = readUltrasonicDistance(station2TrigPin, station2EchoPin);
  if (distance2 < 10) { // Assuming <10 cm means a bicycle is parked
    digitalWrite(station2LockPin, HIGH); // Lock Station 2
    Firebase.setInt(firebaseData, "/station2", 1); // Update Firebase
    Serial.println("Bicycle detected at Station 2, locking and updating Firebase.");
  }

  // Add a delay to avoid hitting Firebase too frequently
  delay(1000);
}
