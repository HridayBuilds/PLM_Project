# PLM Frontend — Product Requirements Document
**Project:** PLM Engineering Change Order System  
**Stack:** React + Vite, Tailwind CSS, Axios, ShadCN UI, React Router DOM, React Icons, React Toastify  
**Folder:** `/Frontend/plm-frontend`  
**Theme:** Dark-first (light toggle to be wired later — stubs only)  
**Design Language:** Industrial/utilitarian — bold, sharp, boxy with surgical rounded corners. Heavy use of tables, pagination, sidebar-driven navigation.

---

## 1. Design System & Tokens

### 1.1 Color Palette (Dark Theme — CSS Variables in `index.css`)

```css
:root {
  /* Backgrounds */
  --bg-base:        #0D0D0F;   /* deepest page bg */
  --bg-surface:     #141417;   /* cards, panels */
  --bg-elevated:    #1C1C21;   /* table rows, inputs */
  --bg-border:      #2A2A32;   /* borders, dividers */

  /* Brand / Accent */
  --accent:         #6C63FF;   /* primary purple (matches reference image) */
  --accent-hover:   #7D75FF;
  --accent-dim:     rgba(108, 99, 255, 0.15);

  /* Status Colors */
  --green:          #22C55E;
  --green-dim:      rgba(34, 197, 94, 0.12);
  --red:            #EF4444;
  --red-dim:        rgba(239, 68, 68, 0.12);
  --yellow:         #F59E0B;
  --yellow-dim:     rgba(245, 158, 11, 0.12);
  --blue:           #3B82F6;
  --blue-dim:       rgba(59, 130, 246, 0.12);

  /* ECO Stage Colors */
  --stage-new:      #3B82F6;
  --stage-approval: #F59E0B;
  --stage-done:     #22C55E;

  /* Text */
  --text-primary:   #F0F0F5;
  --text-secondary: #9090A8;
  --text-muted:     #55556A;

  /* Sidebar */
  --sidebar-width:  240px;
  --sidebar-bg:     #101013;
  --sidebar-border: #1E1E26;
}
```

### 1.2 Typography

Use Google Fonts:
- **Display / Headings:** `"DM Sans"` — weight 600, 700
- **Body / UI:** `"IBM Plex Sans"` — weight 400, 500
- **Monospace (versions, IDs):** `"IBM Plex Mono"` — weight 400, 500

```html
<!-- In index.html -->
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;600;700&family=IBM+Plex+Sans:wght@400;500&family=IBM+Plex+Mono:wght@400;500&display=swap" rel="stylesheet">
```

### 1.3 Spacing & Shape

- Border radius: `4px` for sharp elements (table rows, badges), `8px` for cards/panels, `12px` for modals
- Consistent 1px border: `border: 1px solid var(--bg-border)`
- Box shadows: `0 0 0 1px var(--bg-border), 0 4px 24px rgba(0,0,0,0.4)`

### 1.4 Reusable Component Specs

**Badge / Status Pill**
```jsx
// Usage: <StatusBadge status="New" /> <StatusBadge status="Approved" />
// Variants: new (blue), approval (yellow), done (green), archived (muted), active (green)
```

**Table (core UI pattern — used everywhere)**
- Header: `bg-elevated`, uppercase, `text-muted`, `text-xs`, letter-spacing 0.08em
- Rows: alternating `bg-surface` / `bg-base`, hover `bg-elevated`
- Sticky header on scroll
- Per-column sort arrows (↑↓ icon toggle, active column accent-colored)
- Pagination bar at bottom: `Showing X–Y of Z` + Prev/Next + page size selector (10/25/50)
- Empty state: centered icon + message inside table body area

**Sidebar Nav Item**
- Icon (20px) + label, 40px height
- Active: `bg-accent-dim`, left border `2px solid var(--accent)`, text accent-colored
- Hover: `bg-elevated`
- Collapsed state (icon-only, 64px wide) — toggled by hamburger

---

## 2. Application Shell

