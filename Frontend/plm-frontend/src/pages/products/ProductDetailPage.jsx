import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ChevronRight, Download, FileText, ExternalLink } from 'lucide-react';
import { StatusBadge, DataTable } from '../../components/ui';
import { productApi, fileApi } from '../../api';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';

const ProductDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [versions, setVersions] = useState([]);
  const [relatedEcos, setRelatedEcos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadProductData();
  }, [id]);

  const loadProductData = async () => {
    setIsLoading(true);
    try {
      const [productData, versionsData, ecosData] = await Promise.all([
        productApi.getProductById(id),
        productApi.getProductVersions(id),
        productApi.getProductEcos(id),
      ]);
      setProduct(productData);
      setVersions(versionsData.data || versionsData);
      setRelatedEcos(ecosData.data || ecosData);
    } catch (error) {
      console.error('Failed to load product:', error);
      setProduct(null);
      setVersions([]);
      setRelatedEcos([]);
    } finally {
      setIsLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const handleDownloadAttachment = async (file) => {
    try {
      const response = await fileApi.getFileInfo(file.id);
      const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
      const downloadUrl = `${API_BASE_URL}/files/${file.id}/download`;
      
      const authStore = localStorage.getItem('auth-storage');
      let token = '';
      if (authStore) {
        try {
          const parsed = JSON.parse(authStore);
          token = parsed.state?.token || '';
        } catch(e){}
      }

      const result = await fetch(downloadUrl, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!result.ok) throw new Error('Download failed');
      
      const blob = await result.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = file.originalFileName || file.fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove();
    } catch (error) {
      console.error('Download error:', error);
      toast.error('Failed to download file');
    }
  };

  if (isLoading) {
    return (
      <div className="animate-pulse">
        <div className="h-8 bg-[var(--bg-elevated)] rounded w-1/4 mb-4" />
        <div className="h-64 bg-[var(--bg-elevated)] rounded" />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="text-center py-16">
        <p className="text-[var(--text-secondary)]">Product not found</p>
        <Link to="/products" className="btn btn-primary mt-4">
          Back to Products
        </Link>
      </div>
    );
  }

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/products" className="hover:text-[var(--text-primary)]">
          Products
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">{product.name}</span>
      </nav>

      {/* Header */}
      <div className="flex flex-wrap items-start gap-4 mb-6">
        <div className="flex-1">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-[var(--text-primary)]">
              {product.name}
            </h1>
            <span className="font-mono text-sm px-2 py-1 bg-[var(--bg-elevated)] rounded text-[var(--text-secondary)]">
              v{product.version}
            </span>
            <StatusBadge status={product.status} />
          </div>
        </div>
      </div>

      {/* Two column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Left column - Info Panel */}
        <div className="lg:col-span-3 space-y-6">
          {/* Key-value info */}
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              Product Information
            </h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Sale Price</p>
                <p className="font-medium text-[var(--text-primary)]">
                  {formatCurrency(product.salePrice)}
                </p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Cost Price</p>
                <p className="font-medium text-[var(--text-primary)]">
                  {formatCurrency(product.costPrice)}
                </p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">
                  Current Version
                </p>
                <p className="font-mono text-sm text-[var(--text-primary)]">
                  v{product.version}
                </p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Status</p>
                <StatusBadge status={product.status} />
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Created At</p>
                <p className="text-[var(--text-secondary)]">
                  {dayjs(product.createdAt).format('MMM D, YYYY')}
                </p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Updated At</p>
                <p className="text-[var(--text-secondary)]">
                  {dayjs(product.updatedAt).format('MMM D, YYYY')}
                </p>
              </div>
            </div>
          </div>

          {/* Attachments */}
          {product.attachments && product.attachments.length > 0 && (
            <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
              <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
                Attachments
              </h3>
              <div className="space-y-2">
                {product.attachments.map((file) => (
                  <div
                    key={file.id}
                    className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] rounded"
                  >
                    <div className="flex items-center gap-3 overflow-hidden">
                      <FileText size={20} className="text-[var(--text-muted)] flex-shrink-0" />
                      <div className="overflow-hidden">
                        <p className="text-sm font-medium text-[var(--text-primary)] truncate" title={file.fileName || file.name}>
                          {file.fileName || file.name}
                        </p>
                        <p className="text-xs text-[var(--text-muted)]">
                          {file.fileType}
                        </p>
                      </div>
                    </div>
                    <button 
                      onClick={() => handleDownloadAttachment(file)}
                      className="p-2 hover:bg-[var(--bg-border)] rounded text-[var(--text-secondary)] transition-colors flex-shrink-0"
                      title="Download"
                    >
                      <Download size={18} />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Related ECOs */}
          {relatedEcos.length > 0 && (
            <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
              <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
                Related ECOs
              </h3>
              <DataTable
                columns={[
                  { key: 'title', header: 'Title' },
                  {
                    key: 'type',
                    header: 'Type',
                    render: (val) => <StatusBadge status={val} />,
                  },
                  {
                    key: 'stage',
                    header: 'Stage',
                    render: (val) => <StatusBadge status={val} />,
                  },
                  {
                    key: 'createdAt',
                    header: 'Date',
                    render: (val) => dayjs(val).format('MMM D, YYYY'),
                  },
                  {
                    key: 'actions',
                    header: '',
                    sortable: false,
                    render: (_, row) => (
                      <button
                        onClick={() => navigate(`/eco/${row.id}`)}
                        className="p-1.5 hover:bg-[var(--bg-elevated)] rounded text-[var(--text-secondary)]"
                      >
                        <ExternalLink size={16} />
                      </button>
                    ),
                  },
                ]}
                data={relatedEcos}
                pagination={false}
              />
            </div>
          )}
        </div>

        {/* Right column - Version History */}
        <div className="lg:col-span-2">
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              Version History
            </h3>
            <div className="space-y-3">
              {versions.map((ver, index) => (
                <div
                  key={ver.version}
                  className={`relative p-3 rounded border ${
                    ver.status === 'Active'
                      ? 'bg-[var(--green-dim)] border-[var(--green)]'
                      : 'bg-[var(--bg-elevated)] border-[var(--bg-border)]'
                  }`}
                >
                  {/* Timeline connector */}
                  {index < versions.length - 1 && (
                    <div className="absolute left-6 top-full w-0.5 h-3 bg-[var(--bg-border)]" />
                  )}

                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-medium text-[var(--text-primary)]">
                          v{ver.version}
                        </span>
                        <StatusBadge status={ver.status} size="sm" />
                      </div>
                      <p className="text-sm text-[var(--text-secondary)] mt-1">
                        Applied {dayjs(ver.appliedAt).format('MMM D, YYYY')}
                      </p>
                    </div>
                    {ver.ecoRef && (
                      <Link
                        to={`/eco/${ver.ecoId}`}
                        className="text-xs text-[var(--accent)] hover:underline"
                      >
                        {ver.ecoRef}
                      </Link>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;
