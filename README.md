
The **Asynchronous Video & Media Transcoding Pipeline** is one of the single best projects you can build on your resume. It touches almost every core backend concept companies look for: **S3 storage, decoupling with message queues, background worker execution, state tracking, and security via presigned URLs.**

Here is your complete blueprint to understand and build this project step-by-step.

## 1. Project Explanation in Simple English (ELI5)

Imagine you have a giant 4K video file recorded on your phone. You want to post it on a website so your friends can watch it. But some friends have slow phone internet, and others are watching on a TV.

If you upload that giant video directly, the server will choke trying to play it for everyone.

This project is like a **smart digital factory for videos**:

1. You hand your raw video over to the front door (S3 bucket).
    
2. The front door drops a note into a physical inbox (SQS queue) saying: _"Hey, new video arrived!"_
    
3. A background worker (Python + FFmpeg) pulls the note out of the inbox, takes the raw video, and squishes it into 720p, 480p, and 360p versions, plus creates a cool thumbnail image.
    
4. When finished, it saves the new smaller videos back into the storehouse and updates a digital chalkboard (Database) saying: _"Video #101 is ready for viewing!"_
    

## 2. The Real-World Problem It Solves

If you try to upload a 1 GB video to YouTube or TikTok, you don't stay on a loading screen with your browser spinning for 20 minutes waiting for the video to encode into 10 different resolutions.

Video processing is **CPU-intensive and long-running**.

If a standard web server tried to process videos synchronously inside an HTTP request:

- **Timeouts:** Web browsers and HTTP clients time out after 30 to 60 seconds.
    
- **Server Crashes:** A few simultaneous video uploads would consume 100% CPU and RAM, bringing down the whole website for all users.
    
- **Bad UX:** Users cannot navigate away or do anything else while waiting.
    

## 3. Synchronous vs. Asynchronous Processing

To understand why asynchronous processing is crucial, let's look at an analogy.

- **Synchronous (The Fast-Food Counter Nightmare):** You order a custom cake at a bakery counter. The clerk tells you to stand right there at the register while they go to the back, bake the cake from scratch for 2 hours, and hand it to you. Nobody behind you in line can order until your cake is finished.
    
- **Asynchronous (The Buzzer System):** You order the cake. The clerk hands you a receipt with an Order ID and a buzzer, then immediately calls the next customer. A baker in the back receives the order slip, bakes the cake over 2 hours, and buzzes you when it's done.
    

```
Synchronous Request Flow (BAD for heavy tasks):
[Client] ─── HTTP POST /upload ───> [API Server (Sits locked for 10 mins processing)] ─── HTTP 200 ───> [Client]

Asynchronous Request Flow (GOOD):
[Client] ─── HTTP POST /upload ───> [API Server] ─── HTTP 202 Accepted (Instant) ───> [Client]
                                         │
                                 (Dispatches job to queue)
                                         │
                                         ▼
                               [Background Worker] ─── (Processes in background)
```

In our architecture, **Spring Boot handles quick HTTP metadata and auth requests (under 100ms)**, while **Python background workers handle heavy processing tasks (minutes)**.

## 4. Architecture Diagram

```
                 +--------------------------------------------------------+
                 |                       CLIENT                           |
                 |             (React / Mobile / Postman)                 |
                 +-------------------+----------------+-------------------+
                                     |                ^
                      1. Auth & Get  |                | 6. Get Video /
                      Presigned URL  |                |    Download Links
                                     v                |
                        +----------------------------------+
                        |        SPRING BOOT API           |
                        |          (EC2 / Docker)          |
                        +-----------------+----------------+
                                          |
                      2. Direct Upload    | 3. Save Upload
                      to S3 via URL       |    Metadata (PENDING)
                                          v
      +-----------------------------------+-----------------------------------+
      |                                AWS CLOUD                              |
      |                                                                       |
      |   +------------------+                   +------------------------+   |
      |   |    AMAZON S3     |                   |  POSTGRESQL DATABASE   |   |
      |   |  (Storage Bucket)|                   |      (RDS / EC2)        |   |
      |   +--------+---------+                   +-----------+------------+   |
      |            |                                         ^                |
      |            | S3 Event Notification                   | 5. Update      |
      |            v                                         |    Status to   |
      |   +------------------+                   +-----------+------------+   |
      |   |    AMAZON SQS    | 4. Poll & Process |     PYTHON WORKER      |   |
      |   | (Queue + DLQ)    |------------------>|    (EC2 / Docker)      |   |
      |   +------------------+                   |  (FFmpeg Transcoder)   |   |
      |                                          +------------------------+   |
      +-----------------------------------------------------------------------+
```

