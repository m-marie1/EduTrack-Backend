# Attendance System API Documentation

## Base URL

For development: `http://localhost:8080`
For production: `https://edutrack-backend-orms.onrender.com`

All API endpoints are prefixed with `/api`.

## Authentication

The API uses JWT (JSON Web Token) based authentication. Roles included in the token determine access rights.

### Register a New User

Registers a new user with the `STUDENT` role by default. Sends a verification email.

```
POST /api/auth/register
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

**Response (Success):**

```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification code.",
  "data": {
    "email": "john.doe@example.com",
    "verificationCode": "123456", // Included for debugging/testing, remove in final production
    "message": "A verification code has been sent to john.doe@example.com. If you don't receive it, check logs or contact support."
  },
  "timestamp": "..."
}
```

### Verify Email

Verifies the user's email using the code sent during registration. Returns a JWT token upon success.

```
POST /api/auth/verify-email
```

**Request Body:**

```json
{
  "email": "john.doe@example.com",
  "code": "123456"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "john.doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "role": "STUDENT" // Role included in response
  },
  "timestamp": "..."
}
```

### Login

Authenticates a user and returns a JWT token with user details including the role.

```
POST /api/auth/login
```

**Request Body:** (Can use username or email)

```json
{
  "username": "john.doe", // or "email": "john.doe@example.com"
  "password": "password123"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "john.doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "role": "STUDENT" // Role included in response
  },
  "timestamp": "..."
}
```

## Courses

### Get All Courses

Retrieves a list of all available courses.

```
GET /api/courses
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** `ADMIN`

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
    // ... more courses
  ],
  "timestamp": "..."
}
```

### Create Course (Admin Only)

Creates a new course.

```
POST /api/courses
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Permissions:** `ADMIN`

**Request Body:**

```json
{
  "courseCode": "CS101",
  "courseName": "Introduction to Programming",
  "description": "Learn the basics of programming with Java",
  "startTime": "08:00:00",
  "endTime": "09:30:00",
  "days": ["MONDAY", "WEDNESDAY", "FRIDAY"] // Set of DayOfWeek enums
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Course created successfully",
  "data": {
    // Details of the created course
    "id": 1,
    "courseCode": "CS101"
    // ... other fields
  },
  "timestamp": "..."
}
```

### Update Course (Admin Only)

Updates an existing course by its ID.

```
PUT /api/courses/{id}
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Permissions:** `ADMIN`

**Request Body:** (Same structure as Create Course)

**Response (Success):**

```json
{
  "success": true,
  "message": "Course updated successfully",
  "data": {
    // Details of the updated course
    "id": 1,
    "courseCode": "CS101"
    // ... other fields
  },
  "timestamp": "..."
}
```

### Delete Course (Admin Only)

Deletes a course by its ID.

```
DELETE /api/courses/{id}
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Permissions:** `ADMIN`

**Response (Success):**

```json
{
  "success": true,
  "message": "Course deleted successfully",
  "data": null,
  "timestamp": "..."
}
```

### Get Current Active Courses

Retrieves courses currently in session based on the server's time.

```
GET /api/courses/current
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** Authenticated User (`STUDENT`, `PROFESSOR`, `ADMIN`)

**Response:** Similar structure to Get All Courses, filtered for active sessions.

### Get Enrolled Courses

Retrieves courses the currently authenticated user is enrolled in.

```
GET /api/courses/enrolled
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** Authenticated User (`STUDENT`, `PROFESSOR`, `ADMIN`)

**Response:** Similar structure to Get All Courses, filtered for enrolled courses.

### Create Sample Courses (For Testing)

Creates sample courses if none exist.

```
POST /api/courses/sample
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** Authenticated User (can be restricted further if needed)

**Response:**

```json
{
  "success": true,
  "message": "Created 5 sample courses" // or "Sample courses already exist"
}
```

## Attendance

### Enroll in a Course

Enrolls the authenticated user in the specified course.

```
POST /api/attendance/enroll/{courseId}
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** Authenticated User (`STUDENT`, `PROFESSOR`)

**Response:**

```json
{
  "success": true,
  "message": "Successfully enrolled in [Course Name]",
  "data": "You are now enrolled in [Course Code]",
  "timestamp": "..."
}
```

### Record Attendance

Records attendance for the authenticated user in the specified course. Requires validation (e.g., network identifier).

```
POST /api/attendance/record
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** Authenticated User (`STUDENT`, `PROFESSOR`)

**Request Body:**

```json
{
  "courseId": 1,
  "networkIdentifier": "College-WiFi", // Or other verification data
  "verificationMethod": "WIFI" // Or other method like QR
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Attendance recorded successfully",
  "data": {
    // Details of the attendance record
    "id": 1,
    "studentName": "John Doe",
    "studentId": "S54321", // May be null or removed
    "courseCode": "CS101",
    "courseName": "Introduction to Computer Science",
    "timestamp": "...",
    "verified": true,
    "verificationMethod": "WIFI"
  },
  "timestamp": "..."
}
```

### Get User Attendance for Course

Retrieves attendance records for a specific user (by ID) in a specific course.

```
GET /api/attendance/user/{userId}/course/{courseId}
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** `ADMIN` or `PROFESSOR` (for their courses)

