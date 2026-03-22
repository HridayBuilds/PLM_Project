import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppShell } from '../components/layout';
import ProtectedRoute from './ProtectedRoute';
import { ROLES } from '../utils/roleGuards';

// Auth pages
import AuthPage from '../pages/auth/AuthPage';
import VerifyEmailPage from '../pages/auth/VerifyEmailPage';

// Main pages
import DashboardPage from '../pages/DashboardPage';
import ProductsPage from '../pages/products/ProductsPage';
import ProductCreatePage from '../pages/products/ProductCreatePage';
import ProductDetailPage from '../pages/products/ProductDetailPage';
import BomListPage from '../pages/bom/BomListPage';
import BomCreatePage from '../pages/bom/BomCreatePage';
import BomDetailPage from '../pages/bom/BomDetailPage';
import EcoListPage from '../pages/eco/EcoListPage';
import EcoCreatePage from '../pages/eco/EcoCreatePage';
import EcoDetailPage from '../pages/eco/EcoDetailPage';
import EcoComparisonPage from '../pages/eco/EcoComparisonPage';
import ReportsPage from '../pages/reports/ReportsPage';
import SettingsPage from '../pages/settings/SettingsPage';
import ProfilePage from '../pages/profile/ProfilePage';

const router = createBrowserRouter([
  // Public routes
  {
    path: '/',
    element: <Navigate to="/login" replace />,
  },
  {
    path: '/login',
    element: <AuthPage />,
  },
  {
    path: '/signup',
    element: <AuthPage />,
  },
  {
    path: '/verify',
    element: <VerifyEmailPage />,
  },

  // Protected routes (wrapped in AppShell)
  {
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      {
        path: '/dashboard',
        element: <DashboardPage />,
      },
      {
        path: '/products',
        element: <ProductsPage />,
      },
      {
        path: '/products/new',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ADMIN]}>
            <ProductCreatePage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/products/:id',
        element: <ProductDetailPage />,
      },
      {
        path: '/bom',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ENGINEERING, ROLES.APPROVER, ROLES.OPERATIONS, ROLES.ADMIN]}>
            <BomListPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/bom/new',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ADMIN]}>
            <BomCreatePage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/bom/:id',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ENGINEERING, ROLES.APPROVER, ROLES.OPERATIONS, ROLES.ADMIN]}>
            <BomDetailPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/eco',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ENGINEERING, ROLES.APPROVER, ROLES.ADMIN]}>
            <EcoListPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/eco/new',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ENGINEERING, ROLES.ADMIN]}>
            <EcoCreatePage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/eco/:id',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ENGINEERING, ROLES.APPROVER, ROLES.ADMIN]}>
            <EcoDetailPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/eco/:id/comparison',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ENGINEERING, ROLES.APPROVER, ROLES.ADMIN]}>
            <EcoComparisonPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/reports',
        element: <ReportsPage />,
      },
      {
        path: '/profile',
        element: <ProfilePage />,
      },
      {
        path: '/settings',
        element: (
          <ProtectedRoute allowedRoles={[ROLES.ADMIN]}>
            <SettingsPage />
          </ProtectedRoute>
        ),
      },
    ],
  },

  // Catch all - redirect to login
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);

export default router;