## 5. Complete Step-by-Step Workflow

1. **User Authentication & Presigned URL Request:**
    
    - User logs in via Spring Boot and gets a JWT token.
        
    - User requests to upload `my_vacation.mp4`.
        
    - Spring Boot creates a record in PostgreSQL with status `PENDING` and asks Amazon S3 for a **Presigned Upload URL**. Spring Boot returns this URL to the client.
        
2. **Direct Upload to S3:**
    
    - The Client uploads the raw video file directly from the browser/mobile app to Amazon S3 using the Presigned URL. _Note: The video payload NEVER touches the Spring Boot backend server!_
        
3. **Queue Notification:**
    
    - Amazon S3 triggers an event upon upload completion and automatically pushes a message containing the file location details into an **Amazon SQS queue**.
        
4. **Worker Execution:**
    
    - The **Python Worker** continuously polls SQS for new messages.
        
    - Upon receiving a message, the worker updates the video status in PostgreSQL to `PROCESSING`.
        
    - It downloads the raw video from S3 to a temporary workspace directory on its disk.
        
    - It executes **FFmpeg** commands to generate multiple output resolutions (e.g., `720p`, `480p`) and a thumbnail image (`thumbnail.jpg`).
        
5. **Completion & DB Update:**
    
    - The Python worker uploads all newly processed video formats and thumbnail files back to the output folder in S3.
        
    - It updates the PostgreSQL record status to `COMPLETED` along with file paths to all converted versions.
        
    - It deletes the message from SQS so it won't be processed again.
        
6. **User Download/Stream:**
    
    - The user queries `GET /videos/{id}` on Spring Boot.
        
    - Spring Boot checks PostgreSQL, sees `COMPLETED`, generates short-lived S3 Presigned Download URLs for the processed videos, and returns them to the user.
        

## 6. AWS Services Used

### 1. Amazon S3 (Simple Storage Service)

- **What it does:** Object storage for storing raw uploaded videos and processed media files.
    
- **Why it is used:** Highly scalable, durable (99.999999999%), and designed for large files.
    
- **Why chosen:** Storing videos on local server disks fills up memory instantly and breaks horizontal scaling.
    
- **Free Tier:** Yes (5 GB standard storage, 20,000 GET Requests, 2,000 PUT Requests per month for 12 months).
    
- **If removed:** You would have to save files directly onto the EC2 instance disk, which will quickly run out of space and cause server crashes.
    
- **Simpler alternative:** None for cloud storage. Local folder on disk (only acceptable during local development).
    

### 2. Amazon SQS (Simple Queue Service)

- **What it does:** A managed message queuing service that decouples S3/Spring Boot from the Python background worker.
    
- **Why it is used:** Holds upload notifications safely until the background worker is ready to process them.
    
- **Why chosen:** Very easy to integrate, virtually free, and eliminates the need to host and maintain a message broker like RabbitMQ.
    
- **Free Tier:** Yes (1 Million requests per month forever free).
    
- **If removed:** The backend would have to trigger processing synchronously or via fragile direct HTTP calls to workers, leading to dropped tasks if workers reboot or become overwhelmed.
    
- **Simpler alternative:** RabbitMQ or Redis Pub/Sub hosted on EC2 (adds operational complexity).
    

### 3. Amazon EC2 (Elastic Compute Cloud)

- **What it does:** Virtual virtual private server in the cloud where your Docker containers run.
    
- **Why it is used:** Hosts the Spring Boot API, Python Worker, and PostgreSQL database (or you can use RDS).
    
- **Why chosen:** Standard, flexible compute instance that gives full control over the environment.
    
- **Free Tier:** Yes (`t2.micro` / `t3.micro` instance with 750 hours per month for 12 months).
    
- **If removed:** You wouldn't have a cloud server to run your code!
    
