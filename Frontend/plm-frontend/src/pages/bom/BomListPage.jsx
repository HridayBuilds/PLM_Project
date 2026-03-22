import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, Plus } from 'lucide-react';
import useAuthStore from '../../context/authStore';
import { can } from '../../utils/roleGuards';
import {
  SectionHeader,
  DataTable,
  StatusBadge,
  SearchInput,
  FilterSelect,
} from '../../components/ui';
import { bomApi } from '../../api';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const BomListPage = () => {
  const navigate = useNavigate();
  const { role } = useAuthStore();
  const [boms, setBoms] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    loadBoms();
  }, []);

  const loadBoms = async () => {
    setIsLoading(true);
    try {
      const response = await bomApi.getBoms();
      // Backend returns BomListResponse with 'boms' field
      const bomList = Array.isArray(response)
        ? response
        : (response?.boms || response?.data || response?.content || []);
      setBoms(Array.isArray(bomList) ? bomList : []);
    } catch (error) {
      console.error('Failed to load BOMs:', error);
      setBoms([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Filter BOMs (with safety check)
  const filteredBoms = (boms || []).filter((bom) => {
    const matchesSearch = (bom.productName || bom.product?.name || '')
      .toLowerCase()
      .includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' ||
      (bom.status || '').toLowerCase() === statusFilter.toLowerCase();
    return matchesSearch && matchesStatus;
  });

  const columns = [
    {
      key: 'productName',
      header: 'Finished Product',
      render: (val, row) => (
        <Link
          to={`/products/${row.productId}`}
          className="text-[var(--accent)] hover:underline"
          onClick={(e) => e.stopPropagation()}
        >
          {val}
        </Link>
      ),
    },
    {
      key: 'reference',
      header: 'Reference',
      mono: true,
      render: (val) => (
        <span className="font-mono text-sm text-[var(--text-primary)]">
          {val}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'actions',
      header: 'Actions',
      sortable: false,
      render: (_, row) => (
        <button
          onClick={(e) => {
            e.stopPropagation();
            navigate(`/bom/${row.id}`);
          }}
          className="p-1.5 rounded hover:bg-[var(--bg-elevated)] text-[var(--text-secondary)]"
          aria-label="View BOM"
        >
          <Eye size={18} />
        </button>
      ),
    },
  ];

  return (
    <div>
      {/* Header */}
      <SectionHeader
        title="Bill of Materials"
        count={filteredBoms.length}
        action={
          can(role, 'bom.create') && (
            <Link to="/bom/new" className="btn btn-primary">
              <Plus size={18} />
              New BoM
            </Link>
          )
        }
      />

      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row gap-3 mb-4">
        <SearchInput
          value={search}
          onChange={setSearch}
          placeholder="Search by product..."
          className="w-full sm:w-72"
        />
        <FilterSelect
          value={statusFilter}
          onChange={setStatusFilter}
          options={[
            { value: 'all', label: 'All Status' },
            { value: 'active', label: 'Active' },
            { value: 'archived', label: 'Archived' },
          ]}
          className="w-full sm:w-40"
        />
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredBoms}
        onRowClick={(row) => navigate(`/bom/${row.id}`)}
        isLoading={isLoading}
      />
    </div>
  );
};

export default BomListPage;
