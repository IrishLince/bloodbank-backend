// MongoDB Script to Insert Test Hospitals
// Run this in MongoDB Compass or MongoDB Shell

// 1. Pasay City General Hospital (NEARBY)
db.users_hospital.insertOne({
  "hospital_name": "Pasay City General Hospital",
  "email": "info@pasaycitygeneral.ph",
  "username": "PASAY_GENERAL",
  "password": "$2a$10$N9qo8uLOickgx2ZMRZoMye/Ek5J3nqMMWrxYh2xEWyb98637ws2lW", // password: "password123"
  "phone": "(02) 8834-4000",
  "address": "P Burgos St, Pasay City, Metro Manila",
  "hospital_id": "PASAY001",
  "license_number": "LIC001",
  "profile_photo_url": null,
  "coordinates": {
    "lat": 14.5378,
    "lng": 121.0014
  },
  "operating_hours": "8:00 AM - 5:00 PM",
  "blood_types_available": ["O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-"],
  "is_donation_center": true,
  "urgent_need": false,
  "created_at": new Date(),
  "updated_at": new Date()
});

// 2. Makati Medical Center (FARTHER)
db.users_hospital.insertOne({
  "_id": ObjectId("68dbb90a68748a2bc6391626"),
  "hospital_name": "Makati Medical Center", 
  "email": "info@makatimed.net.ph",
  "username": "MAKATI_MEDICAL",
  "password": "$2a$10$N9qo8uLOickgx2ZMRZoMye/Ek5J3nqMMWrxYh2xEWyb98637ws2lW", // password: "password123"
  "phone": "(02) 8888-8999",
  "address": "2 Amorsolo Street, Legazpi Village, Makati City, 1229 Metro Manila",
  "hospital_id": "MAKATI001", 
  "license_number": "LIC002",
  "profile_photo_url": null,
  "coordinates": {
    "lat": 14.5547,
    "lng": 121.0244
  },
  "operating_hours": "24/7",
  "blood_types_available": ["O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-"],
  "is_donation_center": true,
  "urgent_need": false,
  "created_at": new Date(),
  "updated_at": new Date()
});

print("✅ Test hospitals inserted successfully!");
print("📍 Pasay City General Hospital - NEARBY");
print("📍 Makati Medical Center - FARTHER");
print("🎯 Ready for distance testing!");