### 2.1 Layout Structure

```
┌─────────────────────────────────────────────────────┐
│  TOPBAR (h-14, fixed)                               │
│  [Hamburger] [Logo: PLM] ─────── [User Avatar/Name] │
├──────────────┬──────────────────────────────────────┤
│              │                                      │
│  SIDEBAR     │   MAIN CONTENT AREA                  │
│  (fixed,     │   (scrollable, padding 24px)         │
│  240px)      │                                      │
│              │                                      │
└──────────────┴──────────────────────────────────────┘
```

### 2.2 Topbar (`components/layout/Topbar.jsx`)

- Height: 56px, `bg-sidebar-bg`, bottom border
- Left: hamburger icon (collapses sidebar to 64px) + PLM logo text
- Right: theme toggle stub (Moon/Sun icon, disabled with tooltip "Light theme coming soon"), notification bell (static), user avatar with dropdown (Profile, Logout)

### 2.3 Sidebar (`components/layout/Sidebar.jsx`)

Fixed left, `width: var(--sidebar-width)`, full height, `bg-sidebar-bg`, right border.

**Nav Items by Role:**

| Icon | Label | Route | Roles |
|------|-------|-------|-------|
| `MdDashboard` | Dashboard | `/dashboard` | All |
| `MdInventory` | Products | `/products` | All |
| `MdAccountTree` | Bill of Materials | `/bom` | Eng, Approver, Admin |
| `MdAssignment` | Change Orders | `/eco` | Eng, Approver, Admin |
| `MdBarChart` | Reports | `/reports` | All |
| `MdSettings` | Settings | `/settings` | Admin only |

Sidebar bottom: User name + role badge, small avatar.

### 2.4 Protected Routes (`router/AppRouter.jsx`)

Use `React Router DOM v6`. Wrap all dashboard routes in `<ProtectedRoute>` HOC that checks JWT + role from context. Redirect unauthenticated users to `/login`.

```jsx
// Route map
/              → redirect to /login
/login         → LoginPage
/signup        → SignupPage
/dashboard     → DashboardPage
/products      → ProductsPage
/products/:id  → ProductDetailPage
/bom           → BomListPage
/bom/:id       → BomDetailPage
/eco           → EcoListPage
/eco/new       → EcoCreatePage
/eco/:id       → EcoDetailPage
/reports       → ReportsPage
/settings      → SettingsPage (Admin only)
```

---

## 3. Auth Pages

### 3.1 Login Page (`pages/auth/LoginPage.jsx`)

**Layout:** Full screen, `bg-base`. Two-column: left = branding panel (60%), right = form (40%).

**Left Panel:**
- Dark background with subtle grid/dot pattern (CSS `radial-gradient`)
- PLM logo large, tagline: *"Engineering Changes, Executed with Control"*
- 3 bullet points with icons: Versioned Changes, Approval Workflows, Full Traceability

**Right Panel (Form):**
- Heading: "Welcome back" (DM Sans 700, 28px)
- Subtext: "Sign in to your PLM workspace"
- Email field
- Password field (show/hide toggle)
- "Forgot password?" link (right-aligned)
- Submit button: full-width, `bg-accent`, "Sign In"
- Bottom: "Don't have an account? Sign up" link

### 3.2 Signup Page (`pages/auth/SignupPage.jsx`)

Same two-column layout.

**Form Fields:**
1. Full Name
2. Email
3. Password — with **password strength meter** directly below the field
4. Confirm Password

**Password Strength Meter (component: `PasswordStrengthMeter.jsx`):**
- 4 segment bar below password input
- Segments fill left to right: 1=Weak (red), 2=Fair (orange/yellow), 3=Good (yellow-green), 4=Strong (green)
- Label below bar: "Weak" / "Fair" / "Good" / "Strong"
- Logic: length ≥ 8, has uppercase, has number, has special char → 1 point each
- Smooth CSS transition on fill

**After submit:** Show pending activation message: *"Your account is pending admin approval. You'll be notified once activated."*

