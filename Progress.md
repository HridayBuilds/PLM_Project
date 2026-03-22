# PLM Application - Progress Tracker

## Project Structure
```
/Odoo_Hackathon
├── Backend/plm-backend/          # Spring Boot backend (port 8080)
├── eco-backend/                  # Secondary backend (if any)
├── src/                          # React frontend (Vite + TypeScript)
│   ├── components/
│   ├── pages/
│   ├── services/
│   └── types/
└── Progress.md                   # This file
```

---

## Current Issues (From Screenshots)

### 1. Data Display Issues
- [ ] **Products Page**: Shows "No data found" but DB has 45 products
- [ ] **Bill of Materials Page**: Shows "No data found" but DB has data
- [ ] **Reports Page**: Shows "No data found"
- [ ] Dashboard shows correct counts (works fine)

### 2. UI/UX Layout Issues
- [ ] Profile, Light Theme Toggle, Notifications in top-right header
- [ ] Need to move these to bottom-left sidebar
- [ ] Profile should be standalone button at bottom-left
- [ ] Text getting cut off in some places
- [ ] Spacing issues

### 3. Stage Pipeline Issues
- [ ] NEW and DONE should be default, non-editable
- [ ] Cancelled stage is fine
- [ ] In-between stages need to be customizable
- [ ] Stages should be product/BOM specific, not general
- [ ] Need to assign approvers to each stage

### 4. Admin Flow Requirements
- [ ] Full CRUD on Products
- [ ] Full CRUD on BOMs
- [ ] Full CRUD on ECOs
- [ ] Settings management (ECO Stages, Approval Rules, User Management)
- [ ] Reports with full visibility

---

## Key Files Reference

### Frontend (src/)
| File | Purpose |
|------|---------|
| `src/pages/Products.tsx` | Products listing page |
| `src/pages/BillOfMaterials.tsx` | BOM listing page |
| `src/pages/ECOs.tsx` | ECO listing page |
| `src/pages/Reports.tsx` | Reports page |
| `src/pages/Settings.tsx` | Settings with ECO Stages |
| `src/pages/Dashboard.tsx` | Dashboard with stats |
| `src/components/Sidebar.tsx` | Left navigation |
| `src/components/Header.tsx` | Top header (has profile/theme) |
| `src/services/api.ts` | API service calls |
| `src/types/index.ts` | TypeScript types |

### Backend (Backend/plm-backend/)
| File | Purpose |
|------|---------|
| `controller/ProductController.java` | Product APIs |
| `controller/BomController.java` | BOM APIs |
| `controller/EcoController.java` | ECO APIs |
| `controller/UserController.java` | User APIs |
| `controller/AdminController.java` | Admin APIs |
| `service/ProductService.java` | Product business logic |
| `service/BomService.java` | BOM business logic |
| `service/EcoService.java` | ECO business logic |
| `dto/response/*Response.java` | Response DTOs |
| `entity/*.java` | JPA Entities |

---

## API Endpoints Reference

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### BOMs
- `GET /api/boms` - List all BOMs
- `GET /api/boms/{id}` - Get BOM by ID
- `POST /api/boms` - Create BOM
- `PUT /api/boms/{id}` - Update BOM
- `DELETE /api/boms/{id}` - Delete BOM

### ECOs
- `GET /api/ecos` - List all ECOs
- `GET /api/ecos/{id}` - Get ECO by ID
- `POST /api/ecos` - Create ECO
- `PUT /api/ecos/{id}` - Update ECO
- `DELETE /api/ecos/{id}` - Delete ECO

### Users/Admin
- `GET /api/users` - List users
- `GET /api/admin/pending-signups` - Pending user signups
- `POST /api/admin/approve/{id}` - Approve user signup

---

## Root Cause Analysis

### Backend Response Format (ACTUAL):
```java
// ProductController returns ProductListResponse directly (no ApiResponse wrapper)
return ResponseEntity.ok(productListResponse);
// Returns: { products: [...], totalElements: 45, totalPages: 3, ... }
```

