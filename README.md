# PLM Project - Product Lifecycle Management

A comprehensive **Product Lifecycle Management (PLM)** system built for the Odoo Hackathon.

## Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security, JWT Authentication
- **Database**: PostgreSQL
- **Build Tool**: Maven

## Features

- **User Management**: Role-based access (Admin, Engineering, Approver, Operations)
- **Product Management**: Create, version, and manage products
- **BOM Management**: Bills of Materials with components and operations
- **ECO Workflow**: Engineering Change Orders with multi-stage approval
- **Audit Trail**: Complete activity logging

## Project Structure

```
Backend/
└── plm-backend/
    └── src/main/java/com/odoo/plm/
        ├── controller/    # REST API endpoints
        ├── service/       # Business logic
        ├── repository/    # Database access
        ├── entity/        # JPA entities
        ├── dto/           # Request/Response objects
        ├── security/      # JWT authentication
        ├── config/        # Configuration
        └── enums/         # Constants
```

## Running the Application

```bash
cd Backend/plm-backend
mvn spring-boot:run
```

Server runs at: `http://localhost:8080`

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| POST /api/auth/signup | Register user |
| POST /api/auth/login | Get JWT token |
| GET /api/products | List products |
| POST /api/ecos | Create ECO |
| POST /api/ecos/{id}/approve | Approve ECO |

## License

MIT License