### 3.3 Auth Context (`context/AuthContext.jsx`)

Store: `{ user, token, role, login(), logout() }`. Persist JWT in `localStorage`. Axios default header set on login.

---

## 4. Dashboard Page (`pages/DashboardPage.jsx`)

Role-aware. All roles land here. Content differs.

### 4.1 Stats Row (top)

4 stat cards in a row — boxy, `bg-surface`, 1px border, no shadow theatrics.

| Role | Cards Shown |
|------|-------------|
| Engineering | My Open ECOs, Drafts, Awaiting Approval, Approved This Month |
| Approver | Pending My Review, Approved Today, Rejected, Total Reviewed |
| Operations | Active Products, Active BoMs, Recent Version Updates, — |
| Admin | Total Users, Active ECOs, Pending Signups, Total Products |

Each card: large number (DM Sans 700, 36px), label below, small icon top-right, optional trend arrow.

### 4.2 Main Content Area

**Engineering User:**
- Table: "My Recent ECOs" — columns: Title, Type, Product, Stage, Created Date, Action (View button)
- Quick action button top-right: `+ New ECO`

**Approver:**
- Table: "Pending Approvals" — columns: ECO Title, Type, Product, Submitted By, Stage, Action (Review button)

**Operations:**
- Table: "Recently Updated Products" — columns: Product Name, Version, Last Updated, Status
- Table: "Active BoMs" — abbreviated

**Admin:**
- Table: "Pending User Activations" — columns: Name, Email, Requested At, Action (Activate + Assign Role dropdown)
- Table: "Recent ECO Activity" — columns: ECO Title, User, Stage, Timestamp

---

## 5. Products Module

### 5.1 Products List (`pages/products/ProductsPage.jsx`)

**Toolbar (above table):**
- Left: Page title "Products", record count badge
- Right: Search input (inline, 280px), filter dropdown (Status: All/Active/Archived), `+ New Product` button (Admin/Eng only)

**Table Columns:**
| Column | Sortable | Notes |
|--------|----------|-------|
| Product Name | ✅ | Clickable → detail page |
| Current Version | — | Monospace, e.g. `v3` |
| Sale Price | ✅ | Right-aligned, ₹ formatted |
| Cost Price | ✅ | Right-aligned |
| Status | — | Badge: Active (green) / Archived (muted) |
| Last Updated | ✅ | Relative time |
| Actions | — | View icon button |

Pagination: 10/25/50 per page, Prev/Next.

**Role restrictions:** Operations sees Active only (filter locked). Others see all.

### 5.2 Product Detail (`pages/products/ProductDetailPage.jsx`)

**Header:** Product Name (large), Version badge, Status badge. Breadcrumb: Products > {name}

**Two-column layout:**

Left (60%) — Info Panel:
- Key-value table: Sale Price, Cost Price, Current Version, Status, Created At, Updated At
- Attachments section: file list with download icons

Right (40%) — Version History Panel:
- Timeline/table: Version | Status | Applied Date | ECO Reference (linked)
- Active row highlighted

Bottom — Related ECOs table: Title, Type, Stage, Date, Link

---

## 6. Bill of Materials Module

### 6.1 BoM List (`pages/bom/BomListPage.jsx`)

Same toolbar pattern as Products.

**Table Columns:**
| Column | Notes |
|--------|-------|
| Product | Linked |
| BoM Version | Monospace |
| Components Count | Number |
| Operations Count | Number |
| Status | Active / Archived badge |
| Last Updated | — |
| Actions | View |

### 6.2 BoM Detail (`pages/bom/BomDetailPage.jsx`)

**Header:** Product name + BoM version, status badge. Breadcrumb.

**Two tabs:** `Components` | `Operations`

**Components Tab:**
Table: Component Name | Quantity | Unit | Status (of that component product)  
Row-level visual: no special color (normal state). This is read-only for non-Eng roles.