- **Simpler alternative:** Render, Railway, or Heroku (simpler to deploy, but less educational for learning AWS hands-on).
    

### 4. Amazon CloudWatch

- **What it does:** Centralized logging and monitoring tool.
    
- **Why it is used:** Captures logs from Spring Boot, Python, and S3 event metrics so you can debug errors in production.
    
- **Why chosen:** Pre-integrated with all AWS components automatically.
    
- **Free Tier:** Yes (5 GB of log data ingestion and 10 custom metrics forever free).
    
- **If removed:** You would have to SSH into EC2 instances manually and tail log files using Linux commands.
    
- **Simpler alternative:** Local text file logs managed with standard tools like `Logback`.
    

### 5. AWS IAM (Identity and Access Management)

- **What it does:** Secures access controls and access keys across AWS components.
    
- **Why it is used:** Ensures your EC2 instances have minimal rights—e.g., Python worker can only read/write to specific S3 buckets and pull from SQS.
    
- **Why chosen:** Non-negotiable security layer required by AWS.
    
- **Free Tier:** Always 100% Free.
    
- **If removed:** Impossible—AWS mandates IAM for access authorization.
    

## 7. Component Deep-Dive

- **Client:** A web front-end (React), mobile app, or Postman collection. Responsible for initiating auth requests, directly uploading media to S3 via presigned URLs, and rendering video player links.
    
- **Spring Boot Backend:** The orchestrator API. Handles user registration/login, JWT validation, generating S3 Presigned URLs, writing initial metadata records to the database, and serving status update queries to clients.
    
- **Python Worker:** An independent worker process written in Python (using libraries like `boto3`). It continuously polls SQS, downloads media, invokes the system shell to execute `ffmpeg`, uploads processed output to S3, and updates PostgreSQL.
    
- **Amazon S3:** Object store divided logically into two folders/buckets: `/raw` for incoming unprocessed uploads, and `/processed` for transcoded outputs and thumbnails.
    
- **Amazon SQS:** Standard queue holding JSON messages containing details about newly uploaded files. Includes a Dead-Letter Queue (DLQ) for corrupted or unprocessable files.
    
- **EC2 Instance:** A single `t3.micro` (or `t2.small`) instance running Ubuntu, hosting Docker Engine. Uses `docker-compose` to run Spring Boot, Python Worker, and PostgreSQL containers seamlessly.
    
- **PostgreSQL:** The relational database holding tables for `users` and `videos` to maintain application state, job statuses, and file locations.
    
- **FFmpeg:** A powerful CLI audio/video processing tool installed inside the Python worker's Docker container. Converts `.mp4`/`.mov` files to different bitrates, codecs, and dimensions.
    
- **CloudWatch:** Captures container stdout logs and system health metrics.
    
- **IAM Roles:** An IAM Role attached directly to the EC2 instance so your code does not need hardcoded AWS secret keys inside `.env` files or code repositories.
    

_(Note: AWS Step Functions, AWS Lambda, and SNS are intentionally excluded from this MVP architecture to avoid overengineering. SQS + Python Worker is more than sufficient.)_

## 8. Communication Protocols Between Components

```
Client ───(HTTP REST / JSON)───> Spring Boot API
Client ───(HTTP PUT / Binary)───> S3 Bucket (Direct Upload)
S3 Bucket ───(AWS Event Notification)───> SQS Queue
Python Worker ───(Long Polling via Boto3 SDK)───> SQS Queue
Python Worker ───(HTTP GET/PUT via Boto3 SDK)───> S3 Bucket
Python Worker ───(SQL via psycopg2 / SQLAlchemy)───> PostgreSQL Database
Spring Boot API ───(SQL via Spring Data JPA)───> PostgreSQL Database
```

## 9. Key Concepts Explained Simply

### Background Workers

- **Real-life analogy:** A restaurant kitchen chef. The front-of-house waiter (API) takes orders from guests and places them on a metal rail in the kitchen. The chef (worker) picks up orders off the rail one by one and cooks them in the back without blocking the waiter from taking more orders.
    
- **Technical explanation:** An isolated thread or process running asynchronously from the main API thread. It executes time-consuming computational tasks without blocking HTTP request execution cycles.
    

### Queues

