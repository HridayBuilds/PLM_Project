const statusConfig = {
  // ECO Stages
  new: { color: 'blue', label: 'New' },
  approval: { color: 'yellow', label: 'Approval' },
  done: { color: 'green', label: 'Done' },
  draft: { color: 'muted', label: 'Draft' },
  in_progress: { color: 'accent', label: 'In Progress' },
  'in progress': { color: 'accent', label: 'In Progress' },
  cancelled: { color: 'red', label: 'Cancelled' },

  // ECO Status
  approved: { color: 'green', label: 'Approved' },
  applied: { color: 'green', label: 'Applied' },

  // Product/BoM Status
  active: { color: 'green', label: 'Active' },
  archived: { color: 'muted', label: 'Archived' },
  inactive: { color: 'red', label: 'Inactive' },

  // ECO Types
  product: { color: 'blue', label: 'Product' },
  bom: { color: 'accent', label: 'BoM' },

  // Boolean indicators
  yes: { color: 'green', label: 'Yes' },
  no: { color: 'muted', label: 'No' },

  // User Status
  pending: { color: 'yellow', label: 'Pending' },
  rejected: { color: 'red', label: 'Rejected' },
};

const colorStyles = {
  blue: { backgroundColor: 'var(--blue-dim)', color: 'var(--blue)' },
  green: { backgroundColor: 'var(--green-dim)', color: 'var(--green)' },
  yellow: { backgroundColor: 'var(--yellow-dim)', color: 'var(--yellow)' },
  red: { backgroundColor: 'var(--red-dim)', color: 'var(--red)' },
  muted: { backgroundColor: 'rgba(85, 85, 106, 0.2)', color: 'var(--text-muted)' },
  accent: { backgroundColor: 'var(--accent-dim)', color: 'var(--accent)' },
  cyan: { backgroundColor: 'var(--cyan-dim)', color: 'var(--cyan)' },
};

const sizeStyles = {
  sm: { padding: '0.125rem 0.375rem', fontSize: '10px' },
  md: { padding: '0.25rem 0.625rem', fontSize: '0.75rem' },
  lg: { padding: '0.375rem 0.75rem', fontSize: '0.875rem' },
};

const StatusBadge = ({ status, customLabel, size = 'md', className }) => {
  const normalizedStatus = status?.toLowerCase()?.replace(/\s+/g, '_');
  const config = statusConfig[normalizedStatus] || { color: 'muted', label: status };

  const badgeStyle = {
    display: 'inline-flex',
    alignItems: 'center',
    fontWeight: 500,
    borderRadius: '4px',
    ...sizeStyles[size],
    ...colorStyles[config.color],
  };

  return (
    <span style={badgeStyle} className={className}>
      {customLabel || config.label}
    </span>
  );
};

export default StatusBadge;
