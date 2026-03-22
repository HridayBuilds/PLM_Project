import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ChevronRight, ExternalLink, FileText, Download } from 'lucide-react';
import { StatusBadge, DataTable } from '../../components/ui';
import { bomApi, productApi, fileApi } from '../../api';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';
import clsx from 'clsx';

const BomDetailPage = () => {
  const { id } = useParams();
  const [bom, setBom] = useState(null);
  const [components, setComponents] = useState([]);
  const [operations, setOperations] = useState([]);
  const [activeTab, setActiveTab] = useState('components');
  const [isLoading, setIsLoading] = useState(true);

  // Edit states
  const [products, setProducts] = useState([]);
  const [showCompForm, setShowCompForm] = useState(false);
  const [showOpForm, setShowOpForm] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [compForm, setCompForm] = useState({ componentProductId: '', quantity: '', unit: 'pcs' });
  const [opForm, setOpForm] = useState({ name: '', workCenter: '', expectedDurationMinutes: '', sequence: '' });

  useEffect(() => {
    loadBomData();
    fetchProducts();
  }, [id]);

  const fetchProducts = async () => {
    try {
      const response = await productApi.getProducts({ status: 'ACTIVE' });
      const list = Array.isArray(response) ? response : (response?.products || response?.data || response?.content || []);
      setProducts(Array.isArray(list) ? list : []);
    } catch (err) {
      console.error('Failed to load products');
    }
  };

  const loadBomData = async () => {
    setIsLoading(true);
    try {
      const bomData = await bomApi.getBomById(id);
      setBom(bomData);
      setComponents(bomData.components || []);
      setOperations(bomData.operations || []);
    } catch (error) {
      console.error('Failed to load BOM:', error);
      toast.error('Failed to load BOM');
    } finally {
      setIsLoading(false);
    }
  };

  const saveBomChanges = async (newComponents, newOperations) => {
    setIsSaving(true);
    try {
      await bomApi.updateBom(bom.id, {
        productId: bom.productId,
        reference: bom.reference,
        quantity: bom.quantity,
        components: newComponents.map(c => ({
          componentProductId: c.componentProductId,
          quantity: c.quantity,
          unit: c.unit
        })),
        operations: newOperations.map(o => ({
          name: o.name,
          workCenter: o.workCenter,
          expectedDurationMinutes: o.expectedDurationMinutes || o.duration,
          sequence: o.sequence || 1
        }))
      });
      toast.success('BOM updated successfully');
      loadBomData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update BOM');
    } finally {
      setIsSaving(false);
    }
  };

  const handleAddComponent = () => {
    if (!compForm.componentProductId || !compForm.quantity) return;
    const newComps = [...components, { ...compForm, quantity: parseFloat(compForm.quantity) }];
    saveBomChanges(newComps, operations);
    setShowCompForm(false);
    setCompForm({ componentProductId: '', quantity: '', unit: 'pcs' });
  };

  const handleRemoveComponent = (idx) => {
    const newComps = components.filter((_, i) => i !== idx);
    saveBomChanges(newComps, operations);
  };

  const handleAddOperation = () => {
    if (!opForm.name || !opForm.workCenter) return;
    const newOps = [...operations, { ...opForm, expectedDurationMinutes: parseInt(opForm.expectedDurationMinutes) || 0 }];
    saveBomChanges(components, newOps);
    setShowOpForm(false);
    setOpForm({ name: '', workCenter: '', expectedDurationMinutes: '', sequence: '' });
  };

  const handleRemoveOperation = (idx) => {
    const newOps = operations.filter((_, i) => i !== idx);
    saveBomChanges(components, newOps);
  };

  const handleDownloadAttachment = async (file) => {
    try {
      const response = await fileApi.getFileInfo(file.id);
      // Since we don't have a direct downloadFile method in fileApi yet, 
      // let's create a link and click it to download
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

      // We need to fetch it to pass the Authorization header
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

  if (!bom) {
    return (
      <div className="text-center py-16">
        <p className="text-[var(--text-secondary)]">BOM not found</p>
        <Link to="/bom" className="btn btn-primary mt-4">
          Back to BOMs
        </Link>
      </div>
    );
  }

  const canEdit = bom.status === 'DRAFT';

  const componentColumns = [
    { key: 'componentProductName', header: 'Component Name' },
    { key: 'quantity', header: 'Quantity', align: 'center' },
    { key: 'unit', header: 'Unit' },
  ];
  if (canEdit) {
    componentColumns.push({
      key: 'actions', header: '', align: 'right', render: (val, row, idx) => (
        <button onClick={() => handleRemoveComponent(idx)} disabled={isSaving} className="text-[var(--red)] hover:text-red-400 text-sm">✕</button>
      )
    });
  }

  const operationColumns = [
    { key: 'name', header: 'Operation Name' },
    { key: 'workCenter', header: 'Work Center' },
    {
      key: 'expectedDurationMinutes',
      header: 'Duration',
      align: 'center',
      render: (val) => `${val || 0} mins`,
    },
    { key: 'sequence', header: 'Sequence', align: 'center' },
  ];
  if (canEdit) {
    operationColumns.push({
      key: 'actions', header: '', align: 'right', render: (val, row, idx) => (
        <button onClick={() => handleRemoveOperation(idx)} disabled={isSaving} className="text-[var(--red)] hover:text-red-400 text-sm">✕</button>
      )
    });
  }

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/bom" className="hover:text-[var(--text-primary)]">
          Bill of Materials
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">{bom.productName}</span>
      </nav>

      {/* Header */}
      <div className="flex flex-wrap items-start gap-4 mb-6">
        <div className="flex-1">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-[var(--text-primary)]">
              {bom.productName}
            </h1>
            <span className="font-mono text-sm px-2 py-1 bg-[var(--bg-elevated)] rounded text-[var(--text-secondary)]">
              BoM v{bom.version}
            </span>
            <StatusBadge status={bom.status} />
          </div>
        </div>
      </div>

      {/* Two column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Left - Tabs with tables */}
        <div className="lg:col-span-3">
          <div className="flex items-center justify-between mb-4">
            {/* Tabs */}
            <div className="flex gap-1 p-1 bg-[var(--bg-elevated)] rounded-lg w-fit">
              <button
                onClick={() => setActiveTab('components')}
                className={clsx(
                  'px-4 py-2 rounded text-sm font-medium transition-colors',
                  activeTab === 'components'
                    ? 'bg-[var(--accent)] text-white'
                    : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                )}
              >
                Components ({components.length})
              </button>
              <button
                onClick={() => setActiveTab('operations')}
                className={clsx(
                  'px-4 py-2 rounded text-sm font-medium transition-colors',
                  activeTab === 'operations'
                    ? 'bg-[var(--accent)] text-white'
                    : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                )}
              >
                Operations ({operations.length})
              </button>
            </div>

            {/* Action buttons */}
            {canEdit && (
              <div>
                {activeTab === 'components' && !showCompForm && (
                  <button onClick={() => setShowCompForm(true)} className="btn btn-primary" style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}>+ Add Component</button>
                )}
                {activeTab === 'operations' && !showOpForm && (
                  <button onClick={() => setShowOpForm(true)} className="btn btn-primary" style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}>+ Add Operation</button>
                )}
              </div>
            )}
          </div>

          {/* Component Form */}
          {activeTab === 'components' && showCompForm && canEdit && (
            <div className="mb-4 p-4 bg-[var(--bg-elevated)] border border-[var(--bg-border)] rounded-lg">
              <h4 className="text-sm font-semibold text-[var(--text-primary)] mb-3">Add Component</h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                <div>
                  <label className="block text-xs text-[var(--text-secondary)] mb-1">Product</label>
                  <select
                    className="input py-1.5 px-3 text-sm"
                    value={compForm.componentProductId}
                    onChange={(e) => setCompForm(prev => ({...prev, componentProductId: e.target.value}))}
                  >
                    <option value="">Select product...</option>
                    {products.map(p => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs text-[var(--text-secondary)] mb-1">Quantity</label>
                  <input
                    type="number" className="input py-1.5 px-3 text-sm" placeholder="e.g. 5"
                    value={compForm.quantity}
                    onChange={(e) => setCompForm(prev => ({...prev, quantity: e.target.value}))}
                  />
                </div>
                <div>
                  <label className="block text-xs text-[var(--text-secondary)] mb-1">Unit</label>
                  <select
                    className="input py-1.5 px-3 text-sm"
                    value={compForm.unit}
                    onChange={(e) => setCompForm(prev => ({...prev, unit: e.target.value}))}
                  >
                    <option value="pcs">pcs</option>
                    <option value="kg">kg</option>
                    <option value="m">m</option>
                    <option value="l">l</option>
                  </select>
                </div>
              </div>
              <div className="flex gap-2">
                <button onClick={handleAddComponent} disabled={isSaving || !compForm.componentProductId || !compForm.quantity} className="btn btn-primary py-1.5 px-3 text-sm">Save Component</button>
                <button onClick={() => setShowCompForm(false)} className="btn btn-secondary py-1.5 px-3 text-sm">Cancel</button>
              </div>
            </div>
          )}

          {/* Operation Form */}
          {activeTab === 'operations' && showOpForm && canEdit && (
            <div className="mb-4 p-4 bg-[var(--bg-elevated)] border border-[var(--bg-border)] rounded-lg">
              <h4 className="text-sm font-semibold text-[var(--text-primary)] mb-3">Add Operation</h4>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-3">
                <div className="md:col-span-2">
                  <label className="block text-xs text-[var(--text-secondary)] mb-1">Operation Name</label>
                  <input
                    type="text" className="input py-1.5 px-3 text-sm" placeholder="e.g. Assembly"
                    value={opForm.name}
                    onChange={(e) => setOpForm(prev => ({...prev, name: e.target.value}))}
                  />
                </div>
                <div>
                  <label className="block text-xs text-[var(--text-secondary)] mb-1">Work Center</label>
                  <input
                    type="text" className="input py-1.5 px-3 text-sm" placeholder="e.g. Station 1"
                    value={opForm.workCenter}
                    onChange={(e) => setOpForm(prev => ({...prev, workCenter: e.target.value}))}
                  />
                </div>
                <div>
                  <label className="block text-xs text-[var(--text-secondary)] mb-1">Duration (mins)</label>
                  <input
                    type="number" className="input py-1.5 px-3 text-sm" placeholder="15"
                    value={opForm.expectedDurationMinutes}
                    onChange={(e) => setOpForm(prev => ({...prev, expectedDurationMinutes: e.target.value}))}
                  />
                </div>
              </div>
              <div className="flex gap-2">
                <button onClick={handleAddOperation} disabled={isSaving || !opForm.name || !opForm.workCenter} className="btn btn-primary py-1.5 px-3 text-sm">Save Operation</button>
                <button onClick={() => setShowOpForm(false)} className="btn btn-secondary py-1.5 px-3 text-sm">Cancel</button>
              </div>
            </div>
          )}

          {/* Table content */}
          {activeTab === 'components' ? (
            <DataTable columns={componentColumns} data={components} />
          ) : (
            <DataTable columns={operationColumns} data={operations} />
          )}
        </div>

        {/* Right - Metadata */}
        <div className="lg:col-span-1">
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              BoM Details
            </h3>
            <div className="space-y-4">
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Product</p>
                <Link
                  to={`/products/${bom.productId}`}
                  className="text-[var(--accent)] hover:underline flex items-center gap-1"
                >
                  {bom.productName}
                  <ExternalLink size={14} />
                </Link>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Quantity</p>
                <p className="font-mono text-sm text-[var(--text-primary)]">
                  {bom.quantity || 1}
                </p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Version</p>
                <p className="font-mono text-sm text-[var(--text-primary)]">
                  v{bom.version}
                </p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Status</p>
                <StatusBadge status={bom.status} />
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Created By</p>
                <p className="text-[var(--text-primary)]">{bom.createdBy}</p>
              </div>
              <div>
                <p className="text-sm text-[var(--text-muted)] mb-1">Created At</p>
                <p className="text-[var(--text-secondary)]">
                  {dayjs(bom.createdAt).format('MMM D, YYYY')}
                </p>
              </div>
              {bom.ecoRef && (
                <div>
                  <p className="text-sm text-[var(--text-muted)] mb-1">
                    ECO Reference
                  </p>
                  <Link
                    to={`/eco/${bom.ecoId}`}
                    className="text-[var(--accent)] hover:underline"
                  >
                    {bom.ecoRef}
                  </Link>
                </div>
              )}
            </div>
          </div>

          {/* Attachments */}
          {bom.attachments && bom.attachments.length > 0 && (
            <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5 mt-6">
              <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
                Attachments
              </h3>
              <div className="space-y-2">
                {bom.attachments.map((file) => (
                  <div
                    key={file.id}
                    className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] border border-[var(--bg-border)] rounded"
                  >
                    <div className="flex items-center gap-3 overflow-hidden">
                      <FileText size={20} className="text-[var(--text-muted)] flex-shrink-0" />
                      <div className="overflow-hidden">
                        <p className="text-sm font-medium text-[var(--text-primary)] truncate" title={file.fileName || file.name}>
                          {file.fileName || file.name}
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
        </div>
      </div>
    </div>
  );
};

export default BomDetailPage;
