import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  ClipboardList,
  FileEdit,
  Clock,
  CheckCircle,
  Users,
  Package,
  GitBranch,
  XCircle,
  Plus,
  Eye,
  AlertCircle,
} from 'lucide-react';
import useAuthStore from '../context/authStore';
import { ROLES } from '../utils/roleGuards';
import { StatCard, SectionHeader, DataTable, StatusBadge } from '../components/ui';
import { reportApi, ecoApi, userApi, productApi } from '../api';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const DashboardPage = () => {
  const navigate = useNavigate();
  const { role, user } = useAuthStore();
  const [stats, setStats] = useState(null);
  const [tableData, setTableData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedRoles, setSelectedRoles] = useState({});
  const [activatingUser, setActivatingUser] = useState(null);

  useEffect(() => {
    loadDashboardData();
  }, [role]);

  const loadDashboardData = async () => {
    setIsLoading(true);
    try {
      const statsData = await reportApi.getDashboardStats(role);
      setStats(statsData);

      switch (role) {
        case ROLES.ENGINEERING:
          const myEcos = await ecoApi.getMyEcos({ limit: 5 });
          setTableData(myEcos.ecos || myEcos.content || myEcos.data || myEcos || []);
          break;
        case ROLES.APPROVER:
          const pending = await ecoApi.getPendingApprovals({ limit: 5 });
          setTableData(pending.ecos || pending.content || pending.data || pending || []);
          break;
        case ROLES.ADMIN:
          const pendingUsers = await userApi.getPendingUsers();
          setTableData(pendingUsers || []);
          break;
        default:
          const products = await productApi.getProducts({ size: 5, sort: 'updatedAt,desc' });
          setTableData(products.products || products.content || products.data || products || []);
      }
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
      setStats(getMockStats(role));
      setTableData(getMockTableData(role));
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleChange = (userId, selectedRole) => {
    setSelectedRoles(prev => ({
      ...prev,
      [userId]: selectedRole
    }));
  };

  const handleActivateUser = async (userId) => {
    const selectedRole = selectedRoles[userId];
    if (!selectedRole) {
      alert('Please select a role before activating');
      return;
    }

    setActivatingUser(userId);
    try {
      await userApi.activateUser(userId, selectedRole);
      // Remove activated user from table
      setTableData(prev => prev.filter(u => u.id !== userId));
      // Update stats
      if (stats) {
        setStats(prev => ({
          ...prev,
          pendingSignups: Math.max(0, (prev.pendingSignups || 0) - 1),
          totalUsers: (prev.totalUsers || 0) + 1
        }));
      }
    } catch (error) {
      console.error('Failed to activate user:', error);
      alert('Failed to activate user: ' + (error.response?.data?.message || error.message));
    } finally {
      setActivatingUser(null);
    }
  };

  const renderStats = () => {
    if (!stats) return null;

    const statsConfig = {
      [ROLES.ENGINEERING]: [
        { key: 'myOpenEcos', label: 'My Open ECOs', icon: ClipboardList },
        { key: 'drafts', label: 'Drafts', icon: FileEdit },
        { key: 'awaitingApproval', label: 'Awaiting Approval', icon: Clock },
        { key: 'approvedThisMonth', label: 'Approved This Month', icon: CheckCircle },
      ],
      [ROLES.APPROVER]: [
        { key: 'pendingReview', label: 'Pending My Review', icon: AlertCircle },
        { key: 'approvedToday', label: 'Approved Today', icon: CheckCircle },
        { key: 'rejected', label: 'Rejected', icon: XCircle },
        { key: 'totalReviewed', label: 'Total Reviewed', icon: ClipboardList },
      ],
      [ROLES.OPERATIONS]: [
        { key: 'activeProducts', label: 'Active Products', icon: Package },
        { key: 'activeBoms', label: 'Active BoMs', icon: GitBranch },
        { key: 'recentUpdates', label: 'Recent Updates', icon: Clock },
        { key: 'placeholder', label: '—', icon: null },
      ],
      [ROLES.ADMIN]: [
        { key: 'totalUsers', label: 'Total Users', icon: Users },
        { key: 'activeEcos', label: 'Active ECOs', icon: ClipboardList },
        { key: 'pendingSignups', label: 'Pending Signups', icon: Clock },
        { key: 'totalProducts', label: 'Total Products', icon: Package },
      ],
    };

    const config = statsConfig[role] || statsConfig[ROLES.OPERATIONS];

    return (
      <div className="stats-grid">
        {config.map((stat) => (
          <StatCard
            key={stat.key}
            label={stat.label}
            value={stats[stat.key] ?? '—'}
            icon={stat.icon}
            trend={stats[`${stat.key}Trend`]}
            trendValue={stats[`${stat.key}TrendValue`]}
          />
        ))}
      </div>
    );
  };

  const renderTable = () => {
    if (role === ROLES.ENGINEERING) {
      return (
        <div>
          <SectionHeader
            title="My Recent ECOs"
            count={tableData.length}
            action={
              <Link to="/eco/new" className="btn btn-primary">
                <Plus size={18} />
                New ECO
              </Link>
            }
          />
          <DataTable
            columns={[
              { key: 'title', header: 'Title' },
              {
                key: 'ecoType',
                header: 'Type',
                render: (val) => <StatusBadge status={val} />,
              },
              { key: 'productName', header: 'Product' },
              {
                key: 'currentStageName',
                header: 'Stage',
                render: (val) => <StatusBadge status={val || 'Unknown'} />,
              },
              {
                key: 'createdAt',
                header: 'Created',
                render: (val) => dayjs(val).fromNow(),
              },
              {
                key: 'actions',
                header: 'Action',
                sortable: false,
                render: (_, row) => (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/eco/${row.id}`);
                    }}
                    className="btn btn-ghost btn-sm"
                  >
                    <Eye size={16} />
                    View
                  </button>
                ),
              },
            ]}
            data={tableData}
            onRowClick={(row) => navigate(`/eco/${row.id}`)}
            isLoading={isLoading}
          />
        </div>
      );
    }

    if (role === ROLES.APPROVER) {
      return (
        <div>
          <SectionHeader title="Pending Approvals" count={tableData.length} />
          <DataTable
            columns={[
              { key: 'title', header: 'ECO Title' },
              {
                key: 'ecoType',
                header: 'Type',
                render: (val) => <StatusBadge status={val} />,
              },
              { key: 'productName', header: 'Product' },
              { key: 'createdByName', header: 'Submitted By' },
              {
                key: 'currentStageName',
                header: 'Stage',
                render: (val) => <StatusBadge status={val || 'Unknown'} />,
              },
              {
                key: 'actions',
                header: 'Action',
                sortable: false,
                render: (_, row) => (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/eco/${row.id}`);
                    }}
                    className="btn btn-primary btn-sm"
                  >
                    Review
                  </button>
                ),
              },
            ]}
            data={tableData}
            onRowClick={(row) => navigate(`/eco/${row.id}`)}
            isLoading={isLoading}
          />
        </div>
      );
    }

    if (role === ROLES.ADMIN) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div>
            <SectionHeader
              title="Pending User Activations"
              count={tableData.length}
            />
            <DataTable
              columns={[
                { key: 'name', header: 'Name' },
                { key: 'email', header: 'Email' },
                {
                  key: 'createdAt',
                  header: 'Requested At',
                  render: (val) => dayjs(val).fromNow(),
                },
                {
                  key: 'actions',
                  header: 'Action',
                  sortable: false,
                  render: (_, row) => (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <select
                        className="input"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem', width: '128px' }}
                        value={selectedRoles[row.id] || ''}
                        onChange={(e) => handleRoleChange(row.id, e.target.value)}
                      >
                        <option value="">Select Role</option>
                        <option value="ENGINEERING_USER">Engineering</option>
                        <option value="APPROVER">Approver</option>
                        <option value="OPERATIONS_USER">Operations</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                      <button
                        className="btn btn-success btn-sm"
                        onClick={() => handleActivateUser(row.id)}
                        disabled={!selectedRoles[row.id] || activatingUser === row.id}
                      >
                        {activatingUser === row.id ? 'Activating...' : 'Activate'}
                      </button>
                    </div>
                  ),
                },
              ]}
              data={tableData}
              isLoading={isLoading}
              pagination={false}
            />
          </div>
        </div>
      );
    }

    return (
      <div>
        <SectionHeader title="Recently Updated Products" />
        <DataTable
          columns={[
            { key: 'name', header: 'Product Name' },
            {
              key: 'version',
              header: 'Version',
              mono: true,
              render: (val) => `v${val}`,
            },
            {
              key: 'updatedAt',
              header: 'Last Updated',
              render: (val) => dayjs(val).fromNow(),
            },
            {
              key: 'status',
              header: 'Status',
              render: (val) => <StatusBadge status={val} />,
            },
          ]}
          data={tableData}
          onRowClick={(row) => navigate(`/products/${row.id}`)}
          isLoading={isLoading}
        />
      </div>
    );
  };

  return (
    <div>
      <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '1.5rem' }}>
        Welcome back, {user?.name?.split(' ')[0] || 'User'}
      </h1>

      {renderStats()}
      {renderTable()}
    </div>
  );
};

const getMockStats = (role) => {
  const mockStats = {
    [ROLES.ENGINEERING]: {
      myOpenEcos: 5,
      drafts: 2,
      awaitingApproval: 3,
      approvedThisMonth: 12,
    },
    [ROLES.APPROVER]: {
      pendingReview: 8,
      approvedToday: 3,
      rejected: 1,
      totalReviewed: 47,
    },
    [ROLES.OPERATIONS]: {
      activeProducts: 24,
      activeBoms: 18,
      recentUpdates: 7,
    },
    [ROLES.ADMIN]: {
      totalUsers: 15,
      activeEcos: 23,
      pendingSignups: 2,
      totalProducts: 45,
    },
  };
  return mockStats[role] || mockStats[ROLES.OPERATIONS];
};

const getMockTableData = (role) => {
  // Don't use mock table data - it causes 404 errors when clicked
  // because the fake UUIDs don't exist in the database
  return [];
};

export default DashboardPage;
