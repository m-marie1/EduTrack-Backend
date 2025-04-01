# Attendance System API Documentation

## Base URL

For development: `http://localhost:8080`

## Authentication

The API uses JWT (JSON Web Token) based authentication.

### Register a New User

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

**Response:**

```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification code.",
  "data": "A verification code has been sent to john.doe@example.com",
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Verify Email

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

**Response:**

```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "john.doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Login

```
POST /api/auth/login
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
    "email": "john.doe@example.com"
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Courses

### Get All Courses

```
GET /api/courses
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
    },
    ...
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Get Current Active Courses

```
GET /api/courses/current
Authorization: Bearer YOUR_TOKEN_HERE
```

**Response:** Similar to Get All Courses, but filtered to currently active courses.

## Attendance

### Enroll in a Course

```
POST /api/attendance/enroll/{courseId}
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
POST /api/attendance/record
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
GET /api/attendance/user/{userId}/course/{courseId}
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
    },
    ...
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Professor Requests

### Submit Professor Request

```
POST /api/professor-requests
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

### Get All Pending Professor Requests (Admin Only)

```
GET /api/professor-requests
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
    },
    ...
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Review Professor Request (Admin Only)

```
POST /api/professor-requests/{requestId}/review
Authorization: Bearer ADMIN_TOKEN_HERE
```

**Request Body:**

```json
{
  "approved": true,
  "reviewedBy": "admin"
}
```

or

```json
{
  "approved": false,
  "rejectionReason": "Insufficient credentials",
  "reviewedBy": "admin"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Professor account created successfully",
  "data": "Login credentials have been sent to the professor's email",
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Quizzes

### Create Quiz (Professor Only)

```
POST /api/quizzes
Authorization: Bearer PROFESSOR_TOKEN_HERE
```

**Request Body:**

```json
{
  "title": "Midterm Exam",
  "description": "Covers chapters 1-5",
  "courseId": 1,
  "startDate": "2023-04-01T09:00:00",
  "endDate": "2023-04-01T23:59:59",
  "durationMinutes": 60,
  "questions": [
    {
      "text": "What is the capital of France?",
      "type": "MULTIPLE_CHOICE",
      "points": 2,
      "options": [
        {
          "text": "London",
          "correct": false
        },
        {
          "text": "Paris",
          "correct": true
        },
        {
          "text": "Rome",
          "correct": false
        }
      ]
    },
    {
      "text": "Define polymorphism",
      "type": "TEXT_ANSWER",
      "points": 5,
      "correctAnswer": "Polymorphism is the ability of an object to take many forms"
    }
  ]
}
```

**Response:**

```json
{
  "success": true,
  "message": "Quiz created successfully",
  "data": {
    "id": 1,
    "title": "Midterm Exam",
    "description": "Covers chapters 1-5",
    "startDate": "2023-04-01T09:00:00",
    "endDate": "2023-04-01T23:59:59",
    "durationMinutes": 60,
    "questions": [...]
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Get Available Quizzes

```
GET /api/quizzes/available?courseId=1
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
      "title": "Midterm Exam",
      "description": "Covers chapters 1-5",
      "startDate": "2023-04-01T09:00:00",
      "endDate": "2023-04-01T23:59:59",
      "durationMinutes": 60
    },
    ...
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Start Quiz (Student Only)

```
POST /api/quizzes/{quizId}/start
Authorization: Bearer STUDENT_TOKEN_HERE
```

**Response:**

```json
{
  "success": true,
  "message": "Quiz started successfully",
  "data": {
    "id": 1,
    "quiz": {...},
    "student": {...},
    "startTime": "2023-04-01T10:15:30",
    "completed": false
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Submit Quiz (Student Only)

```
POST /api/quizzes/{quizId}/submit
Authorization: Bearer STUDENT_TOKEN_HERE
```

**Request Body:**

```json
{
  "quizId": 1,
  "answers": [
    {
      "questionId": 1,
      "selectedOptionId": 2
    },
    {
      "questionId": 2,
      "textAnswer": "Polymorphism is the ability of an object to take many forms"
    }
  ]
}
```

**Response:**

```json
{
  "success": true,
  "message": "Quiz submitted successfully",
  "data": {
    "id": 1,
    "quiz": {...},
    "student": {...},
    "startTime": "2023-04-01T10:15:30",
    "endTime": "2023-04-01T11:10:45",
    "completed": true,
    "score": 7,
    "maxScore": 7
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## Assignments

### Create Assignment (Professor Only)

```
POST /api/assignments
Authorization: Bearer PROFESSOR_TOKEN_HERE
```

**Request Body:**

```json
{
  "title": "Final Project",
  "description": "Implement a web application using Spring Boot",
  "courseId": 1,
  "dueDate": "2023-05-15T23:59:59",
  "maxPoints": 100,
  "files": [
    {
      "fileName": "project_requirements.pdf",
      "fileUrl": "/uploads/project_requirements.pdf",
      "contentType": "application/pdf",
      "fileSize": 245789
    }
  ]
}
```

**Response:**

```json
{
  "success": true,
  "message": "Assignment created successfully",
  "data": {
    "id": 1,
    "title": "Final Project",
    "description": "Implement a web application using Spring Boot",
    "course": {...},
    "creator": {...},
    "dueDate": "2023-05-15T23:59:59",
    "createdAt": "2023-03-23T12:34:56.789",
    "maxPoints": 100,
    "files": [...]
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Get Active Assignments

```
GET /api/assignments/active?courseId=1
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
      "title": "Final Project",
      "description": "Implement a web application using Spring Boot",
      "dueDate": "2023-05-15T23:59:59",
      "maxPoints": 100
    },
    ...
  ],
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Submit Assignment (Student Only)