**Operations Tab:**
Table: Operation Name | Work Center | Duration (mins) | Sequence

**Right sidebar:** BoM metadata card (Product, Version, Status, Created By, ECO Reference)

---

## 7. Engineering Change Orders (ECO) Module

This is the most complex and important module.

### 7.1 ECO List (`pages/eco/EcoListPage.jsx`)

**Toolbar:**
- Search (by title), filter by Type (Product/BoM), filter by Stage (dynamic from API), filter by My ECOs toggle
- `+ New ECO` button (Engineering + Admin only)

**Table Columns:**
| Column | Sortable | Notes |
|--------|----------|-------|
| ECO Title | ✅ | Clickable |
| Type | — | Badge: Product (blue) / BoM (purple) |
| Product | — | Linked |
| Stage | — | Stage badge with color |
| Version Update | — | Yes/No pill |
| Effective Date | ✅ | — |
| Created By | — | — |
| Actions | — | View / Review (Approver) |

Role filtering:
- Operations: cannot see this page (redirect or hidden nav item)
- Approver: defaults to showing ECOs in their approval stage
- Engineering: defaults to showing own ECOs

### 7.2 Create ECO (`pages/eco/EcoCreatePage.jsx`)

**Step-based form** — NOT wizard stepper, just a single clean form with clear sections:

**Section 1: ECO Basics**
- Title (required, text input)
- ECO Type (required): Radio card buttons — `Product Change` | `BoM Change` (large clickable cards with icon + description)
- Product (required): Searchable select dropdown — shows Active products only
- BoM (conditional, shown only if ECO Type = BoM): Searchable select — filtered by selected Product
- Effective Date: Date picker

**Section 2: Version Settings**
- Version Update checkbox: large checkbox with label "Create new version on approval"
  - Checked: shows info banner "A new version will be created. Existing version will be archived."
  - Unchecked: shows warning banner "Changes will be applied to the current version directly."

**Section 3: Proposed Changes (inline, shown after Type is selected)**

*If Product ECO:*
- Sale Price: number input with current value shown as hint
- Cost Price: number input with current value shown as hint
- Attachments: file upload area (drag & drop), list of uploaded files

*If BoM ECO:*
- Components table (editable):
  - Each row: Component Name (read-only) | Current Qty | New Qty (editable number) | Action (remove row)
  - `+ Add Component` button → opens inline searchable row
- Operations table (editable):
  - Each row: Name | Work Center | Current Duration | New Duration | Action
  - `+ Add Operation` button

**Bottom Action Bar (sticky):**
- Left: "Save as Draft" (ghost button)
- Right: "Submit for Review →" (primary button, accent)
- Validation: all required fields must be filled, at least one change must be proposed

### 7.3 ECO Detail (`pages/eco/EcoDetailPage.jsx`)

**Header Section:**
- ECO Title (large), Type badge, Stage badge (with stage progress bar — linear steps)
- Effective Date | Product | Version Update: Yes/No | Created By
- Breadcrumb: ECOs > {title}

**Stage Progress Bar:**
- Horizontal bar showing all configured stages
- Current stage highlighted with accent color, completed stages with checkmark
- Example: `[New ✓] → [Approval ●] → [Done]`

**Two-column layout:**

Left (65%) — Changes Panel:

Tab: `Proposed Changes` | `Comparison View`

*Proposed Changes tab:*
- If Product ECO: key-value pairs of what was changed (Sale Price: old → new)
- If BoM ECO: table of component/operation changes

*Comparison View tab (THE MOST IMPORTANT UI):*

For **BoM ECO:**
```
┌─────────────────────────────────────────────────────────────┐
│  Component Changes                                          │
├─────────────────┬──────────────┬──────────────┬────────────┤
│ Component       │ Old Qty      │ New Qty       │ Change     │
├─────────────────┼──────────────┼──────────────┼────────────┤
│ Wooden Legs     │ 4            │ 4             │ — (grey)   │
│ Screws          │ 12           │ 16            │ ▲ +4 (green row) │
│ New Part XYZ    │ —            │ 8             │ + Added (green row) │
│ Old Part ABC    │ 5            │ —             │ × Removed (red row) │
└─────────────────┴──────────────┴──────────────┴────────────┘
```
Row background colors:
- Added: `bg-green-dim`, text green
- Reduced/Removed: `bg-red-dim`, text red
- Unchanged: normal

