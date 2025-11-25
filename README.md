# ⚡ Energy Management System (EMS)

A distributed microservices-based platform for monitoring and managing energy consumption across smart devices. Built with Spring Boot, React, and orchestrated with Docker.

![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![React](https://img.shields.io/badge/React-19-61DAFB)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Service Details](#-service-details)
- [API Documentation](#-api-documentation)
- [Message Queue Events](#-message-queue-events)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)
- [Development](#-development)

## 🎯 Overview

The Energy Management System provides a comprehensive solution for:
- **User Management**: Role-based access control (Admin/User)
- **Device Management**: Register and assign smart devices to users
- **Real-time Monitoring**: Track energy consumption via message queues
- **Analytics**: Visualize hourly consumption statistics with interactive charts

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Traefik Gateway (:81)                        │
│                    (Routing + Forward Authentication)                │
└─────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Frontend    │         │     Auth      │         │    User       │
│    (React)    │         │   Service     │         │   Service     │
│    :80        │         │    :8083      │         │    :8081      │
└───────────────┘         └───────┬───────┘         └───────┬───────┘
                                  │                         │
                                  │    ┌────────────────────┤
                                  │    │                    │
                                  ▼    ▼                    ▼
                          ┌─────────────────┐       ┌───────────────┐
                          │    RabbitMQ     │       │    Device     │
                          │  (CloudAMQP)    │       │   Service     │
                          │                 │       │    :8082      │
                          └────────┬────────┘       └───────┬───────┘
                                   │                        │
                                   ▼                        │
                          ┌───────────────┐                 │
                          │  Monitoring   │◄────────────────┘
                          │   Service     │
                          │    :8084      │
                          └───────────────┘
```

### Data Flow

1. **User Registration**: AuthService → RabbitMQ → UserService & DeviceService
2. **Device Creation**: DeviceService → RabbitMQ → MonitoringService
3. **Consumption Data**: Python Simulator → RabbitMQ → MonitoringService

## ✨ Features

### Admin Dashboard
- ✅ Full CRUD operations for users
- ✅ Full CRUD operations for devices
- ✅ Assign devices to any user
- ✅ View all system devices and users
- ✅ Access consumption statistics for any device

### User Dashboard
- ✅ View assigned devices
- ✅ Access personal device statistics
- ✅ Hourly consumption charts
- ✅ Historical data visualization

### Security
- 🔐 JWT-based authentication
- 🔐 Role-based access control (RBAC)
- 🔐 Forward authentication via Traefik
- 🔐 Policy-based route protection

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 19, TypeScript, Vite, React Router |
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL (4 instances) |
| **Message Broker** | RabbitMQ (CloudAMQP) |
| **API Gateway** | Traefik v3 |
| **Containerization** | Docker, Docker Compose |
| **Build Tools** | Maven 3.9, npm |

## 📦 Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop) (or Docker Engine + Compose)
- [Git](https://git-scm.com/)
- (Optional) [Python 3.x](https://www.python.org/) - for the data simulator

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd energy-management-system
```

### 2. Create Docker Network

```bash
docker network create tema1_net
```

### 3. Start All Services

```bash
docker-compose up --build
```

### 4. Access the Application

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:81 |
| **Traefik Dashboard** | http://localhost:8081 |
| **Auth Service (direct)** | http://localhost:8093 |
| **User Service (direct)** | http://localhost:8091 |
| **Device Service (direct)** | http://localhost:8092 |
| **Monitoring Service (direct)** | http://localhost:8094 |

### 5. Default Credentials

```
Username: admin
Password: admin
Role: ADMIN
```

## 📡 Service Details

### Authentication Service (Port 8083)

Handles user authentication, JWT token management, and authorization policies.

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/auth/register` | POST | Register new user | Public |
| `/api/auth/login` | POST | Authenticate user | Public |
| `/api/auth/register-admin` | POST | Create user (admin) | Admin |
| `/api/auth` | GET | List all users | Admin |
| `/api/auth/{id}` | DELETE | Delete user | Admin |
| `/validate` | * | Forward auth endpoint | Internal |

### User Service (Port 8081)

Manages user profile information (synced via RabbitMQ).

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/users` | GET | List all user profiles | Admin |
| `/api/users` | POST | Create user profile | Admin |
| `/api/users/{id}` | PUT | Update user profile | Admin |
| `/api/users/{id}` | DELETE | Delete user profile | Admin |

### Device Service (Port 8082)

Manages smart devices and their assignments.

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/devices` | GET | List all devices | Admin |
| `/api/devices` | POST | Create new device | Admin |
| `/api/devices/{id}` | GET | Get user's devices | User/Admin |
| `/api/devices/{id}` | PUT | Update device | Admin |
| `/api/devices/{id}` | DELETE | Delete device | Admin |

### Monitoring Service (Port 8084)

Processes and stores consumption data from devices.

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/monitoring` | GET | Get consumption stats | User/Admin |
| `/api/monitoring/device` | POST | Register device | Admin |
| `/api/monitoring/device/{id}` | DELETE | Remove device | Admin |

**Query Parameters for GET `/api/monitoring`:**
- `deviceId` (required): Device ID
- `day` (required): Date in `YYYY-MM-DD` format

## 📨 Message Queue Events

The system uses RabbitMQ with a topic exchange (`energy_sync_exchange`) for inter-service communication.

| Routing Key | Publisher | Subscribers | Payload |
|-------------|-----------|-------------|---------|
| `user.insert` | AuthService | UserService, DeviceService | `{id, firstName, lastName, email, address}` |
| `user.delete` | AuthService | UserService, DeviceService | `{id}` |
| `device.insert` | DeviceService | MonitoringService | `{id}` |
| `device.delete` | DeviceService | MonitoringService | `{id}` |

### Device Measurements Queue

The `device_measurements` queue receives consumption data:

```json
{
  "timestamp": "2024-01-15T14:30:00",
  "device": { "id": 1 },
  "consumption": 0.15
}
```

## 📁 Project Structure

```
energy-management-system/
├── AuthenticationService/     # JWT auth & policy management
│   └── src/main/java/...
├── UserService/               # User profile management
│   └── src/main/java/...
├── DeviceService/             # Device CRUD operations
│   └── src/main/java/...
├── MonitoringDevice/          # Consumption data processing
│   └── src/main/java/...
├── ems-frontend/              # React TypeScript frontend
│   ├── src/
│   │   ├── components/
│   │   ├── context/           # Auth context
│   │   ├── pages/             # Dashboard, Statistics, etc.
│   │   ├── lib/               # API client, JWT utils
│   │   └── types/             # TypeScript definitions
│   └── Dockerfile
├── DeviceDataSimulator/       # Python consumption simulator
│   └── script.py
├── dynamic/
│   └── path.yml               # Traefik routing config
├── docker-compose.yml
├── traefik.yml
└── README.md
```

## ⚙️ Configuration

### Environment Variables

Each service supports the following environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_IP` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_USER` | postgres | Database username |
| `DB_PASSWORD` | root | Database password |
| `DB_DBNAME` | varies | Database name |
| `PORT` | varies | Service port |

### Frontend Configuration

Create `.env` file in `ems-frontend/`:

```env
VITE_API_BASE=http://localhost:81
```

## 🔧 Development

### Running Services Locally

Each service can be run independently for development:

```bash
# Backend services (requires local PostgreSQL)
cd AuthenticationService
./mvnw spring-boot:run

# Frontend
cd ems-frontend
npm install
npm run dev
```

### Running the Data Simulator

```bash
cd DeviceDataSimulator
python -m venv .venv
source .venv/bin/activate  # or .venv\Scripts\activate on Windows
pip install pika
python script.py
```

The simulator will prompt for:
- Date (YYYY-MM-DD format)
- Device ID

It generates realistic consumption patterns based on time of day.

### Stopping the System

```bash
docker-compose down

# To also remove volumes (databases):
docker-compose down -v
```

## 🔒 Security Notes

- JWT tokens expire after 24 hours
- Passwords are hashed with BCrypt
- All protected routes require valid JWT in `app-auth` header
- Forward authentication validates every request through AuthService

## 📝 API Testing

Swagger UI is available for each service:
- Auth: http://localhost:8093/swagger-ui.html
- User: http://localhost:8091/swagger-ui.html
- Device: http://localhost:8092/swagger-ui.html
- Monitoring: http://localhost:8094/swagger-ui.html

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

---

**Built with ❤️ for energy-conscious applications**