**Response:**

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    // List of attendance records
    {
      "id": 1
      // ... other fields
    }
  ],
  "timestamp": "..."
}
```

### Get Current User Attendance for Course

Retrieves attendance records for the _currently authenticated_ user in a specific course.

```
GET /api/attendance/user/current/course/{courseId}
Authorization: Bearer YOUR_TOKEN_HERE
```

**Permissions:** Authenticated User (`STUDENT`, `PROFESSOR`)

**Response:** Similar structure to Get User Attendance.

## Professor Requests

### Submit Professor Request

Allows anyone (unauthenticated) to submit a request for a professor account. Requires details and an ID image upload.

```
POST /api/professor-requests
```

**Permissions:** Public

**Request Body:**

```json
{
  "fullName": "Professor Smith",
  "email": "prof.smith@university.edu",
  "idImageUrl": "/uploads/unique-id-image.jpg", // URL returned by public file upload
  "department": "Computer Science",
  "additionalInfo": "Specialized in Artificial Intelligence"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Request submitted successfully",
  "data": {
    // Details of the submitted request
    "id": 1,
    "fullName": "Professor Smith",
    "email": "prof.smith@university.edu",
    "idImageUrl": "/uploads/unique-id-image.jpg",
    "department": "Computer Science",
    "additionalInfo": "Specialized in Artificial Intelligence",
    "status": "PENDING",
    "requestDate": "..."
  },
  "timestamp": "..."
}
```

### Get All Pending Professor Requests (Admin Only)

Retrieves all professor requests with the status `PENDING`.

```
GET /api/professor-requests
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Permissions:** `ADMIN`

**Response:**

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    // List of pending requests
    {
      "id": 1
      // ... other fields from submit request response
    }
  ],
  "timestamp": "..."
}
```

### Review Professor Request (Admin Only)

Allows an admin to approve or reject a pending professor request. If approved, creates a new user account with the `PROFESSOR` role and sends credentials via email.

```
PUT /api/professor-requests/{requestId}/review
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Permissions:** `ADMIN`

**Request Body (Approve):**

```json
{
  "approved": true
}
```

**Request Body (Reject):**

```json
{
  "approved": false,
  "rejectionReason": "Insufficient credentials provided."
}
```

**Response (Approval Success):**

```json
{
  "success": true,
  "message": "Request approved successfully",
  "data": {
    // Details of the reviewed request (status now APPROVED)
    "id": 1,
    "status": "APPROVED"
    // ... other fields
  }
}
```

**Response (Rejection Success):**

```json
{
  "success": true,
  "message": "Request rejected successfully",
  "data": {
    // Details of the reviewed request (status now REJECTED)
    "id": 1,
    "status": "REJECTED",
    "rejectionReason": "Insufficient credentials provided."
    // ... other fields
  }
}
```

_(Note: Previous documentation mentioned sending credentials in the response data; this is generally insecure. Credentials should be sent securely via email, and the response confirms the action.)_

## File Upload

### Upload File (Authenticated)

Uploads a file for authenticated users (e.g., assignment submissions).

```
POST /api/upload
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: multipart/form-data
```

**Permissions:** Authenticated User

**Form Data:**

- `file`: The file to upload

**Response:**

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileName": "document.pdf",
    "fileUrl": "/uploads/unique-filename.pdf", // Relative URL to access the file
    "contentType": "application/pdf",
    "fileSize": 253421,
    "uploadedAt": "..."
  },
  "timestamp": "..."
}
```

### Upload File (Public)

Uploads a file for public use cases, specifically for professor ID image uploads during requests.

```
POST /api/upload/public
Content-Type: multipart/form-data
```

**Permissions:** Public

**Form Data:**

- `file`: The ID image file to upload

**Response:** Same structure as the authenticated upload response. The `fileUrl` should be used in the `idImageUrl` field when submitting the professor request.

## Quizzes (Placeholder)

Endpoints for quiz creation, retrieval, starting, and submission exist but are not fully documented here yet. Requires `PROFESSOR` role for creation/management and `STUDENT` role for taking.

```
POST /api/quizzes (Professor)
GET /api/quizzes/available?courseId={id} (Student/Professor)
POST /api/quizzes/{quizId}/start (Student)
POST /api/quizzes/{quizId}/submit (Student)
```

## Assignments (Placeholder)

Endpoints for assignment creation, retrieval, submission, and grading exist but are not fully documented here yet. Requires `PROFESSOR` role for creation/grading and `STUDENT` role for submission.

```
POST /api/assignments (Professor)
GET /api/assignments/active?courseId={id} (Student/Professor)
POST /api/assignments/{assignmentId}/submit (Student)
POST /api/assignments/submissions/{submissionId}/grade (Professor)
```

## Admin User

An admin user is automatically created on application startup with the following default credentials (change in production):

- **Username:** `admin_edutrack`
- **Password:** `A9$k2pL8#xB7!fR3`
- **Email:** `admin@edutrack.com`

Use these credentials to log in via `POST /api/auth/login` to obtain an admin JWT token.
