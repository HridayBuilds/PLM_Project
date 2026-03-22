import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  ClipboardList,
  History,
  GitBranch,
  Archive,
  Grid3X3,
  Download,
  ExternalLink,
} from 'lucide-react';
import clsx from 'clsx';
import toast from 'react-hot-toast';
import { DataTable, StatusBadge, FilterSelect, SlideDrawer } from '../../components/ui';
import { ProductComparisonTable, BomComparisonTable } from '../../components/eco';
import { reportApi, productApi } from '../../api';
import dayjs from 'dayjs';

const tabs = [
  { id: 'eco', label: 'ECO Report', icon: ClipboardList },
  { id: 'versions', label: 'Product Version History', icon: History },
  { id: 'bom', label: 'BoM Change History', icon: GitBranch },
  { id: 'archived', label: 'Archived Products', icon: Archive },
  { id: 'matrix', label: 'Product–BoM Matrix', icon: Grid3X3 },
];

const ReportsPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('eco');
  const [data, setData] = useState([]);
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Filters
  const [selectedProduct, setSelectedProduct] = useState('');
  const [dateRange, setDateRange] = useState({ start: '', end: '' });
  const [typeFilter, setTypeFilter] = useState('all');
  const [stageFilter, setStageFilter] = useState('all');

  // Slide drawer for diff view
  const [showDiffDrawer, setShowDiffDrawer] = useState(false);
  const [selectedDiff, setSelectedDiff] = useState(null);

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    loadReportData();
  }, [activeTab, selectedProduct]);

  const loadProducts = async () => {
    try {
      const response = await productApi.getProducts();
      // Backend returns ProductListResponse with 'products' field
      const productList = Array.isArray(response) ? response : (response?.products || response?.data || response?.content || []);
      setProducts(Array.isArray(productList) ? productList : []);
    } catch (error) {
      console.error('Failed to load products:', error);
      setProducts([]);
    }
  };

  const loadReportData = async () => {
    setIsLoading(true);
    try {
      let response;
      switch (activeTab) {
        case 'eco':
          response = await reportApi.getEcoReport();
          break;
        case 'versions':
          if (selectedProduct) {
            response = await reportApi.getProductVersionHistory(selectedProduct);
          }
          break;
        case 'bom':
          if (selectedProduct) {
            response = await reportApi.getBomChangeHistory(selectedProduct);
          }
          break;
        case 'archived':
          response = await reportApi.getArchivedProducts();
          break;
        case 'matrix':
          response = await reportApi.getProductBomMatrix();
          break;
      }
      const reportData = response?.data || response || [];
      if (activeTab === 'versions') {
        let versionsList = [];
        if (Array.isArray(reportData)) {
          versionsList = reportData.flatMap(r => r.versions || []);
        } else if (reportData && reportData.versions) {
          versionsList = reportData.versions;
        }
        setData(versionsList);
      } else {
        setData(Array.isArray(reportData) ? reportData : []);
      }
    } catch (error) {
      console.error('Failed to load report:', error);
      setData([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleViewDiff = (row) => {
    setSelectedDiff({
      type: row.type || row.ecoType || 'Product',
      changes: row.changes || [],
      components: row.components || [],
      operations: row.operations || [],
    });
    setShowDiffDrawer(true);
  };

  const handleExport = () => {
    if (!data || data.length === 0) {
      toast.error('No data to export');
      return;
    }

    try {
      // Define headers based on active tab
      let headers = [];
      let fileName = '';

      switch (activeTab) {
        case 'eco':
          headers = ['Title', 'Type', 'Product', 'Stage', 'Created By', 'Date'];
          fileName = 'eco-report';
          break;
        case 'versions':
          headers = ['Version', 'Status', 'Applied Date', 'ECO Reference', 'Changes Summary'];
          fileName = 'version-history';
          break;
        case 'bom':
          headers = ['Change', 'Old Value', 'New Value', 'ECO', 'Applied By', 'Date'];
          fileName = 'bom-changes';
          break;
        case 'archived':
          headers = ['Product Name', 'Version', 'Archived Date', 'Reason'];
          fileName = 'archived-products';
          break;
        case 'matrix':
          headers = ['Product Name', 'Active Version', 'Active BoM Version', 'Components', 'Last Change'];
          fileName = 'product-bom-matrix';
          break;
        default:
          headers = Object.keys(data[0] || {});
          fileName = 'report';
      }

      // Convert data to CSV
      const csvContent = [
        headers.join(','),
        ...data.map(row => {
          switch (activeTab) {
            case 'eco':
              return [
                `"${row.title || ''}"`,
                `"${row.type || ''}"`,
                `"${row.productName || ''}"`,
                `"${row.stage || ''}"`,
                `"${row.createdBy || ''}"`,
                `"${row.createdAt ? dayjs(row.createdAt).format('YYYY-MM-DD') : ''}"`,
              ].join(',');
            case 'versions':
              return [
                `"v${row.version || ''}"`,
                `"${row.status || ''}"`,
                `"${row.createdAt ? dayjs(row.createdAt).format('YYYY-MM-DD') : ''}"`,
                `"${row.ecoTitle || ''}"`,
                `"${row.description || ''}"`,
              ].join(',');
            case 'bom':
              return [
                `"${row.change || ''}"`,
                `"${row.oldValue || ''}"`,
                `"${row.newValue || ''}"`,
                `"${row.ecoRef || ''}"`,
                `"${row.appliedBy || ''}"`,
                `"${row.appliedAt ? dayjs(row.appliedAt).format('YYYY-MM-DD') : ''}"`,
              ].join(',');
            case 'archived':
              return [
                `"${row.name || ''}"`,
                `"v${row.version || ''}"`,
                `"${row.archivedAt ? dayjs(row.archivedAt).format('YYYY-MM-DD') : ''}"`,
                `"${row.ecoRef || ''}"`,
              ].join(',');
            case 'matrix':
              return [
                `"${row.productName || ''}"`,
                `"v${row.activeVersion || ''}"`,
                `"v${row.activeBomVersion || ''}"`,
                `"${row.componentsCount || ''}"`,
                `"${row.lastChangeDate ? dayjs(row.lastChangeDate).format('YYYY-MM-DD') : ''}"`,
              ].join(',');
            default:
              return Object.values(row).map(v => `"${v || ''}"`).join(',');
          }
        })
      ].join('\n');

      // Create and trigger download
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `${fileName}-${dayjs().format('YYYY-MM-DD')}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      toast.success(`Exported ${data.length} records to CSV`);
    } catch (error) {
      console.error('Export failed:', error);
      toast.error('Failed to export data');
    }
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'eco':
        return (
          <div>
            {/* Filters */}
            <div className="flex flex-wrap gap-3 mb-4">
              <input
                type="date"
                className="input w-40"
                placeholder="Start date"
                value={dateRange.start}
                onChange={(e) => setDateRange((p) => ({ ...p, start: e.target.value }))}
              />
              <input
                type="date"
                className="input w-40"
                placeholder="End date"
                value={dateRange.end}
                onChange={(e) => setDateRange((p) => ({ ...p, end: e.target.value }))}
              />
              <FilterSelect
                value={typeFilter}
                onChange={setTypeFilter}
                options={[
                  { value: 'all', label: 'All Types' },
                  { value: 'product', label: 'Product' },
                  { value: 'bom', label: 'BoM' },
                ]}
                className="w-36"
              />
              <FilterSelect
                value={stageFilter}
                onChange={setStageFilter}
                options={[
                  { value: 'all', label: 'All Stages' },
                  { value: 'new', label: 'New' },
                  { value: 'approval', label: 'Approval' },
                  { value: 'done', label: 'Done' },
                ]}
                className="w-36"
              />
              <button onClick={handleExport} className="btn btn-secondary ml-auto">
                <Download size={18} />
                Export
              </button>
            </div>

            <DataTable
              columns={[
                { key: 'title', header: 'ECO Title' },
                { key: 'ecoType', header: 'Type', render: (v) => <StatusBadge status={v} /> },
                { key: 'productName', header: 'Product' },
                { key: 'stageName', header: 'Stage', render: (v) => <StatusBadge status={v} /> },
                { key: 'createdByName', header: 'Created By' },
                { key: 'createdAt', header: 'Date', render: (v) => v ? dayjs(v).format('MMM D, YYYY') : 'N/A' },
                {
                  key: 'actions',
                  header: 'Changes',
                  sortable: false,
                  render: (_, row) => (
                    <button
                      onClick={() => handleViewDiff(row)}
                      className="text-[var(--accent)] hover:underline text-sm"
                    >
                      View Diff
                    </button>
                  ),
                },
              ]}
              data={data}
              isLoading={isLoading}
            />
          </div>
        );

      case 'versions':
        return (
          <div>
            <div className="mb-4">
              <FilterSelect
                value={selectedProduct}
                onChange={setSelectedProduct}
                options={(products || []).map((p) => ({ value: String(p.id), label: p.name || p.productName || 'Unknown' }))}
                placeholder="Select a product"
                className="w-64"
              />
            </div>

            {selectedProduct ? (
              <DataTable
                columns={[
                  { key: 'version', header: 'Version', mono: true, render: (v) => v ? `v${v}` : 'N/A' },
                  { key: 'status', header: 'Status', render: (v) => <StatusBadge status={v} /> },
                  { key: 'createdAt', header: 'Applied Date', render: (v) => v ? dayjs(v).format('MMM D, YYYY') : 'N/A' },
                  {
                    key: 'ecoTitle',
                    header: 'ECO Reference',
                    render: (v, row) => (
                      <Link to={`/eco/${row.ecoId}`} className="text-[var(--accent)] hover:underline">
                        {v || 'N/A'}
                      </Link>
                    ),
                  },
                  { key: 'description', header: 'Changes Summary' },
                ]}
                data={data}
                isLoading={isLoading}
              />
            ) : (
              <p className="text-center py-8 text-[var(--text-muted)]">
                Select a product to view version history
              </p>
            )}
          </div>
        );

      case 'bom':
        return (
          <div>
            <div className="mb-4">
              <FilterSelect
                value={selectedProduct}
                onChange={setSelectedProduct}
                options={(products || []).map((p) => ({ value: String(p.id), label: p.name || p.productName || 'Unknown' }))}
                placeholder="Select a product"
                className="w-64"
              />
            </div>

            {selectedProduct ? (
              <DataTable
                columns={[
                  { key: 'change', header: 'Change' },
                  { key: 'oldValue', header: 'Old Value' },
                  { key: 'newValue', header: 'New Value' },
                  {
                    key: 'ecoRef',
                    header: 'ECO',
                    render: (v, row) => (
                      <Link to={`/eco/${row.ecoId}`} className="text-[var(--accent)] hover:underline">
                        {v}
                      </Link>
                    ),
                  },
                  { key: 'appliedBy', header: 'Applied By' },
                  { key: 'appliedAt', header: 'Date', render: (v) => v ? dayjs(v).format('MMM D, YYYY') : 'N/A' },
                ]}
                data={data}
                isLoading={isLoading}
              />
            ) : (
              <p className="text-center py-8 text-[var(--text-muted)]">
                Select a product to view BoM change history
              </p>
            )}
          </div>
        );

      case 'archived':
        return (
          <DataTable
            columns={[
              { key: 'name', header: 'Product Name' },
              { key: 'version', header: 'Version', mono: true, render: (v) => v ? `v${v}` : 'N/A' },
              { key: 'archivedAt', header: 'Archived Date', render: (v) => v ? dayjs(v).format('MMM D, YYYY') : 'N/A' },
              {
                key: 'ecoRef',
                header: 'Archived Reason',
                render: (v, row) => (
                  <Link to={`/eco/${row.ecoId}`} className="text-[var(--accent)] hover:underline">
                    {v}
                  </Link>
                ),
              },
              {
                key: 'actions',
                header: '',
                sortable: false,
                render: (_, row) => (
                  <button
                    onClick={() => navigate(`/products/${row.id}`)}
                    className="p-1.5 hover:bg-[var(--bg-elevated)] rounded text-[var(--text-secondary)]"
                  >
                    <ExternalLink size={16} />
                  </button>
                ),
              },
            ]}
            data={data}
            isLoading={isLoading}
          />
        );

      case 'matrix':
        return (
          <DataTable
            columns={[
              { key: 'productName', header: 'Product Name' },
              { key: 'activeVersion', header: 'Active Version', mono: true, render: (v) => v ? `v${v}` : 'N/A' },
              { key: 'activeBomVersion', header: 'Active BoM Version', mono: true, render: (v) => v ? `v${v}` : 'N/A' },
              { key: 'componentsCount', header: 'Components', align: 'center' },
              { key: 'lastChangeDate', header: 'Last Change', render: (v) => v ? dayjs(v).format('MMM D, YYYY') : 'N/A' },
            ]}
            data={data}
            isLoading={isLoading}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-[var(--text-primary)] mb-6">Reports</h1>

      {/* Tab navigation */}
      <div className="flex gap-1 p-1 bg-[var(--bg-elevated)] rounded-lg mb-6 overflow-x-auto">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded text-sm font-medium whitespace-nowrap transition-colors',
              activeTab === tab.id
                ? 'bg-[var(--accent)] text-white'
                : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
            )}
          >
            <tab.icon size={16} />
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {renderTabContent()}

      {/* Diff Drawer */}
      <SlideDrawer
        isOpen={showDiffDrawer}
        onClose={() => setShowDiffDrawer(false)}
        title="Change Comparison"
        width="lg"
      >
        {selectedDiff && (
          selectedDiff.type === 'Product' ? (
            <ProductComparisonTable changes={selectedDiff.changes} />
          ) : (
            <BomComparisonTable
              components={selectedDiff.components}
              operations={selectedDiff.operations}
            />
          )
        )}
      </SlideDrawer>
    </div>
  );
};

export default ReportsPage;
