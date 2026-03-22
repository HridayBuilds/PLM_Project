import { Navigate, useLocation } from 'react-router-dom';
import useAuthStore from '../context/authStore';
import { hasRole } from '../utils/roleGuards';

const ProtectedRoute = ({ children, allowedRoles = null }) => {
  const { isAuthenticated, role } = useAuthStore();
  const location = useLocation();

  // Check if user is authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check if user has required role (if specified)
  if (allowedRoles && !hasRole(role, allowedRoles)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default ProtectedRoute;
