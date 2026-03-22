# PLM Backend/Frontend Fixes Progress

## Issues Identified

### Backend Issues (CRITICAL)

| Issue | File | Status |
|-------|------|--------|
| Transaction rollback in getFinalStage() | EcoStageService.java:207-214 | PENDING |
| Missing null checks in mapApprovalToResponse() | EcoService.java:770-781 | PENDING |
| Lazy loading outside try-catch in mapBomChangeToResponse() | EcoService.java:740-759 | PENDING |
| Missing null checks in buildComparisonResponse() | EcoService.java:783-823 | PENDING |
| Lazy loading in ProductService.mapToResponse() | ProductService.java:360-361 | PENDING |

### Entity Lazy Loading Configuration
- Eco.java: product, bom, createdBy, currentStage (all LAZY)
- EcoApproval.java: eco, approverUser, stage (all LAZY)
- EcoBomChange.java: bomComponent, newComponentProduct (all LAZY)
- BomComponent.java: bom, componentProduct (all LAZY)

### Frontend Issues
| Issue | File | Status |
|-------|------|--------|
| Products not loading in ECO create | EcoCreatePage.jsx | PENDING |
| ECOs not displaying in list | EcoListPage.jsx | PENDING |
| Dashboard "something went wrong" | DashboardPage.jsx | PENDING |

## Fixes Applied

### Round 1 - Previous Session
- Fixed UserController.updateUserRole (@RequestParam to @RequestBody)
- Added /api/users/approvers endpoint
- Added /api/ecos/stages endpoint for all authenticated users
- Added OPERATIONS_USER to ReportController permissions
- Fixed DashboardPage role dropdown values
- Removed mock data with fake UUIDs
- Added approver selection to SettingsPage stage modal
- Fixed BomListPage create button permission
- Added OPERATIONS_USER to BOM routes

### Round 2 - Current Session (COMPLETED ✅)
- [x] Fix EcoService.mapToResponse — replaced lazy loading try-catch with JOIN FETCH queries
- [x] Fix EcoService.mapBomChangeToResponse — uses `findByEcoIdWithDetails` (JOIN FETCH)
- [x] Fix EcoService.mapApprovalToResponse — uses `findByEcoIdWithDetails` (JOIN FETCH)
- [x] Fix EcoService.buildComparisonResponse — uses `findByEcoIdWithDetails` (JOIN FETCH)
- [x] Added JOIN FETCH queries to EcoRepository (findByIdWithDetails, findByCreatedByIdWithDetails, findPendingForApproverWithDetails, searchEcosWithDetails, findAllWithDetails)
- [x] Added JOIN FETCH query to EcoApprovalRepository (findByEcoIdWithDetails)
- [x] Added JOIN FETCH query to EcoBomChangeRepository (findByEcoIdWithDetails)
- [x] Added `GET /api/ecos` endpoint to EcoController + `getAllEcos()` in EcoService
- [x] Fixed EcoListPage.jsx to use `getAllEcos` API
- [x] Fixed DashboardPage.jsx column keys: type→ecoType, stage→currentStageName, createdBy→createdByName
- [x] Fixed ProductDetailPage.jsx — removed hardcoded mock data fallback
- [x] Added `getAllEcos()` to ecoApi.js

