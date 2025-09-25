# PROJECT SETUP

## 1. Persiapan Environtment

   - Java 17+
   - Maven 3.8+
   - Docker

## 2. Clone Repository
   ```bash
   git clone https://github.com/farizibnu/payment-gateway-cip
   cd payment-gateway-cip
   ```
## 3. Install Dependencies
   ```bash
   mvn clean install
   ```
# RUN INSTRUCTIONS

## 1. Jalankan docker compose
   Akan menjalankan:
   - Database PostgreSQL (port: 5432)
   - Kafka (port: 9092)
   - Zookeeper (port: 2181)
   - Keycloak (port: 8083)
   - Aplikasi Payment Gateway (port: 8080)
   ```bash
   docker-compose up -d
   ```
## 2. Jalankan Aplikasi
   ```bash
   mvn spring-boot:run
   ```
## 3. Akses Aplikasi
   - Aplikasi Payment Gateway: `http://localhost:8080`
   - Keycloak Admin Console: `http://localhost:8083/admin` (default user: admin, password: admin)
## 4. Konfigurasi Keycloak
   - Buat realm baru dengan nama `payment-gateway` menggunakan import dari file `keycloak/realm-export.json`.
   - Gunakan user "demo/demo" untuk testing.
   - Dapatkan access token dari Keycloak untuk mengakses endpoint yang dilindungi.
   ```bash
   curl -X POST "http://localhost:8083/realms/payments/protocol/openid-connect/token" -d "client_id=payment-gateway" -d "username=demo" -d "password=demo" -d "grant_type=password" 
   ``` 
## 5. API - Gunakan Postman untuk menguji endpoint API.
- Gunakan postman collection yang ada di `postman/PaymentGateway.postman_collection.json`. 
- Pastikan untuk menambahkan token Bearer berupa access token dari keycloak di header setiap request yang memerlukan otentikasi. 
- ### POST /api/v1/payments untuk membuat pembayaran baru. Dengan request body:
    ```json
    {
    "orderId": "ORD-994",
    "channel": "MOBILE_BANKING",
    "amount": 99999,
    "currency": "IDR",
    "paymentMethod": "VA",
    "account": "12345678"
    }
    ```

- ### GET /api/v1/payments/{paymentId} untuk mendapatkan detail pembayaran berdasarkan paymentId.
## 6. Dokumentasi API
- Akses dokumentasi API Swagger di `http://localhost:8080/swagger-ui.html` untuk melihat dan menguji endpoint API yang tersedia.
## 7. Testing
 - Jalankan unit test dan integration test menggunakan perintah:
    ```bash
    mvn test
    ```
## 8. Stop Aplikasi
   ```bash
    docker-compose down