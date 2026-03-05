# 💻 Laptop E-commerce System with AI & Online Payment

A comprehensive full-stack e-commerce solution featuring a modern Android mobile client and a robust PHP backend API. This project demonstrates advanced integrations including AI-driven features and online payment processing.

## 🚀 Technologies Used
* **Frontend:** Android (Java/Kotlin), XML.
* **Backend:** PHP (RESTful API), MySQL.
* **Cloud & Real-time:** Firebase (Realtime Database, Authentication).
* **AI Integration:** Google Gemini AI API.
* **Payment Gateway:** PayOS Integration.
* **Dev Tools:** ngrok (for local tunneling and webhook testing), Postman, Android Studio.

## ✨ Key Features
* **Smart Shopping:** Browse, filter, and sort laptops dynamically via PHP API.
* **AI Assistant:** Integrated Google Gemini AI to provide smart suggestions or support (AI-powered features).
* **Online Payment:** Secure checkout flow integrated with PayOS for real-time transactions.
* **Real-time Sync:** Orders and transaction statuses are synced instantly using Firebase.
* **User Management:** Secure Sign-up and Login system with data stored in MySQL/Firebase.

## 📸 Project Preview
<p align="center">
  <img src="screenshots/manhinhchinh.png" width="200" />
  <img src="screenshots/payos.png" width="200" /> 
  <img src="screenshots/payos_thanhcong.png" width="200" />
  <img src="screenshots/troliai.png" width="200" />
</p>

## 🛠️ Installation & Setup
1. **Clone the project:** `git clone https://github.com/wood-lang/laptop-ecommerce-android.git`

2. **Backend Setup:**
   * Import the MySQL database schema (SQL file).
   * Configure your server environment (XAMPP/WAMP).
   * Update `connectDB.php` with your database credentials.

3. **Security Configuration (Important):**
   * This project requires several API keys to function. For security reasons, these keys are **not** included in the source code.
   * Open `connectDB.php` and replace the following placeholders with your actual keys:
     - `YOUR_GEMINI_API_KEY_HERE`
     - `YOUR_PAYOS_CLIENT_ID`
     - `YOUR_PAYOS_API_KEY`
     - `YOUR_PAYOS_CHECKSUM_KEY`

4. **Run with ngrok:** Use `ngrok http 80` to expose your local PHP server for Android and PayOS Webhook testing.

5. **Mobile Client:**
   Open the Android project in **Android Studio**, update the `BASE_URL` to your ngrok/server link, and Build!
