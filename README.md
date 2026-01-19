# course-work.net
### Service Centric CourseWork

CycleNest is a service-oriented, RESTful middleware application designed to enable individuals to rent out unused items for short periods of time, encouraging a more sustainable, circular economy. The system is inspired by platforms such as Fat Llama and Media Dog Hire, and has been developed to meet the requirements of a Service-Oriented Architecture (SOA) coursework assessment.

This repository contains the complete implementation of the CycleNest Orchestrator, supporting cloud-based data storage, external API integration, Quality of Service (QoS) evaluation, and containerised deployment.

## System Overview

CycleNest follows a service-oriented design in which the Orchestrator acts as the central coordination point between clients, persistent storage, message queues, and an external geospatial API. Clients interact with the system exclusively via RESTful endpoints exposed by the Orchestrator.

The system supports:

* **Browsing and Searching:** Rentable items can be filtered by criteria such as category and price.
* **Proximity Search:** Users can filter items based on real-time driving distance from their location.
* **Rental Requests:** Users can submit requests to rent items.
* **Request Management:** Existing requests can be cancelled.

*Note: The application is not a full booking system. Requests are recorded with statuses, but no payments or confirmed bookings are processed.*

---

## Architecture

The CycleNest system is structured around the following loosely coupled components:

1. **Client:** REST client (Postman, curl, PowerShell, or text-based Java client).
2. **Orchestrator Service:** Java-based REST service deployed on Apache Tomcat.
3. **Cloud Database:** Azure Cosmos DB (NoSQL) for persistent storage.
4. **External API:** OSRM (Open Source Routing Machine) for distance/proximity calculations.
5. **Message Broker:** RabbitMQ for asynchronous handling of write-heavy operations.

All communication between components uses **JSON over HTTP** or **AMQP** (for RabbitMQ).

---

## Technologies Used

The following technologies and tools were used in the actual implementation of CycleNest:

* **Language:** Java (JDK 21)
* **REST Framework:** JAX-RS (Jersey)
* **Build Tool:** Apache Maven
* **IDE:** Apache NetBeans
* **Application Server:** Apache Tomcat 9
* **Database:** Azure Cosmos DB (NoSQL, Core (SQL) API, JSON documents)
* **External API:** OSRM Route Service (HTTP, JSON)
* **Messaging:** RabbitMQ (AMQP Protocol)
* **Testing:** Apache JMeter (QoS Analysis)
* **Containerisation:** Docker (Multi-stage build)

---

## Part A: Orchestrator Service

### Implementation Overview

The Orchestrator is implemented as a Java-based RESTful web service using JAX-RS (Jersey) and packaged as a WAR file. It serves as the interface to the cloud persistence layer. Persistence is handled via **Azure Cosmos DB**, which stores item and rental request data as JSON documents.

### Implemented Endpoints

| Endpoint | Method | Description |
| --- | --- | --- |
| `/api/items` | `GET` | Retrieve all available items (supports query filters). |
| `/api/requests` | `POST` | Create a rental request (status defaults to pending). |
| `/api/requests/{id}/cancel` | `PUT` | Cancel an existing request (status set to cancelled). |

### JSON Handling

* All request and response payloads are strictly JSON.
* Custom Java model classes (`Item`, `RentalRequest`) are used.
* Jackson handles serialization and deserialization automatically via annotations.

### Notes

* No booking confirmation or payment processing is implemented.
* The Orchestrator manages the request lifecycle states (pending, queued, cancelled).

---

## Part B: External API Integration

### OSRM Route Service

The Orchestrator integrates with the **OSRM Route API** to calculate the driving distance between a user-provided location and an item’s stored coordinates. This allows for "Proximity Search" functionality, filtering items that are within a 20km radius.

The Route service was selected because it provides:

* Realistic travel distance calculations (vs. straight-line distance).
* JSON responses suitable for REST consumption.

### Usage

1. User supplies latitude and longitude via query parameters.
2. Item coordinates are retrieved from Cosmos DB.
3. OSRM returns route distance in meters, which is converted to kilometers.

### Error Handling

The system gracefully handles:

