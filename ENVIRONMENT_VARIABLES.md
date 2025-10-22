# Environment Variables Configuration Guide

This document lists all environment variables needed for the Blood Bank Management System backend.

---

## 🚀 **For Railway Deployment**

Copy these to Railway Dashboard > Your Backend Project > Variables > Raw Editor:

```env
# Required Variables
SEMAPHORE_SMS_API_KEY=your-actual-semaphore-api-key
SEMAPHORE_SMS_API_URL=https://api.semaphore.co/api/v4/otp
SEMAPHORE_SMS_SENDER_NAME=Redsource
SMS_TESTING_MODE=false

JWT_SECRET=your-production-jwt-secret-here
MONGODB_URI=mongodb+srv://bloodbank-admin:password@cluster.mongodb.net/bloodbank?retryWrites=true&w=majority

CLOUDINARY_URL=cloudinary://api_key:api_secret@cloud_name
CLOUDINARY_API_KEY=your-cloudinary-key
CLOUDINARY_API_SECRET=your-cloudinary-secret
CLOUDINARY_CLOUD_NAME=your-cloud-name

CORS_ALLOWED_ORIGINS=https://your-frontend-domain.up.railway.app

PORT=8080
SPRING_PROFILES_ACTIVE=production
```

---

## 💻 **For Local Development**

Create a `.env` file in the backend root (this file is gitignored):

```env
SEMAPHORE_SMS_API_KEY=your-semaphore-api-key-here
SMS_TESTING_MODE=true
```

**Note**: Other variables have defaults in `application.properties` that work for local development.

---

## 📋 **Variable Descriptions**

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SEMAPHORE_SMS_API_KEY` | Your Semaphore SMS API key | ✅ Yes | - |
| `SEMAPHORE_SMS_API_URL` | Semaphore OTP endpoint | No | `https://api.semaphore.co/api/v4/otp` |
| `SEMAPHORE_SMS_SENDER_NAME` | SMS sender name | No | `Redsource` |
| `SMS_TESTING_MODE` | Enable/disable real SMS | No | `false` |
| `JWT_SECRET` | Secret for JWT tokens | ✅ Yes | Auto-generated (dev only) |
| `MONGODB_URI` | MongoDB connection string | ✅ Yes | Provided default (dev) |
| `CLOUDINARY_URL` | Cloudinary connection URL | ✅ Yes | - |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | ✅ Yes | `http://localhost:3000` |
| `PORT` | Server port | No | `8080` |

---

## ⚠️ **Security Notes**

1. ✅ **Never commit real API keys** to GitHub
2. ✅ Use environment variables for all sensitive data
3. ✅ The `application.properties` file uses placeholders - **safe to commit**
4. ✅ Railway environment variables **override** application.properties defaults
5. ✅ Keep your `.env` file in `.gitignore` (already configured)

---

## 🔐 **How It Works**

```properties
# application.properties format:
property=${ENV_VARIABLE:default_value}
```

**Example:**
```properties
semaphore.sms.api.key=${SEMAPHORE_SMS_API_KEY:your-semaphore-api-key-here}
```

- **Production (Railway)**: Uses `SEMAPHORE_SMS_API_KEY` from environment
- **Local Dev**: Uses `your-semaphore-api-key-here` as fallback

---

## ✅ **Ready to Push to GitHub?**

After updating `application.properties` with placeholders:

```bash
git add .
git commit -m "Add Semaphore SMS configuration with environment variables"
git push origin main
```

**Safe to push because:**
- ✅ Real API keys are in Railway environment variables
- ✅ Default values are placeholders or development-only
- ✅ `.env` files are gitignored
- ✅ Sensitive data never committed

---

## 📞 **Need Help?**

Check if your environment variables are loaded:
1. Railway Dashboard > Deployments > View Logs
2. Look for configuration values during startup
3. SMS should work if `SEMAPHORE_SMS_API_KEY` is set correctly

