import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Plus, Eye, Archive, Lock } from 'lucide-react';
import useAuthStore from '../../context/authStore';
import { can } from '../../utils/roleGuards';
import {
  SectionHeader,
  DataTable,
  StatusBadge,
  SearchInput,
  FilterSelect,
} from '../../components/ui';
import { productApi } from '../../api';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const ProductsPage = () => {
  const navigate = useNavigate();
  const { role } = useAuthStore();
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    setIsLoading(true);
    try {
      const response = await productApi.getProducts();
      // Backend returns ProductListResponse with 'products' field
      const productList = Array.isArray(response)
        ? response
        : (response?.products || response?.data || response?.content || []);
      setProducts(Array.isArray(productList) ? productList : []);
    } catch (error) {
      console.error('Failed to load products:', error);
      setProducts([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Filter products (with safety check)
  const filteredProducts = (products || []).filter((product) => {
    const matchesSearch = (product.name || '')
      .toLowerCase()
      .includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' ||
      (product.status || '').toLowerCase() === statusFilter.toLowerCase();
    return matchesSearch && matchesStatus;
  });

  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const columns = [
    {
      key: 'name',
      header: 'Product Name',
      render: (val, row) => (
        <div className="flex items-center gap-2">
          <span className="font-medium text-[var(--text-primary)]">{val}</span>
          {row.status === 'Archived' && (
            <Lock size={14} className="text-[var(--text-muted)]" />
          )}
        </div>
      ),
    },
    {
      key: 'version',
      header: 'Current Version',
      mono: true,
      render: (val) => (
        <span className="font-mono text-sm text-[var(--text-secondary)]">v{val}</span>
      ),
    },
    {
      key: 'salePrice',
      header: 'Sale Price',
      align: 'right',
      render: (val) => formatCurrency(val),
    },
    {
      key: 'costPrice',
      header: 'Cost Price',
      align: 'right',
      render: (val) => formatCurrency(val),
    },
    {
      key: 'status',
      header: 'Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'updatedAt',
      header: 'Last Updated',
      render: (val) => dayjs(val).fromNow(),
    },
    {
      key: 'actions',
      header: 'Actions',
      sortable: false,
      render: (_, row) => (
        <button
          onClick={(e) => {
            e.stopPropagation();
            navigate(`/products/${row.id}`);
          }}
          className="p-1.5 rounded hover:bg-[var(--bg-elevated)] text-[var(--text-secondary)]"
          aria-label="View product"
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
        title="Products"
        count={filteredProducts.length}
        action={
          can(role, 'product.create') && (
            <Link to="/products/new" className="btn btn-primary">
              <Plus size={18} />
              New Product
            </Link>
          )
        }
      />

      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row gap-3 mb-4">
        <SearchInput
          value={search}
          onChange={setSearch}
          placeholder="Search products..."
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
        data={filteredProducts}
        onRowClick={(row) => navigate(`/products/${row.id}`)}
        isLoading={isLoading}
      />
    </div>
  );
};

export default ProductsPage;
