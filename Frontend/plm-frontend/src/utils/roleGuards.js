export const ROLES = {
  ENGINEERING: 'ENGINEERING_USER',
  APPROVER: 'APPROVER',
  OPERATIONS: 'OPERATIONS_USER',
  ADMIN: 'ADMIN',
};

const permissions = {
  // Navigation permissions
  'nav.bom': [ROLES.ENGINEERING, ROLES.APPROVER, ROLES.OPERATIONS, ROLES.ADMIN],
  'nav.eco': [ROLES.ENGINEERING, ROLES.APPROVER, ROLES.ADMIN],
  'nav.settings': [ROLES.ADMIN],

  // ECO permissions
  'eco.create': [ROLES.ENGINEERING, ROLES.ADMIN],
  'eco.edit': [ROLES.ENGINEERING, ROLES.ADMIN],
  'eco.approve': [ROLES.APPROVER, ROLES.ADMIN],
  'eco.submit': [ROLES.ENGINEERING, ROLES.ADMIN],

  // Product permissions (only ADMIN can create/edit products)
  'product.create': [ROLES.ADMIN],
  'product.edit': [ROLES.ADMIN],

  // BOM permissions (only ADMIN can create/edit BOMs)
  'bom.create': [ROLES.ADMIN],
  'bom.edit': [ROLES.ADMIN],

  // User management
  'user.manage': [ROLES.ADMIN],
  'user.activate': [ROLES.ADMIN],

  // Stage management
  'stage.manage': [ROLES.ADMIN],
};

/**
 * Check if a role has permission for a specific action
 * @param {string} role - The user's role
 * @param {string} action - The action to check permission for
 * @returns {boolean}
 */
export const can = (role, action) => {
  if (!role || !action) return false;
  const allowedRoles = permissions[action];
  if (!allowedRoles) return false;
  return allowedRoles.includes(role);
};

/**
 * Check if a role is in the allowed roles list
 * @param {string} role - The user's role
 * @param {string[]} allowedRoles - Array of allowed roles
 * @returns {boolean}
 */
export const hasRole = (role, allowedRoles) => {
  if (!role || !allowedRoles) return false;
  return allowedRoles.includes(role);
};

/**
 * Get display name for a role
 * @param {string} role - The role
 * @returns {string}
 */
export const getRoleDisplayName = (role) => {
  const names = {
    [ROLES.ENGINEERING]: 'Engineering',
    [ROLES.APPROVER]: 'Approver',
    [ROLES.OPERATIONS]: 'Operations',
    [ROLES.ADMIN]: 'Admin',
  };
  return names[role] || role;
};

/**
 * Get role badge color
 * @param {string} role - The role
 * @returns {string}
 */
export const getRoleBadgeColor = (role) => {
  const colors = {
    [ROLES.ENGINEERING]: 'blue',
    [ROLES.APPROVER]: 'yellow',
    [ROLES.OPERATIONS]: 'green',
    [ROLES.ADMIN]: 'accent',
  };
  return colors[role] || 'muted';
};