Operations changes shown in separate table below with same color logic.

For **Product ECO:**
```
┌────────────────────────────────────────────────────┐
│            Side-by-Side Comparison                 │
├───────────────────┬────────────┬───────────────────┤
│ Field             │ Current    │ Proposed          │
├───────────────────┼────────────┼───────────────────┤
│ Sale Price        │ ₹1,200     │ ₹1,400  ▲         │
│ Cost Price        │ ₹800       │ ₹900    ▲         │
│ Attachments       │ spec_v1.pdf│ spec_v2.pdf (new) │
└───────────────────┴────────────┴───────────────────┘
```
Changed rows: `bg-yellow-dim`

Right (35%) — Action & Info Panel:

Card 1: ECO Metadata
- All fields in compact key-value format

Card 2: Stage Actions
- Engineering (own draft ECO, stage=New): "Submit for Approval" button
- Approver (ECO in their stage): "Approve" (green) + "Reject / Send Back" (red outline) buttons
- If no approver configured for stage: "Validate" button (blue)
- After final stage: "Applied" state, green checkmark, timestamp

Card 3: Activity Log (bottom of right panel)
- Timeline list: icon + action text + user + timestamp
- e.g. "ECO Created by John · 2 days ago"
- "Moved to Approval by John · 1 day ago"
- "Approved by Sarah · 3 hours ago"

---

## 8. Reports Module (`pages/reports/ReportsPage.jsx`)

**Left nav tabs (within page):**
1. ECO Report
2. Product Version History
3. BoM Change History
4. Archived Products
5. Active Product–Version–BoM Matrix

### 8.1 ECO Report

Toolbar: date range picker, type filter, stage filter, export button (stub)

Table: ECO Title | ECO Type | Product | Stage | Created By | Date | Changes (clickable "View Diff" link)

Clicking "View Diff" opens a slide-over/drawer from right showing the same comparison view as ECO detail.

### 8.2 Product Version History

Select product (dropdown). Table: Version | Status | Applied Date | ECO Reference | Changes Summary

### 8.3 BoM Change History

Select product → select BoM. Table: Change | Old Value | New Value | ECO | Applied By | Date

### 8.4 Archived Products

Table: Product Name | Version | Archived Date | Archived Reason (ECO link) | View (read-only)

### 8.5 Active Product–Version–BoM Matrix

Grid/table: Product Name | Active Version | Active BoM Version | Components Count | Last Change Date

---

## 9. Settings Module (`pages/settings/SettingsPage.jsx`)

**Admin only.** Two sub-sections via tabs.

### 9.1 ECO Stages Tab

**Stage Pipeline Visualizer (top):**
Visual horizontal pipeline: boxes connected by arrows showing current stage order.
Example: `[New] → [Engineering Review] → [Approval] → [Done]`

**Stages Table below:**
| Stage Name | Order | Is Final Stage | Requires Approval | Approvers Assigned | Actions |
|------------|-------|----------------|-------------------|-------------------|---------|
| New | 1 | No | No | — | Edit / Delete |
| Approval | 2 | No | Yes | Sarah, Mike | Edit / Delete |
| Done | 3 | Yes | No | — | Edit / Delete |

`+ Add Stage` button opens inline form row or modal.

Edit Stage Modal:
- Stage Name input
- Order (number)
- Is Final Stage toggle
- Requires Approval toggle
- Assign Approvers (multi-select user picker — searchable)

### 9.2 User Management Tab

**Pending Activations Section (top, highlighted if any pending):**
Table: Name | Email | Requested At | Activate (button) + Role dropdown (Eng / Approver / Operations / Admin)

