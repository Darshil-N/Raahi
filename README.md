<p align="center">
  <img src="https://github.com/Anonymous-7777/Raahi/blob/main/Logo.png" alt="Project Raahi Logo" width="150">
</p>

<h1 align="center">Project Raahi </h1>

<p align="center">
  <strong>A Smart Tourist Safety & Convenience Ecosystem</strong>
  <br />
</p>

<p align="center">
  <a href="https://raahi-eta.vercel.app//"><img src="https://img.shields.io/badge/Live-Dashboard-brightgreen?style=for-the-badge&logo=firebase" alt="Live Dashboard"></a>
  <a href="https://github.com/Anonymous-7777/Raahi"><img src="https://img.shields.io/badge/Download-Android_APK-blue?style=for-the-badge&logo=android" alt="Download APK"></a>
</p>

---

## 1. Overview

Welcome to Project Raahi. We started this project with a powerful question: how can we use technology to make every tourist feel truly safe, no matter how far off the beaten path they venture, while also making their journey seamless and enjoyable?

This platform is our answer. It's a complete safety and convenience ecosystem designed from the ground up to empower both visitors and local authorities, creating a trusted environment for exploration and adventure.

## 2. The Problem

The freedom to explore beautiful, remote places like Northeast India comes with a hidden worry: what happens if you get into trouble with no phone signal? A dream trip can quickly become a nightmare when you're alone and help is hours away. This is more than a logistical problem; it’s about providing profound peace of mind for every traveler and their family, and ensuring regions can safely unlock their full tourism potential.

## 3. The Solution: Raahi - Your Digital Companion & Command Center

Raahi is a robust digital ecosystem that directly addresses these challenges. It replaces outdated, reactive methods with a proactive, data-driven approach, fostering trust, boosting the local economy, and streamlining incident response.

* **🛡️ Blockchain-Verified Digital ID:** A secure, tamper-proof digital ID for each tourist, issued on the Polygon network. Crucially, registrations are bundled and sent as an **array to the blockchain** to reduce gas fees and accelerate processing at entry points. This ID is linked to a physical **NFC wristband** for instant, on-the-ground verification by authorities.

* **📱 Smart Mobile App:** A native Android app serving as the tourist's all-in-one companion.
    * **Panic Button with SMS Fallback:** A one-touch SOS button that instantly shares live location. If internet is lost, it **automatically sends an SMS with GPS coordinates**, ensuring help always gets through.
    * **Integrated Travel Hub:** Securely stores **travel itineraries** and documents like **flight and train tickets**. Features an integrated **map showing nearby monuments and police stations**.
    * **Convenience Bookings:** Seamlessly book **local transport** and **monument tickets** directly within the app.
    * **AI-Powered Safety Score:** A dynamic score assigned to the user, monitoring for unusual activity patterns.
    * **Voice Assistant (In Progress):** Enhancing accessibility with voice commands for app features and emergency triggers.

* **🗺️ Unified Authorities' Dashboard:** A real-time command center for police and tourism departments.
    * **Live Situational Awareness:** Provides real-time map visualizations, including **heatmaps** for tourist clusters and potential overcrowding.
    * **Smart Emergency Resource Mapper:** When an emergency is triggered, the dashboard automatically identifies and maps the **nearest critical resources** (e.g., hospitals, pharmacies, mechanics) based on incident type, drawing optimal routes.
    * **Crowd Control:** Authorities can **temporarily block ticket bookings** for specific areas directly from the dashboard to prevent overcrowding.
    * **Investigative Tools (In Progress):** Enhancements to access **past travel history of tourists with consistently low safety scores** to identify risk patterns.

## 4. Demo Video

