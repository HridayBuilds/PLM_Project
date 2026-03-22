# Ecova - Engineering Change Management (ECM) System

A robust Product Lifecycle Management (PLM) solution focusing on Engineering Change Orders (ECO), Bill of Materials (BOM), and Product versioning.

## Features
- **Role-Based Access Control:** Separate dashboards and workflows for Admins, Engineers, and Approvers.
- **Product Management:** Track product versions, attributes, and file attachments.
- **Bill of Materials (BOM):** Multi-level component trees with routing operations (time tracking, cost centers).
- **ECO Workflow:** Submit proposed changes, track approvals through dynamic stages, and visualize impact via the Changes Comparison timeline.
- **Reporting:** Exportable matrix insights on global supply chain components.

## Technology Stack
- **Frontend:** React, Vite, Tailwind CSS, Lucide Icons
- **Backend:** Java, Spring Boot, Spring Security (JWT)
- **Database:** PostgreSQL via Hibernate/JPA

## Getting Started

### Prerequisites
- Node.js & npm
- Java 21+
- PostgreSQL

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd Backend/plm-backend
   ```
2. Create `src/main/resources/application.properties` with your database credentials and secret keys (this file is git-ignored for security).
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd Frontend/plm-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```

## Development
- Ensure code follows formatting guidelines.
- Database schemas are managed via Spring JPA auto DDL properties.