- **Real-life analogy:** A line of people waiting for a bank teller. First come, first served.
    
- **Technical explanation:** A First-In, First-Out (FIFO) or standard message buffer that holds task data sent by a producer until a consumer picks it up for processing.
    

### Retries

- **Real-life analogy:** Redialing a phone number when you get a busy signal instead of giving up immediately.
    
- **Technical explanation:** Automatically re-executing a failed action (like downloading an S3 file during network fluctuation) using exponential backoff before marking the job as failed.
    

### Dead-Letter Queue (DLQ)

- **Real-life analogy:** The "Lost & Found" bin at a school.
    
- **Technical explanation:** A specialized secondary SQS queue where messages are automatically moved after failing a maximum number of processing attempts (e.g., 3 retries). This keeps broken messages (like a corrupt 0-byte file) from infinitely blocking workers.
    

### Idempotency

- **Real-life analogy:** An elevator call button. Pressing it once turns on the light. Pressing it 10 more times does not bring 10 different elevators—it produces the exact same result as pressing it once.
    
- **Technical explanation:** Designing an operation such that performing it multiple times with the same input produces the exact same state output without duplicate side-effects. (e.g., if a worker receives the same video task twice, it overwrites the processed file rather than creating duplicate database records).
    

### State Machines

- **Real-life analogy:** A traffic light. It transitions predictably from GREEN → YELLOW → RED. It can never jump from GREEN directly to RED without going through YELLOW.
    
- **Technical explanation:** A behavioral model that tracks an entity's status (`PENDING` → `PROCESSING` → `COMPLETED` or `FAILED`), ensuring state updates follow valid business rules.
    

## 10. Database Design

We keep the database simple and relational with two core entities:

```
+------------------------------------+          +------------------------------------+
|               USERS                |          |               VIDEOS               |
+------------------------------------+          +------------------------------------+
| id (PK, UUID)                      |          | id (PK, UUID)                      |
| email (VARCHAR, UNIQUE)            |1        *| user_id (FK -> Users.id)           |
| password_hash (VARCHAR)            |----------| original_filename (VARCHAR)       |
| created_at (TIMESTAMP)             |          | s3_raw_key (VARCHAR)               |
+------------------------------------+          | status (ENUM: PENDING,             |
                                                |   PROCESSING, COMPLETED, FAILED)   |
                                                | error_message (VARCHAR, NULLABLE)  |
                                                | s3_720p_key (VARCHAR, NULLABLE)    |
                                                | s3_480p_key (VARCHAR, NULLABLE)    |
                                                | s3_thumbnail_key (VARCHAR, NULL)   |
                                                | created_at (TIMESTAMP)             |
                                                | updated_at (TIMESTAMP)             |
                                                +------------------------------------+
```

### Table Descriptions & Relationships

- **`users` Table:** Stores user account details and credentials.
    
- **`videos` Table:** Tracks the processing state, metadata, and storage locations of each media file.
    
- **Relationship:** One-to-Many (`users` to `videos`). One user can upload multiple videos.
    
- **Crucial Field - `status`:** Managed as a strict state transition string:
    
    1. `PENDING`: Initial state created by Spring Boot when presigned URL is granted.
        
    2. `PROCESSING`: Set by Python worker as soon as it picks up the message from SQS.
        
    3. `COMPLETED`: Set by Python worker after FFmpeg finishes and output files are safe in S3.
        
    4. `FAILED`: Set if processing crashes or max retries are exceeded.
        

## 11. Authentication & Security

### 1. JWT (JSON Web Tokens)

- Users log in via `/api/v1/auth/login` with email and password.
    
- Spring Boot validates credentials and returns a signed JWT token.
    
- Client attaches this JWT in the `Authorization: Bearer <TOKEN>` header for all subsequent API calls.
    

### 2. S3 Presigned URLs (Crucial Security & Architecture Pattern)

To avoid uploading massive video files through our Spring Boot server, we use **Presigned URLs**.

- Spring Boot uses the AWS SDK to generate a temporary, cryptographically signed S3 upload URL valid for only 15 minutes.
    
- **Security Benefit:** The S3 bucket remains 100% private. Nobody can access or upload files without explicitly requesting a short-lived presigned token from our authenticated Spring Boot API first.
    