* [**Watch the Demo Video on YouTube**](https://youtu.be/EfS6qsVRZcI?feature=shared)

## Demo Credentials

To test the mobile app and web dashboard, please use the following pre-configured credentials for our test users.

### 📱 For the Tourist Mobile App (Raahi)

* **Username:** `king@gmail.com`
* **Password:** `321654987`

### 🖥️ For the Authorities' Web Dashboard

* **Username:** `aadya@p.com`
* **Password:** `12345678`

 

## 5. Architecture Highlights

The platform is built on a modern, serverless architecture designed for massive scalability, real-time performance, and resilience.

* **Serverless-First Architecture:** The entire backend is built on Google's serverless platform, Firebase. This includes **Cloud Functions** for custom logic, **Firestore** for data, and **Authentication**, completely eliminating the need to manage traditional server infrastructure.
* **Real-time Data Core:** The platform leverages both **Cloud Firestore** and the **Firebase Realtime Database** to provide instantaneous data synchronization for the live map and emergency alerts, ensuring a highly responsive system for safety-critical events.
* **Geospatial Querying via Geohashing:** This architecture uses **Leaflet** with the Firebase Realtime Database. This allows for efficient, scalable radius-based queries (e.g., "find all users within 5km"), which is a core requirement for the dashboard.
* **Privacy by Design:** Enforces a strict separation of on-chain vs. off-chain data. Only an anonymous, non-personal hash is ever stored on the public blockchain, while all sensitive user data resides in the secure, off-chain Firestore database.

## 6. Technology Stack

* **Frontend (Mobile):** Native Android (**Kotlin**/Jetpack Compose). 
* **Frontend (Web):** **React.js** with TypeScript, Mapbox, Firebase Client SDK.
* **Backend (Serverless):** **Firebase Cloud Functions** (written in Node.js/TypeScript).
* **Core BaaS:** The full **Firebase Suite**, including **Firebase Authentication** for user management and **Cloud Storage for Firebase** for files.
* **Database (Primary):** **Cloud Firestore** (NoSQL) for all primary user data, profiles, and itineraries.
* **Database (Real-time & Geospatial):** **Firebase Realtime Database** with the **Leaflet** library for live location tracking and proximity queries.
* **Blockchain:** **Solidity** Smart Contract on **Polygon** (Layer 2).
* **DevOps:** **Git & GitHub** for version control and **Firebase Hosting** for the web dashboard.


## 7. Getting Started

To get a local copy up and running, follow these steps.

### Prerequisites
* Node.js (v18+)
* Android Studio (latest version)
* A Firebase account (free "Spark" plan)
* Firebase CLI: `npm install -g firebase-tools`

### Firebase Project Setup
1.  Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2.  In your project, enable **Firestore**, **Realtime Database**, **Authentication** (with Email/Password provider), and **Storage**.
3.  Upgrade your project to the "Blaze (Pay as you go)" plan to enable Cloud Functions. *You will not be charged as long as you stay within the generous free tier.*
4.  Register a new **Web App** in your project settings. Copy the `firebaseConfig` object.
5.  Register a new **Android App**. Follow the steps to download the `google-services.json` file.

### Local Installation
1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/Anonymous-7777/Raahi](https://github.com/Anonymous-7777/Raahi)
    ```
2.  **Web Dashboard Setup:**
    * Navigate to the `web-dashboard` directory.
    * Create a `.env.local` file and place your Web App's `firebaseConfig` variables into it.
    * Install dependencies and run: `npm install && npm start`

3.  **Firebase Functions (Backend) Setup:**
    * Navigate to the `functions` directory.
    * Install dependencies: `npm install`
    * Deploy your functions: `firebase deploy --only functions`

4.  **Mobile App Setup:**
    * Place the `google-services.json` file you downloaded into the `/mobile-app/app` directory.
    * Open the `/mobile-app` project in Android Studio and let Gradle sync.
    * Run the app on an emulator or a physical device.

## 8. Acknowledgments

* The logo and visual assets used in this project were generated using AI image generation tools.
## 9. Contributors
Team Paradox
1. Nathwani Darshil
2. Aadya Baranwal
3. Aditya R Murthy
4. Musaddik Jamadar
5. Anand Raj
6. M Jahnavi Reddy
