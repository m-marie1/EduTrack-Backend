# Attendance System API Documentation

## Base URL

For deployment: `https://edutrack-backend-orms.onrender.com`

## API Path Structure

The API uses a double `/api` prefix in the URL path. For example:

- Correct format: `https://edutrack-backend-orms.onrender.com/api/api/courses`
- Not: `https://edutrack-backend-orms.onrender.com/api/courses`

## Authentication

The API uses JWT (JSON Web Token) based authentication.

### Register a New User

```
POST /api/api/auth/register
```

**Request Body:**

```json
{
  "username": "john.doe",
  "password": "password123",
  "fullName": "John Doe",
  "email": "john.doe@example.com"
}
```

**Response:**

```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification code.",
  "data": {
    "email": "john.doe@example.com",
    "verificationCode": "123456", // For testing only
    "message": "A verification code has been sent to john.doe@example.com. If you don't receive it, check logs or contact support."
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Verify Email

```
POST /api/api/auth/verify-email
```

**Request Body:**

```json
{
  "email": "john.doe@example.com",
  "code": "123456"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "john.doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "role": "ROLE_STUDENT"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Login

```
POST /api/api/auth/login
```

**Request Body:**

```json
{
  "username": "john.doe",
  "password": "password123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "john.doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "role": "ROLE_STUDENT"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Courses

### Get All Courses

```
GET /api/api/courses
Authorization: Bearer YOUR_TOKEN_HERE
```

**Response:**

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "courseCode": "CS101",
      "courseName": "Introduction to Computer Science",
      "description": "Fundamental concepts of programming",
      "startTime": "09:00:00",
      "endTime": "10:30:00",
      "days": ["MONDAY", "WEDNESDAY"]
    }
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Get Current Active Courses

```
GET /api/api/courses/current
Authorization: Bearer YOUR_TOKEN_HERE
```

**Response:** Similar to Get All Courses, but filtered to currently active courses.

### Get Enrolled Courses

```
GET /api/api/courses/enrolled
Authorization: Bearer YOUR_TOKEN_HERE
```

**Response:** Returns courses the current user is enrolled in.

### Create a New Course (Admin Only)

```
POST /api/api/courses
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Request Body:**

```json
{
  "courseCode": "CS501",
  "courseName": "Advanced Algorithms",
  "description": "Study of advanced algorithmic techniques",
  "startTime": "14:00:00",
  "endTime": "15:30:00",
  "days": ["MONDAY", "WEDNESDAY"]
}
```

**Response:**

```json
{
  "success": true,
  "message": "Course created successfully",
  "data": {
    "id": 6,
    "courseCode": "CS501",
    "courseName": "Advanced Algorithms",
    "description": "Study of advanced algorithmic techniques",
    "startTime": "14:00:00",
    "endTime": "15:30:00",
    "days": ["MONDAY", "WEDNESDAY"]
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Update a Course (Admin Only)

```
PUT /api/api/courses/{courseId}
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Request Body:** Same as Create Course

**Response:** Similar to Create Course response

### Delete a Course (Admin Only)

```
DELETE /api/api/courses/{courseId}
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Response:**

```json
{
  "success": true,
  "message": "Course deleted successfully",
  "data": null,
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Attendance

### Enroll in a Course

```
POST /api/api/attendance/enroll/{courseId}
Authorization: Bearer YOUR_TOKEN_HERE
```

**Response:**

```json
{
  "success": true,
  "message": "Successfully enrolled in Introduction to Computer Science",
  "data": "You are now enrolled in CS101",
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Record Attendance

```
POST /api/api/attendance/record
Authorization: Bearer YOUR_TOKEN_HERE
```

**Request Body:**

```json
{
  "courseId": 1,
  "networkIdentifier": "College-WiFi",
  "verificationMethod": "WIFI"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Attendance recorded successfully",
  "data": {
    "id": 1,
    "studentName": "John Doe",
    "studentId": "S54321",
    "courseCode": "CS101",
    "courseName": "Introduction to Computer Science",
    "timestamp": "2023-03-23T12:34:56.789",
    "verified": true,
    "verificationMethod": "WIFI"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Get User Attendance for Course

```
GET /api/api/attendance/user/{userId}/course/{courseId}
Authorization: Bearer YOUR_TOKEN_HERE
```

**Response:**

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "studentName": "John Doe",
      "studentId": "S54321",
      "courseCode": "CS101",
      "courseName": "Introduction to Computer Science",
      "timestamp": "2023-03-23T09:15:00",
      "verified": true,
      "verificationMethod": "WIFI"
    }
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Professor Requests

### Submit Professor Request (No Authentication Required)

```
POST /api/api/professor-requests
```

**Request Body:**

```json
{
  "fullName": "Professor Smith",
  "email": "prof.smith@university.edu",
  "idImageUrl": "/uploads/id-12345.jpg",
  "department": "Computer Science",
  "additionalInfo": "Specialized in Artificial Intelligence"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Request submitted successfully",
  "data": {
    "id": 1,
    "fullName": "Professor Smith",
    "email": "prof.smith@university.edu",
    "idImageUrl": "/uploads/id-12345.jpg",
    "department": "Computer Science",
    "additionalInfo": "Specialized in Artificial Intelligence",
    "status": "PENDING",
    "requestDate": "2023-03-23T12:34:56.789"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Upload ID Image for Professor Request (No Authentication Required)

```
POST /api/api/upload/public
Content-Type: multipart/form-data
```

**Form Data:**

- `file`: The image file to upload

**Response:**

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileName": "id-12345.jpg",
    "fileUrl": "/uploads/id-12345.jpg",
    "contentType": "image/jpeg",
    "fileSize": 253421,
    "uploadedAt": "2023-03-23T12:34:56.789"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Get Pending Professor Requests (Admin Only)

```
GET /api/api/professor-requests
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Response:**

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "fullName": "Professor Smith",
      "email": "prof.smith@university.edu",
      "idImageUrl": "/uploads/id-12345.jpg",
      "department": "Computer Science",
      "additionalInfo": "Specialized in Artificial Intelligence",
      "status": "PENDING",
      "requestDate": "2023-03-23T12:34:56.789"
    }
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Review Professor Request (Admin Only)

```
PUT /api/api/professor-requests/{requestId}/review
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Request Body:**

```json
{
  "approved": true
}
```

or

```json
{
  "approved": false,
  "rejectionReason": "Insufficient credentials"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Request approved successfully",
  "data": {
    "id": 1,
    "fullName": "Professor Smith",
    "email": "prof.smith@university.edu",
    "idImageUrl": "/uploads/id-12345.jpg",
    "department": "Computer Science",
    "additionalInfo": "Specialized in Artificial Intelligence",
    "status": "APPROVED",
    "requestDate": "2023-03-23T12:34:56.789",
    "reviewDate": "2023-03-24T10:15:30.123",
    "reviewedBy": "admin_edutrack"
  },
  "timestamp": "2023-03-24T10:15:30.456"
}
```

## User Roles and Permissions

The system supports three types of users, each with different permissions:

### Admin

- Credentials: Username: `admin_edutrack`, Password: `A9$k2pL8#xB7!fR3`
- Permissions:
  - View, create, update, and delete courses
  - Review professor requests
  - Access admin-specific endpoints

### Professor

- Created when an admin approves a professor request
- Permissions:
  - Create quizzes and assignments
  - Access professor-specific endpoints
  - View enrolled students and record attendance

### Student

- Default role for registered users
- Permissions:
  - Enroll in courses
  - Record attendance
  - View available courses and attendance history

## File Upload

### Upload File (Authenticated)

```
POST /api/api/upload
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: multipart/form-data
```

**Form Data:**

- `file`: The file to upload

**Response:**

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileName": "document.pdf",
    "fileUrl": "/uploads/b4f9c8d7-e6f5-4a3b-b2d1-c0d9e8f7a6b5.pdf",
    "contentType": "application/pdf",
    "fileSize": 253421,
    "uploadedAt": "2023-03-23T12:34:56.789"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```
