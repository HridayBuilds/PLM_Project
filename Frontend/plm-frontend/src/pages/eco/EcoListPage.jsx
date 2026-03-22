import { useState, useEffect, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Plus, Eye, Filter, SortAsc, ChevronLeft, ChevronRight, MoreHorizontal, GitCompare } from 'lucide-react';
import useAuthStore from '../../context/authStore';
import { can, ROLES } from '../../utils/roleGuards';
import { ecoApi, stageApi } from '../../api';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import clsx from 'clsx';

dayjs.extend(relativeTime);

const EcoListPage = () => {
  const navigate = useNavigate();
  const { role, user } = useAuthStore();
  const [ecos, setEcos] = useState([]);
  const [stages, setStages] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 10;

  // Filters
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');
  const [stateFilter, setStateFilter] = useState('all');

  useEffect(() => {
    loadData();
  }, [currentPage]);

  const loadData = async () => {
    setIsLoading(true);
    try {
      // Use the GET /api/ecos endpoint for all ECOs listing
      const ecosData = await ecoApi.getAllEcos({ 
        page: currentPage - 1, 
        size: pageSize,
        sortBy: 'createdAt',
        sortDir: 'desc'
      });

      const ecosList = Array.isArray(ecosData)
        ? ecosData
        : (ecosData?.ecos || ecosData?.content || ecosData?.data || []);
      setEcos(Array.isArray(ecosList) ? ecosList : []);
      setTotalPages(ecosData?.totalPages || 1);

      setStages([{id: 'DRAFT', name: 'Draft'}, {id: 'IN_PROGRESS', name: 'In Progress'}, {id: 'APPROVED', name: 'Approved'}]);
    } catch (error) {
      console.error('Failed to load ECOs:', error);
      setEcos([]);
      setStages([]);
      setTotalPages(1);
    } finally {
      setIsLoading(false);
    }
  };

  // Calculate stats - use currentStageName from backend
  const stats = useMemo(() => {
    const active = ecos.filter((e) => e.status === 'DRAFT' || e.status === 'IN_PROGRESS').length;
    const inProgress = ecos.filter((e) => {
      const stageName = e.currentStageName || e.currentStage?.name;
      return stageName === 'In Progress' || stageName === 'Approval';
    }).length;
    const approved = ecos.filter((e) => {
      const stageName = e.currentStageName || e.currentStage?.name;
      return e.status === 'APPROVED' || e.status === 'APPLIED' || stageName === 'Done';
    }).length;
    const cancelled = ecos.filter((e) => {
      const stageName = e.currentStageName || e.currentStage?.name;
      return e.status === 'CANCELLED' || stageName === 'Cancelled';
    }).length;

    return { active, inProgress, approved, cancelled };
  }, [ecos]);

  // Filter ECOs - use currentStageName from backend
  const filteredEcos = useMemo(() => {
    return ecos.filter((eco) => {
      const matchesSearch =
        eco.title?.toLowerCase().includes(search.toLowerCase()) ||
        eco.reference?.toLowerCase().includes(search.toLowerCase());
      const matchesType =
        typeFilter === 'all' || eco.ecoType?.toLowerCase() === typeFilter.toLowerCase();
      const stageName = eco.currentStageName || eco.currentStage?.name || '';
      const matchesState =
        stateFilter === 'all' ||
        stageName.toLowerCase() === stateFilter.toLowerCase();

      return matchesSearch && matchesType && matchesState;
    });
  }, [ecos, search, typeFilter, stateFilter]);

  const getStateBadgeClass = (eco) => {
    const stageName = (eco.currentStageName || eco.currentStage?.name || eco.status || '').toLowerCase();
    switch (stageName) {
      case 'draft':
        return 'badge-muted';
      case 'new':
        return 'badge-blue';
      case 'in progress':
      case 'in_progress':
        return 'badge-accent';
      case 'approval':
        return 'badge-yellow';
      case 'done':
      case 'approved':
      case 'applied':
        return 'badge-green';
      case 'cancelled':
        return 'badge-red';
      default:
        return 'badge-muted';
    }
  };

  const getTypeBadgeClass = (type) => {
    return type?.toLowerCase() === 'bom' ? 'badge-accent' : 'badge-blue';
  };

  return (
    <div>
      {/* Stats Grid */}
      <div className="stats-grid">
        <div className={clsx('eco-stat-card', 'active')}>
          <span className="eco-stat-value">{stats.active}</span>
          <span className="eco-stat-label">Active ECOs</span>
        </div>
        <div className="eco-stat-card">
          <span className="eco-stat-value">{stats.inProgress}</span>
          <span className="eco-stat-label">In Progress</span>
        </div>
        <div className="eco-stat-card">
          <span className="eco-stat-value">{stats.approved}</span>
          <span className="eco-stat-label">Approved</span>
        </div>
        <div className="eco-stat-card">
          <span className="eco-stat-value">{stats.cancelled}</span>
          <span className="eco-stat-label">Cancelled</span>
        </div>
      </div>

      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold text-[var(--text-primary)]">
          Engineering Change Orders
        </h1>
        <div className="flex items-center gap-3">
          {/* Filter Button */}
          <div className="relative">
            <button className="btn btn-secondary">
              <Filter size={16} />
              Filter
            </button>
          </div>

          {/* Sort Button */}
          <button className="btn btn-secondary">
            <SortAsc size={16} />
            Sort
          </button>

          {/* New ECO Button */}
          {can(role, 'eco.create') && (
            <Link to="/eco/new" className="btn btn-primary">
              <Plus size={18} />
              New ECO
            </Link>
          )}
        </div>
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Reference Name</th>
                <th>ECO Type</th>
                <th>Product Assembly</th>
                <th>Current State</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="text-center py-8">
                    <div className="animate-pulse text-[var(--text-muted)]">Loading...</div>
                  </td>
                </tr>
              ) : filteredEcos.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-8">
                    <p className="text-[var(--text-muted)]">No ECOs found</p>
                  </td>
                </tr>
              ) : (
                filteredEcos.map((eco) => (
                  <tr
                    key={eco.id}
                    onClick={() => navigate(`/eco/${eco.id}`)}
                    className="cursor-pointer"
                  >
                    <td>
                      <div>
                        <p className="font-medium text-[var(--text-primary)]">
                          {eco.reference || `ECO-${eco.id}`}
                        </p>
                        <p className="text-sm text-[var(--text-secondary)]">{eco.title}</p>
                      </div>
                    </td>
                    <td>
                      <span className={clsx('badge', getTypeBadgeClass(eco.ecoType))}>
                        {eco.ecoType === 'BOM' ? 'BoM' : 'Product'}
                      </span>
                    </td>
                    <td>
                      <Link
                        to={`/products/${eco.productId}`}
                        className="text-[var(--accent)] hover:underline"
                        onClick={(e) => e.stopPropagation()}
                      >
                        {eco.productName || eco.product?.name || 'Unknown Product'}
                      </Link>
                    </td>
                    <td>
                      <span className={clsx('badge', getStateBadgeClass(eco))}>
                        {eco.currentStageName || eco.currentStage?.name || eco.status}
                      </span>
                    </td>
                    <td>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/eco/${eco.id}`);
                          }}
                          className="p-1.5 rounded hover:bg-[var(--bg-elevated)] text-[var(--text-secondary)]"
                          title="View ECO"
                        >
                          <Eye size={18} />
                        </button>
                        {((eco.currentStageName || eco.currentStage?.name) === 'In Progress' ||
                          (eco.currentStageName || eco.currentStage?.name) === 'Approval') && (
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              navigate(`/eco/${eco.id}/comparison`);
                            }}
                            className="p-1.5 rounded hover:bg-[var(--bg-elevated)] text-[var(--text-secondary)]"
                            title="View Comparison"
                          >
                            <GitCompare size={18} />
                          </button>
                        )}
                        <button
                          onClick={(e) => e.stopPropagation()}
                          className="p-1.5 rounded hover:bg-[var(--bg-elevated)] text-[var(--text-secondary)]"
                        >
                          <MoreHorizontal size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between px-4 py-3 border-t border-[var(--bg-border)]">
          <p className="text-sm text-[var(--text-secondary)]">
            Showing {filteredEcos.length} of {ecos.length} ECOs
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="btn btn-ghost btn-sm"
            >
              <ChevronLeft size={16} />
            </button>
            {[...Array(Math.min(5, totalPages))].map((_, i) => {
              const pageNum = i + 1;
              return (
                <button
                  key={pageNum}
                  onClick={() => setCurrentPage(pageNum)}
                  className={clsx(
                    'btn btn-sm min-w-[2rem]',
                    currentPage === pageNum ? 'btn-primary' : 'btn-ghost'
                  )}
                >
                  {pageNum}
                </button>
              );
            })}
            <button
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
              className="btn btn-ghost btn-sm"
            >
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EcoListPage;