* OSRM service downtime or HTTP errors.
* Network timeouts (via OkHttp configuration).
* Invalid or incomplete API responses.

In such cases, the Orchestrator returns meaningful HTTP status codes (e.g., 500 Internal Server Error) or excludes the specific item without crashing the entire search.

---

## Part C: QoS Analysis and Improvement

### QoS Testing Setup

Quality of Service (QoS) testing was performed using **Apache JMeter** to evaluate the scalability of the Orchestrator under concurrent load.
The test configuration consisted of:

* **50 Concurrent Users.**
* Requests executed against the Proximity Search (`GET`) and Rental Request (`POST`) endpoints.

### Test Results & Bottleneck Identification

* **Synchronous Search (GET):** The average response time reached **~60,068 ms (60 seconds)**. This severe latency confirmed a scalability bottleneck caused by blocking external API calls to OSRM.
* **Bottleneck:** Synchronous processing meant each thread waited for the external service, leading to thread pool saturation.

### Proposed and Implemented Improvement

To address blocking issues in the critical "Booking" path, an **Asynchronous Messaging** solution was implemented using **RabbitMQ**.

* **Improvement:** The `POST /requests` endpoint was refactored to offload write operations to a queue (`rental_requests_queue`).
* **Result:** Re-testing the asynchronous endpoint showed an average latency of **~26 ms**, a massive improvement over the synchronous approach.
* **Mechanism:** A separate `RabbitMQWorker` process consumes messages from the queue and handles the database writes in the background.

### Evidence Submitted

* Apache JMeter `.jmx` test plan files.
* Screenshots of Aggregate Reports (GET vs. POST comparison).
* Source code for `RabbitMQProducer` and `RabbitMQWorker`.

---

## Part D: Containerisation

### Docker Implementation

The Orchestrator is containerised using Docker to demonstrate cloud readiness and portability.

* A **Multi-Stage Dockerfile** is included at the root of the repository.
* **Stage 1:** Uses a Maven image to compile the source code and build the WAR file.
* **Stage 2:** Deploys the WAR file to a lightweight Tomcat 9 container.
* REST endpoints are exposed via port `8080`.

### Cloud Deployment

The Docker image is designed to run locally or on platforms like **Azure Container Instances (ACI)**. It supports environment variable configuration (e.g., `RABBITMQ_HOST`) to ensure connectivity in diverse environments.

---

## Data Model

Each item stored in the database includes the following attributes:

* `id` (String, UUID)
* `name` (String)
* `category` (String)
* `location` (String: "lat,lon")
* `dailyRate` (Double)
* `availability` (Boolean)

Toy JSON datasets have been used for testing, including specific items with coordinates to test the geospatial functionality.

---

## Setup and Deployment

### Local Deployment

1. Clone the repository.
2. Ensure **RabbitMQ** is running locally (Port 5672).
3. **Important:** Run the `RabbitMQWorker.java` file manually to start the background consumer.
4. Build the project using Maven:
```bash
mvn clean install

```


5. Deploy the generated WAR file to **Apache Tomcat 9**.
6. Access the service via `http://localhost:8080/CycleNestOrchestrator/api/items`.

### Docker Deployment

1. Build the Docker image:
```bash
docker build -t cyclenest-orchestrator .

```


2. Run the container (ensure it can see your host's RabbitMQ):
```bash
docker run -e RABBITMQ_HOST=host.docker.internal -p 8080:8080 cyclenest-orchestrator

```



---

## Testing

The project supports various testing methods:

* **Functional Testing:** Via PowerShell scripts (`Invoke-RestMethod`).
* **Queue Storage:** RabbitMQWorker.java is meant to be run manually.
* **Load Testing:** Via Apache JMeter (for Part C QoS analysis).
* **Integration Testing:** Verifying the full flow from API -> RabbitMQ -> Worker -> Cosmos DB.

---

## Limitations

* **Authentication:** Authentication and authorisation are not implemented.
* **Rate Limits:** The OSRM public API has rate limits which may cause the Proximity Search to fail under heavy load.
* **Worker orchestration:** The RabbitMQ Worker is a standalone Java application and is not automatically started by the Tomcat container.