**All Users Section:**
Table: Name | Email | Role (editable badge dropdown) | Status (Active/Inactive) | Joined | Actions (Deactivate)

---

## 10. Component Library Map

All components live in `src/components/`:

```
components/
├── layout/
│   ├── Topbar.jsx
│   ├── Sidebar.jsx
│   ├── SidebarNavItem.jsx
│   └── AppShell.jsx          ← wraps Sidebar + Topbar + <Outlet>
│
├── ui/
│   ├── StatusBadge.jsx       ← status/stage colored pills
│   ├── DataTable.jsx         ← reusable sortable paginated table
│   ├── StatCard.jsx          ← dashboard stat card
│   ├── SectionHeader.jsx     ← page title + action button row
│   ├── SearchInput.jsx       ← styled search with icon
│   ├── FilterSelect.jsx      ← styled dropdown filter
│   ├── ConfirmModal.jsx      ← generic confirm dialog
│   ├── SlideDrawer.jsx       ← right slide-over panel
│   └── EmptyState.jsx        ← empty table/page state
│
├── auth/
│   └── PasswordStrengthMeter.jsx
│
├── eco/
│   ├── StageProgressBar.jsx
│   ├── BomComparisonTable.jsx
│   ├── ProductComparisonTable.jsx
│   ├── ActivityLog.jsx
│   ├── EcoBomChangesEditor.jsx
│   └── EcoProductChangesEditor.jsx
│
└── settings/
    ├── StagePipelineVisualizer.jsx
    └── StageFormModal.jsx
```

---

## 11. API Integration Map (Axios)

Create `src/api/` with one file per module:

```
api/
├── axiosInstance.js    ← base URL, interceptors, JWT header
├── authApi.js          ← login, signup, logout
├── productApi.js       ← getProducts, getProductById, createProduct
├── bomApi.js           ← getBoms, getBomById
├── ecoApi.js           ← getEcos, getEcoById, createEco, submitEco, approveEco, rejectEco
├── stageApi.js         ← getStages, createStage, updateStage, deleteStage
├── userApi.js          ← getUsers, activateUser, updateUserRole
└── reportApi.js        ← getEcoReport, getVersionHistory, etc.
```

**Axios Instance:**
- `baseURL: import.meta.env.VITE_API_BASE_URL`
- Request interceptor: inject `Authorization: Bearer {token}` from localStorage
- Response interceptor: on 401 → logout + redirect to `/login`, on other errors → toast notification

---

## 12. State Management

Use **React Context + useReducer** (no Redux needed for this scale):

```
context/
├── AuthContext.jsx       ← user, token, role
└── ThemeContext.jsx      ← dark/light toggle (stub for now, always dark)
```

All server state managed via **local component state + Axios calls** (no React Query needed unless builder prefers it).

---

## 13. Toast Notification Rules

Use `react-toastify` with custom dark theme config matching the design system.

| Event | Toast Type |
|-------|-----------|
| ECO Created | Success |
| ECO Submitted | Success |
| ECO Approved | Success |
| ECO Rejected | Info |
| Save Draft | Info |
| User Activated | Success |
| API Error (4xx) | Error (show message from API) |
| API Error (5xx) | Error ("Something went wrong. Please try again.") |
| Validation Error | Warning |

Config in `main.jsx`:
```jsx
<ToastContainer
  position="bottom-right"
  theme="dark"
  toastStyle={{ background: 'var(--bg-elevated)', border: '1px solid var(--bg-border)' }}
/>
```

---

## 14. Role-Based UI Guards

Create `utils/roleGuards.js`:
```js
export const ROLES = {
  ENGINEERING: 'ENGINEERING',
  APPROVER: 'APPROVER',
  OPERATIONS: 'OPERATIONS',
  ADMIN: 'ADMIN',
};

export const can = (role, action) => { ... }
```

Use `<RoleGuard roles={['ADMIN', 'ENGINEERING']}>` wrapper component to conditionally render UI elements (buttons, nav items, table action columns).