```
Client                     Spring Boot API                      Amazon S3
  │                               │                                 │
  ├─ 1. POST /videos/presigned ──>│                                 │
  │     (with JWT token)          ├─ 2. Generate Presigned URL ────>│
  │                               │    (Valid for 15 mins)          │
  │<─ 3. Return Presigned URL ────┤                                 │
  │                                                                 │
  └─ 4. PUT /raw-video.mp4 (Direct binary payload upload) ─────────>│
```

### 3. IAM Roles & Least Privilege

- Never store AWS access key/secret key pairs in code or configuration files on the EC2 instance.
    
- Attach an **IAM Role** directly to the EC2 instance.
    
- Give this IAM Role _only_ permissions to `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `s3:GetObject`, and `s3:PutObject` for your specific bucket.
    

### 4. Basic Encryption

- **In-Transit:** All client-to-API and client-to-S3 communications happen strictly over HTTPS (TLS).
    
- **At-Rest:** Enable S3 Server-Side Encryption (SSE-S3) with a single click in AWS console (encrypts video files automatically on disk using AES-256).
    

## 12. Handling Concurrency (Multiple Uploads at Once)

How does this setup handle 20 users uploading videos at the same time without crashing?

1. **Upload Phase:** All 20 users upload directly to Amazon S3 simultaneously. Amazon S3 handles massive scaling automatically. Your EC2 instance uses virtually zero CPU or memory during this phase!
    
2. **Queueing Phase:** S3 sends 20 messages into Amazon SQS. SQS effortlessly buffers these messages.
    
3. **Processing Phase:** Your Python worker pulls messages off SQS one by one (or in small batches of 2-3 using worker threads).
    
    - If processing one video takes 1 minute, 20 videos will be processed sequentially over 20 minutes.
        
    - **The API remains lightning fast and responsive**, and the server never crashes due to CPU overload!
        

## 13. Future Enhancements (Scaling to Thousands of Users)

If this project grows into a real startup, here are the modular upgrades to add:

- **Amazon EventBridge:** Replace standard S3 SQS event triggers with an EventBridge event bus to route media events to multiple subscribers (e.g., trigger transcoding + trigger AI content moderation simultaneously).
    
- **Multiple / Auto-Scaling Worker Nodes:** Move Python workers into **AWS ECS (Elastic Container Service)** with an Auto-Scaling Group based on SQS queue depth (e.g., if queue length > 50 messages, spin up 3 additional worker containers automatically).
    
- **Amazon ElastiCache (Redis):** Cache user profiles, JWT blocklists, and video processing progress metrics in memory for ultra-low latency reads.
    
- **Amazon CloudFront (CDN):** Put a Content Delivery Network in front of S3 so users globally stream/download processed videos from edge locations near them with zero buffering.
    
- **Advanced Monitoring (AWS X-Ray / Prometheus + Grafana):** Track end-to-end trace latency from the exact millisecond a user clicks upload to when the final thumbnail is rendered.
    

## 14. AWS Free Tier Cost Estimate

As a student, you can build and run this project for **$0 / month** using the AWS Free Tier.

|**Service**|**Free Tier Allowance**|**Project Usage**|**Cost Impact**|
|---|---|---|---|
|**Amazon S3**|5 GB Storage, 2,000 PUTs|~2-3 GB testing video storage|**$0.00**|
|**Amazon SQS**|1,000,000 Requests/month|~1,000 requests during development|**$0.00**|
|**Amazon EC2**|750 Hours/month (`t2.micro`/`t3.micro`)|1 instance running Docker 24/7 (720 hrs)|**$0.00**|
|**CloudWatch**|5 GB Log Data|~100 MB testing logs|**$0.00**|
|**IAM**|Always Free|Security configuration|**$0.00**|

### What Could Incur Costs Eventually?

- **Leaving raw video files in S3:** If you store 50 GB of 4K test videos, you will exceed the 5 GB free limit (costs ~$0.023 per GB/month after that). _Tip: Set up an S3 Lifecycle rule to delete raw uploads after 3 days!_
    
- **EC2 Instance Size:** Upgrading to a large EC2 instance (like `c5.xlarge` for super fast video encoding) will charge ~$0.17/hour. Stick to `t3.micro` or `t3.small` for testing.