### Frontend API Flow:
```javascript
// productApi.js
const response = await axiosInstance.get('/products');
return response.data;
// Returns: { products: [...], totalElements: 45, ... }

// ProductsPage.jsx
const response = await productApi.getProducts(); // This IS response.data
const productList = response?.products; // This should work!
```

### ISSUE FOUND:
The code looks correct, but possible issues:
1. Backend may not have data in DB
2. Backend service may be returning empty list
3. CORS or auth issues blocking the request

### NEXT STEPS:
1. Check if backend is running and returning data
2. Check browser network tab for actual response
3. Add console.log to debug actual response

---

## Fixes In Progress

### Fix #1: Products Page Data Display
**File**: `src/pages/Products.tsx`
**Issue**: Data extraction from API response
**Status**: [ ] Pending

### Fix #2: BOM Page Data Display
**File**: `src/pages/BillOfMaterials.tsx`
**Issue**: Data extraction from API response
**Status**: [ ] Pending

### Fix #3: Move Profile to Sidebar
**Files**: `src/components/Sidebar.tsx`, `src/components/Header.tsx`
**Issue**: Profile/theme/notifications in header, need to move to sidebar bottom
**Status**: [ ] Pending

### Fix #4: ECO Stages Configuration
**Files**: `src/pages/Settings.tsx`, related backend
**Issue**: Stages need to be product-specific with approvers
**Status**: [ ] Pending

---

## Database Info
- **Products**: 45 records
- **Users**: 15 records
- **ECOs**: 23 records
- **Pending Signups**: 2 records

---

## Next Steps (Priority Order)
1. Fix Products page data display
2. Fix BOM page data display
3. Fix Reports page
4. Move profile/theme to sidebar
5. Implement stage pipeline improvements
6. Add approver assignment to stages

---

## Completed Fixes

### 1. UI Layout - Profile moved to Sidebar (DONE)
- **Files Changed**: `Sidebar.jsx`, `Topbar.jsx`
- Theme toggle, notifications, and profile now in sidebar bottom
- Topbar simplified to just logo and menu button

### 2. Report API Methods Fixed (DONE)
- **File Changed**: `reportApi.js`
- Added missing methods: `getEcoReport`, `getProductVersionHistory`, `getBomChangeHistory`, `getArchivedProducts`, `getProductBomMatrix`

### 3. ECO Lazy Loading & API Fixes (DONE ✅)
- **Root Cause**: All entity relationships use `FetchType.LAZY`. `EcoService.mapToResponse()` accessed lazy-loaded fields inside try-catch blocks, silently swallowing exceptions and returning "Unknown" values.
- **Fix**: Added JOIN FETCH queries to `EcoRepository`, `EcoApprovalRepository`, `EcoBomChangeRepository` to eagerly load related entities. Removed all try-catch lazy loading workarounds.
- **Backend Files Changed**: `EcoService.java`, `EcoRepository.java`, `EcoApprovalRepository.java`, `EcoBomChangeRepository.java`, `EcoController.java`
- **Frontend Files Changed**: `EcoListPage.jsx`, `DashboardPage.jsx`, `ProductDetailPage.jsx`, `ecoApi.js`
- **New Endpoint**: `GET /api/ecos` — lists all ECOs with pagination (for general ECO listing)

---

## Backend Endpoints Missing (TODO)

### ReportController needs these endpoints:
```java
GET /api/reports/eco                           // ECO Report
GET /api/reports/products/{id}/versions        // Product Version History
GET /api/reports/products/{id}/bom-changes     // BOM Change History
GET /api/reports/products/archived             // Archived Products
GET /api/reports/product-bom-matrix            // Product-BOM Matrix
```

---

## Notes
- Backend runs on port 8080
- Frontend uses Vite + React + TypeScript
- API uses JWT authentication
- Admin role has full access
- Regular users have limited access