```
POST /api/assignments/{assignmentId}/submit
Authorization: Bearer STUDENT_TOKEN_HERE
```

**Request Body:**

```json
{
  "assignmentId": 1,
  "notes": "My final project submission",
  "files": [
    {
      "fileName": "FinalProject.zip",
      "fileUrl": "/uploads/FinalProject.zip",
      "contentType": "application/zip",
      "fileSize": 3456789
    }
  ]
}
```

**Response:**

```json
{
  "success": true,
  "message": "Assignment submitted successfully",
  "data": {
    "id": 1,
    "assignment": {...},
    "student": {...},
    "notes": "My final project submission",
    "submissionDate": "2023-05-14T20:45:12",
    "graded": false,
    "late": false,
    "files": [...]
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

### Grade Assignment Submission (Professor Only)

```
POST /api/assignments/submissions/{submissionId}/grade
Authorization: Bearer PROFESSOR_TOKEN_HERE
```

**Request Body:**

```json
{
  "score": 85,
  "feedback": "Good work, but missing proper documentation"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Submission graded successfully",
  "data": {
    "id": 1,
    "assignment": {...},
    "student": {...},
    "notes": "My final project submission",
    "submissionDate": "2023-05-14T20:45:12",
    "gradedDate": "2023-05-16T14:30:45",
    "score": 85,
    "feedback": "Good work, but missing proper documentation",
    "graded": true,
    "late": false,
    "files": [...]
  },
  "timestamp": "2023-03-23T12:34:56.789"
}
```

## File Upload

### Upload File

```
POST /api/upload
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

## Admin API Access

### Getting an Admin JWT Token

To access admin-protected endpoints, you need to log in with an administrator account:

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "admin",
    "fullName": "System Administrator",
    "email": "admin@college.edu"
  },
  "timestamp": "2023-03-31T15:30:45.123"
}
```

Use the returned token in the `Authorization` header of subsequent requests:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Managing Professor Requests

#### Get All Pending Professor Requests

```
GET /api/professor-requests
Authorization: Bearer {ADMIN_TOKEN}
```

Response:

```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 1,
      "fullName": "John Doe",
      "email": "john.doe@college.edu",
      "idImageUrl": "uploads/id-image-123.jpg",
      "department": "Computer Science",
      "additionalInfo": "PhD in Computer Science",
      "status": "PENDING",
      "requestDate": "2023-03-30T10:15:30",
      "reviewDate": null,
      "reviewedBy": null,
      "rejectionReason": null
    }
  ],
  "timestamp": "2023-03-31T15:45:30.123"
}
```

#### Review a Professor Request

```
POST /api/professor-requests/{requestId}/review
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "approved": true,
  "rejectionReason": null,
  "reviewedBy": "admin"
}
```

For rejection:

```json
{
  "approved": false,
  "rejectionReason": "Insufficient qualifications",
  "reviewedBy": "admin"
}
```

Response on approval:

```json
{
  "success": true,
  "message": "Professor account created successfully",
  "data": "Login credentials have been sent to the professor's email",
  "timestamp": "2023-03-31T15:50:20.456"
}
```

### Admin User Creation in Production

In a production environment, you should create an admin user securely using one of these methods:

1. Use a database migration script to create the admin user during deployment
2. Set up a secure bootstrap process that runs only on initial setup
3. Use environment variables to configure admin credentials on first startup

Example migration script approach:

```sql
-- To be run manually or through a secure migration process
INSERT INTO users (username, password, full_name, email, role, email_verified)
VALUES (
  'admin',
  '$2a$10$rJH7kLTJCWxbE8qFbI4Tz.CXNJTvP9CFSvY9aLXpLxUgFn72QJMBy', -- bcrypt hash for 'secure_password'
  'System Administrator',
  'admin@yourdomain.com',
  'ADMIN',
  true
);
```

Always use a strong password in production and follow security best practices.