---

## 15. Page-by-Page Accessibility Checklist

- All inputs have associated `<label>` elements
- Buttons have `aria-label` when icon-only
- Tables have proper `<thead>` + `<th scope>` structure
- Focus visible states styled (never `outline: none` without replacement)
- Color is never the only indicator (always pair color with icon/text)

---

## 16. File Structure

```
src/
├── api/
├── assets/
│   └── logo.svg
├── components/
│   ├── layout/
│   ├── ui/
│   ├── auth/
│   ├── eco/
│   └── settings/
├── context/
├── pages/
│   ├── auth/
│   │   ├── LoginPage.jsx
│   │   └── SignupPage.jsx
│   ├── DashboardPage.jsx
│   ├── products/
│   │   ├── ProductsPage.jsx
│   │   └── ProductDetailPage.jsx
│   ├── bom/
│   │   ├── BomListPage.jsx
│   │   └── BomDetailPage.jsx
│   ├── eco/
│   │   ├── EcoListPage.jsx
│   │   ├── EcoCreatePage.jsx
│   │   └── EcoDetailPage.jsx
│   ├── reports/
│   │   └── ReportsPage.jsx
│   └── settings/
│       └── SettingsPage.jsx
├── router/
│   ├── AppRouter.jsx
│   └── ProtectedRoute.jsx
├── utils/
│   └── roleGuards.js
├── index.css         ← all CSS variables, base styles
└── main.jsx
```

---

## 17. Additional npm Dependencies Needed

Beyond the existing setup, install these:

```bash
npm install react-toastify date-fns clsx react-day-picker
```

- `react-toastify` — toast notifications
- `date-fns` — date formatting (relative time, formatting)
- `clsx` — conditional className utility
- `react-day-picker` — date picker for Effective Date field

ShadCN components to initialize:
```bash
npx shadcn@latest add button input label select dialog tabs badge tooltip dropdown-menu separator
```

---

## 18. Build & Environment

```
.env
VITE_API_BASE_URL=http://localhost:8080/api
```

`vite.config.js` — no special changes needed for this scope.

---

## 19. Key Constraints & Rules (Must Follow)

1. **Never allow direct navigation to ECO Create for Operations role** — redirect to dashboard
2. **Comparison View is always read-only** — no editing from the diff screen
3. **Archived products** — show in lists with visual dimming + lock icon, never selectable in dropdowns
4. **ECO submission** — validate that at least one change is proposed before allowing Submit
5. **Stage progression** — always driven by backend; frontend only calls the API and refreshes state
6. **Password strength** — purely frontend, never sent to backend as a field
7. **All monetary values** — display with `₹` symbol and thousand separators
8. **Version numbers** — always displayed in IBM Plex Mono, e.g. `v1`, `v2`
9. **Light theme** — wire the toggle button and ThemeContext, but the `.light` CSS class overrides are left as `// TODO` stubs — no light styles written yet
10. **No inline styles** — use Tailwind classes and CSS variables only

---

## 20. Priority Build Order (for Claude Code)

Build in this sequence so each layer is testable before the next:

1. `index.css` — full design token setup
2. `AppShell` + `Sidebar` + `Topbar` + `AppRouter` + `ProtectedRoute`
3. `AuthContext` + `axiosInstance`
4. `LoginPage` + `SignupPage` (with PasswordStrengthMeter)
5. Reusable UI: `DataTable`, `StatusBadge`, `StatCard`, `SectionHeader`
6. `DashboardPage` (all 4 role views)
7. `ProductsPage` + `ProductDetailPage`
8. `BomListPage` + `BomDetailPage`
9. `EcoListPage` → `EcoCreatePage` → `EcoDetailPage` (largest effort)
10. `ReportsPage`
11. `SettingsPage` (Admin)
12. Toast wiring, role guards, error boundaries

---

*End of PRD — Version 1.0*
*Ready for Claude Code implementation.*
