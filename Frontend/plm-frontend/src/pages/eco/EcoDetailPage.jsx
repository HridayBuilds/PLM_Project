import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  ChevronRight,
  Calendar,
  User,
  Package,
  ExternalLink,
  Check,
  X,
  Send,
  GitCompare,
} from 'lucide-react';
import toast from 'react-hot-toast';
import clsx from 'clsx';
import useAuthStore from '../../context/authStore';
import { can, ROLES } from '../../utils/roleGuards';
import { StatusBadge, ConfirmModal } from '../../components/ui';
import {
  StageProgressBar,
  BomComparisonTable,
  ProductComparisonTable,
  ActivityLog,
} from '../../components/eco';
import { ecoApi, stageApi, reportApi } from '../../api';
import dayjs from 'dayjs';

const EcoDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role, user } = useAuthStore();

  const [eco, setEco] = useState(null);
  const [stages, setStages] = useState([]);
  const [comparison, setComparison] = useState(null);
  const [activities, setActivities] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('proposed');

  // Modal states
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Edit states
  const [showAddForm, setShowAddForm] = useState(false);
  const [addingChange, setAddingChange] = useState(false);
  const [changeForm, setChangeForm] = useState({
    fieldName: '',
    newValue: '',
    changeType: 'MODIFIED',
    newQuantity: '',
    unit: 'pcs',
    componentProductId: '',
    bomComponentId: '',
    changeCategory: 'COMPONENT', // COMPONENT or OPERATION
    operationName: '',
    workCenter: '',
    expectedDurationMinutes: '',
    sequence: 1,
  });

  const [uploadingAttachment, setUploadingAttachment] = useState(false);

  useEffect(() => {
    loadEcoData();
  }, [id]);

  const loadEcoData = async () => {
    setIsLoading(true);
    try {
      const [ecoData, stagesData] = await Promise.all([
        ecoApi.getEcoById(id),
        stageApi.getStages(),
      ]);
      setEco(ecoData);
      setStages(stagesData.data || stagesData || []);

      // Load comparison and activity in separate try-catch
      try {
        const comparisonData = await ecoApi.getComparison(id);
        setComparison(comparisonData);
      } catch (e) {
        console.log('Comparison not available');
      }

      try {
        const activityData = await reportApi.getEcoAuditLogs(id);
        setActivities(activityData.data || activityData || []);
      } catch (e) {
        console.log('Activity logs not available');
      }
    } catch (error) {
      console.error('Failed to load ECO:', error);
      toast.error('Failed to load ECO details');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmitForReview = async () => {
    setIsSubmitting(true);
    try {
      await ecoApi.submitEco(id);
      toast.success('ECO submitted for review');
      loadEcoData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to submit ECO');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleApprove = async () => {
    setIsSubmitting(true);
    try {
      await ecoApi.approveEco(id);
      toast.success('ECO approved successfully');
      setShowApproveModal(false);
      loadEcoData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to approve ECO');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async () => {
    setIsSubmitting(true);
    try {
      await ecoApi.rejectEco(id, rejectReason);
      toast.success('ECO rejected');
      setShowRejectModal(false);
      loadEcoData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to reject ECO');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleAddProductChange = async () => {
    if (!changeForm.fieldName || !changeForm.newValue) return;
    setAddingChange(true);
    try {
      await ecoApi.addProductChange(id, {
        fieldName: changeForm.fieldName,
        newValue: changeForm.newValue
      });
      toast.success('Product change added');
      setShowAddForm(false);
      setChangeForm(prev => ({ ...prev, fieldName: '', newValue: '' }));
      loadEcoData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add change');
    } finally {
      setAddingChange(false);
    }
  };

  const handleAddBomChange = async () => {
    setAddingChange(true);
    try {
      if (changeForm.changeCategory === 'COMPONENT') {
        await ecoApi.addBomChange(id, {
          changeType: changeForm.changeType,
          newQuantity: parseFloat(changeForm.newQuantity) || 0,
          unit: changeForm.unit,
          componentProductId: changeForm.componentProductId || null,
          bomComponentId: changeForm.bomComponentId || null
        });
        toast.success('BOM Component change added');
      } else {
        await ecoApi.addBomOperationChange(id, {
          changeType: changeForm.changeType,
          operationName: changeForm.operationName,
          workCenter: changeForm.workCenter,
          expectedDurationMinutes: parseInt(changeForm.expectedDurationMinutes) || 0,
          sequence: parseInt(changeForm.sequence) || 1,
          bomOperationId: null
        });
        toast.success('BOM Operation change added');
      }
      setShowAddForm(false);
      resetChangeForm();
      loadEcoData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add BOM change');
    } finally {
      setAddingChange(false);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploadingAttachment(true);
    try {
      await ecoApi.addAttachment(id, file);
      toast.success('Attachment uploaded successfully');
      loadEcoData();
    } catch (error) {
      toast.error('Failed to upload attachment');
    } finally {
      setUploadingAttachment(false);
      e.target.value = ''; // Reset input
    }
  };

  const handleRemoveAttachment = async (attachmentId) => {
    if (!window.confirm('Remove this attachment?')) return;
    try {
      await ecoApi.removeAttachment(id, attachmentId);
      toast.success('Attachment removed');
      loadEcoData();
    } catch (error) {
      toast.error('Failed to remove attachment');
    }
  };

  const resetChangeForm = () => {
    setChangeForm({ fieldName: '', newValue: '', changeType: 'MODIFIED', newQuantity: '', unit: 'pcs', componentProductId: '', bomComponentId: '', changeCategory: 'COMPONENT', operationName: '', workCenter: '', expectedDurationMinutes: '', sequence: 1 });
  };

  const handleRemoveChange = async (changeId) => {
    if (!window.confirm('Remove this proposed change?')) return;
    try {
      await ecoApi.removeChange(id, changeId);
      toast.success('Change removed');
      loadEcoData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to remove change');
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount);
  };

  if (isLoading) {
    return (
      <div className="animate-pulse">
        <div className="h-8 bg-[var(--bg-elevated)] rounded w-1/4 mb-4" />
        <div className="h-64 bg-[var(--bg-elevated)] rounded" />
      </div>
    );
  }

  if (!eco) {
    return (
      <div className="text-center py-16">
        <p className="text-[var(--text-secondary)]">ECO not found</p>
        <Link to="/eco" className="btn btn-primary mt-4">
          Back to ECOs
        </Link>
      </div>
    );
  }

  const isOwner = eco.createdById === user?.id;
  const isDraft = eco.status === 'DRAFT';
  const isInProgress = eco.status === 'IN_PROGRESS';
  const isApplied = eco.status === 'APPLIED' || eco.status === 'APPROVED';
  const hasApproved = eco?.approvals?.some(a => a.approverId === user?.id);
  const canApproveEco = can(role, 'eco.approve') && isInProgress && !hasApproved;
  const canSubmit = isOwner && isDraft;
  const canEdit = isOwner && isDraft;

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/eco" className="hover:text-[var(--text-primary)]">
          Change Orders
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">{eco.title}</span>
      </nav>

      {/* Header */}
      <div className="flex flex-wrap items-start gap-4 mb-6">
        <div className="flex-1">
          <div className="flex flex-wrap items-center gap-3 mb-2">
            <h1 className="text-2xl font-bold text-[var(--text-primary)]">
              {eco.title}
            </h1>
            <StatusBadge status={eco.ecoType || eco.type} />
            <StatusBadge status={eco.currentStageName || eco.stage || eco.status} />
          </div>
          <div className="flex flex-wrap items-center gap-4 text-sm text-[var(--text-secondary)]">
            <span className="flex items-center gap-1">
              <Calendar size={14} />
              {eco.effectiveDate ? dayjs(eco.effectiveDate).format('MMM D, YYYY') : 'Not set'}
            </span>
            <span className="flex items-center gap-1">
              <Package size={14} />
              <Link
                to={`/products/${eco.productId}`}
                className="text-[var(--accent)] hover:underline"
              >
                {eco.productName}
              </Link>
            </span>
            <span className="flex items-center gap-1">
              <User size={14} />
              {eco.createdByName || eco.createdBy}
            </span>
            <span>
              Version Update:{' '}
              <StatusBadge status={eco.versionUpdate ? 'yes' : 'no'} size="sm" />
            </span>
          </div>
        </div>
      </div>

      {/* Stage Progress Bar */}
      <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-6 mb-6">
        <StageProgressBar stages={stages} currentStage={eco.currentStageName || eco.stage} />
      </div>

      {/* Two column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left - Changes Panel */}
        <div className="lg:col-span-2">
          {/* Tabs */}
          <div className="flex gap-1 p-1 bg-[var(--bg-elevated)] rounded-lg mb-4 w-fit">
            <button
              onClick={() => setActiveTab('proposed')}
              className={clsx(
                'px-4 py-2 rounded text-sm font-medium transition-colors',
                activeTab === 'proposed'
                  ? 'bg-[var(--accent)] text-white'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              )}
            >
              Proposed Changes
            </button>
            <button
              onClick={() => setActiveTab('comparison')}
              className={clsx(
                'px-4 py-2 rounded text-sm font-medium transition-colors',
                activeTab === 'comparison'
                  ? 'bg-[var(--accent)] text-white'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              )}
            >
              Comparison View
            </button>
            <Link
              to={`/eco/${id}/comparison`}
              className="px-4 py-2 rounded text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] flex items-center gap-1.5"
            >
              <GitCompare size={16} />
              Full Comparison
            </Link>
          </div>

           {/* Content */}
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-6">
            {activeTab === 'proposed' ? (
              <div>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-[var(--text-primary)]">
                    Proposed Changes
                  </h3>
                  {canEdit && !showAddForm && (
                    <button
                      onClick={() => setShowAddForm(true)}
                      className="btn btn-primary"
                      style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                    >
                      + Add Change
                    </button>
                  )}
                </div>

                {/* Add Change Form */}
                {showAddForm && canEdit && (
                  <div className="mb-6 p-4 bg-[var(--bg-elevated)] rounded-lg border border-[var(--bg-border)]">
                    <h4 className="text-sm font-semibold text-[var(--text-primary)] mb-3">
                      {eco.ecoType === 'PRODUCT' ? 'Add Product Change' : 'Add BOM Change'}
                    </h4>

                    {eco.ecoType === 'PRODUCT' ? (
                      /* Product Change Form */
                      <div className="space-y-3">
                        <div>
                          <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                            Field to Change
                          </label>
                          <select
                            value={changeForm.fieldName}
                            onChange={(e) => setChangeForm(prev => ({ ...prev, fieldName: e.target.value }))}
                            className="input"
                            style={{ fontSize: '0.875rem' }}
                          >
                            <option value="">Select a field...</option>
                            <option value="salePrice">Sale Price</option>
                            <option value="costPrice">Cost Price</option>
                            <option value="name">Product Name</option>
                            <option value="description">Description</option>
                          </select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                            New Value
                          </label>
                          <input
                            type="text"
                            value={changeForm.newValue}
                            onChange={(e) => setChangeForm(prev => ({ ...prev, newValue: e.target.value }))}
                            className="input"
                            placeholder="Enter new value..."
                            style={{ fontSize: '0.875rem' }}
                          />
                        </div>
                        <div className="flex gap-2 pt-1">
                          <button
                            onClick={handleAddProductChange}
                            disabled={!changeForm.fieldName || !changeForm.newValue || addingChange}
                            className="btn btn-primary"
                            style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                          >
                            {addingChange ? 'Adding...' : 'Add Change'}
                          </button>
                          <button
                            onClick={() => { setShowAddForm(false); resetChangeForm(); }}
                            className="btn btn-secondary"
                            style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    ) : (
                      /* BOM Change Form */
                      <div className="space-y-3">
                        <div className="flex gap-4 mb-2">
                          <label className="flex items-center gap-2 cursor-pointer text-sm">
                            <input
                              type="radio"
                              name="changeCategory"
                              value="COMPONENT"
                              checked={changeForm.changeCategory === 'COMPONENT'}
                              onChange={() => setChangeForm(prev => ({ ...prev, changeCategory: 'COMPONENT' }))}
                              className="accent-[var(--accent)]"
                            />
                            Component
                          </label>
                          <label className="flex items-center gap-2 cursor-pointer text-sm">
                            <input
                              type="radio"
                              name="changeCategory"
                              value="OPERATION"
                              checked={changeForm.changeCategory === 'OPERATION'}
                              onChange={() => setChangeForm(prev => ({ ...prev, changeCategory: 'OPERATION' }))}
                              className="accent-[var(--accent)]"
                            />
                            Operation Routing
                          </label>
                        </div>

                        <div>
                          <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                            Change Type
                          </label>
                          <select
                            value={changeForm.changeType}
                            onChange={(e) => setChangeForm(prev => ({ ...prev, changeType: e.target.value }))}
                            className="input"
                            style={{ fontSize: '0.875rem' }}
                          >
                            <option value="MODIFIED">Modify Existing</option>
                            <option value="ADDED">Add New</option>
                            <option value="REMOVED">Remove completely</option>
                          </select>
                        </div>

                        {changeForm.changeCategory === 'COMPONENT' && changeForm.changeType !== 'REMOVED' && (
                          <>
                            <div>
                              <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                                New Quantity
                              </label>
                              <input
                                type="number"
                                value={changeForm.newQuantity}
                                onChange={(e) => setChangeForm(prev => ({ ...prev, newQuantity: e.target.value }))}
                                className="input"
                                placeholder="Enter quantity..."
                                min="0"
                                step="0.01"
                                style={{ fontSize: '0.875rem' }}
                              />
                            </div>
                            <div>
                              <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                                Unit
                              </label>
                              <select
                                value={changeForm.unit}
                                onChange={(e) => setChangeForm(prev => ({ ...prev, unit: e.target.value }))}
                                className="input"
                                style={{ fontSize: '0.875rem' }}
                              >
                                <option value="pcs">Pieces (pcs)</option>
                                <option value="kg">Kilograms (kg)</option>
                                <option value="m">Meters (m)</option>
                                <option value="l">Liters (l)</option>
                                <option value="unit">Units</option>
                              </select>
                            </div>
                          </>
                        )}

                        {changeForm.changeCategory === 'OPERATION' && (
                          <>
                            <div>
                              <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                                Operation Name
                              </label>
                              <input
                                type="text"
                                value={changeForm.operationName}
                                onChange={(e) => setChangeForm(prev => ({ ...prev, operationName: e.target.value }))}
                                className="input"
                                placeholder="e.g. Setting and Testing"
                                style={{ fontSize: '0.875rem' }}
                              />
                            </div>
                            {changeForm.changeType !== 'REMOVED' && (
                              <>
                                <div>
                                  <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                                    Work Center
                                  </label>
                                  <input
                                    type="text"
                                    value={changeForm.workCenter}
                                    onChange={(e) => setChangeForm(prev => ({ ...prev, workCenter: e.target.value }))}
                                    className="input"
                                    placeholder="e.g. Assembly Line 1"
                                    style={{ fontSize: '0.875rem' }}
                                  />
                                </div>
                                <div>
                                  <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                                    Duration (Minutes)
                                  </label>
                                  <input
                                    type="number"
                                    value={changeForm.expectedDurationMinutes}
                                    onChange={(e) => setChangeForm(prev => ({ ...prev, expectedDurationMinutes: e.target.value }))}
                                    className="input"
                                    placeholder="Minutes"
                                    min="0"
                                    style={{ fontSize: '0.875rem' }}
                                  />
                                </div>
                                <div>
                                  <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1">
                                    Sequence (Order)
                                  </label>
                                  <input
                                    type="number"
                                    value={changeForm.sequence}
                                    onChange={(e) => setChangeForm(prev => ({ ...prev, sequence: e.target.value }))}
                                    className="input"
                                    placeholder="1, 2, 3..."
                                    min="1"
                                    style={{ fontSize: '0.875rem' }}
                                  />
                                </div>
                              </>
                            )}
                          </>
                        )}

                        <div className="flex gap-2 pt-1">
                          <button
                            onClick={handleAddBomChange}
                            disabled={addingChange}
                            className="btn btn-primary"
                            style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                          >
                            {addingChange ? 'Adding...' : 'Add BOM Change'}
                          </button>
                          <button
                            onClick={() => { setShowAddForm(false); resetChangeForm(); }}
                            className="btn btn-secondary"
                            style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Existing Changes */}
                {(eco.productChanges?.length > 0 || eco.bomChanges?.length > 0) ? (
                  <div className="space-y-3">
                    {/* Product changes */}
                    {(eco.productChanges || []).map((change, index) => (
                      <div
                        key={change.id || index}
                        className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] rounded"
                      >
                        <div>
                          <span className="font-medium text-[var(--text-primary)]">
                            {change.fieldName || change.field || change.label}
                          </span>
                          <span className="text-[var(--text-secondary)] ml-2">
                            {change.oldValue || '—'} → {change.newValue}
                          </span>
                        </div>
                        {canEdit && change.id && (
                          <button
                            onClick={() => handleRemoveChange(change.id)}
                            className="text-[var(--red)] hover:text-red-400 text-sm"
                            title="Remove change"
                          >
                            ✕
                          </button>
                        )}
                      </div>
                    ))}
                    {/* BOM changes */}
                    {(eco.bomChanges || []).map((change, index) => (
                      <div
                        key={change.id || `bom-${index}`}
                        className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] rounded"
                      >
                        <div>
                          <span className="font-medium text-[var(--text-primary)]">
                            {change.componentName || 'Component'} ({change.changeType})
                          </span>
                          <span className="text-[var(--text-secondary)] ml-2">
                            {change.oldQuantity || '-'} → {change.newQuantity || '-'} {change.unit || ''}
                          </span>
                        </div>
                        {canEdit && change.id && (
                          <button
                            onClick={() => handleRemoveChange(change.id)}
                            className="text-[var(--red)] hover:text-red-400 text-sm"
                            title="Remove change"
                          >
                            ✕
                          </button>
                        )}
                      </div>
                    ))}
                    {/* Operation changes */}
                    {(eco.bomOperationChanges || []).map((change, index) => (
                      <div
                        key={change.id || `op-${index}`}
                        className="flex items-center justify-between p-3 bg-[var(--bg-elevated)] rounded"
                      >
                        <div>
                          <span className="font-medium text-[var(--text-primary)]">
                            Operation: {change.operationName} ({change.changeType})
                          </span>
                          <span className="text-[var(--text-secondary)] ml-2 text-sm">
                            {change.workCenter || '-'} | {change.expectedDurationMinutes} min | Seq: {change.sequence}
                          </span>
                        </div>
                        {canEdit && change.id && (
                          <button
                            onClick={async () => {
                              if (!window.confirm('Remove this proposed operation change?')) return;
                              try {
                                await ecoApi.removeBomOperationChange(id, change.id);
                                loadEcoData();
                                toast.success('Operation change removed');
                              } catch (e) {
                                toast.error('Failed to remove operation change');
                              }
                            }}
                            className="text-[var(--red)] hover:text-red-400 text-sm"
                            title="Remove change"
                          >
                            ✕
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  !showAddForm && (
                    <p className="text-[var(--text-secondary)]">
                      No changes have been added yet.{canEdit && ' Click "Add Change" to propose modifications.'}
                    </p>
                  )
                )}

                {/* Attachments Section Inside Proposed Changes */}
                <div className="mt-8 pt-6 border-t border-[var(--bg-border)]">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-md font-semibold text-[var(--text-primary)]">
                      Attachments
                    </h3>
                    {canEdit && (
                      <label className="btn btn-secondary cursor-pointer" style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}>
                        {uploadingAttachment ? 'Uploading...' : '+ Upload File'}
                        <input type="file" className="hidden" onChange={handleFileUpload} disabled={uploadingAttachment} />
                      </label>
                    )}
                  </div>
                  {eco.attachments?.length > 0 ? (
                    <div className="flex flex-wrap gap-2">
                      {eco.attachments.map(att => (
                        <div key={att.id} className="flex items-center gap-2 p-2 bg-[var(--bg-elevated)] rounded shadow-sm border border-[var(--bg-border)]">
                            <a href={att.fileUrl} target="_blank" rel="noopener noreferrer" className="text-sm text-[var(--accent)] hover:underline truncate max-w-[150px]">
                              {att.fileName}
                            </a>
                            {canEdit && (
                              <button onClick={() => handleRemoveAttachment(att.id)} className="text-[var(--red)] hover:text-red-400">
                                <X size={14} />
                              </button>
                            )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-[var(--text-secondary)]">No files attached to this ECO.</p>
                  )}
                </div>

              </div>
            ) : (
              <div>
                <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
                  Comparison View
                </h3>
                {comparison?.type === 'Product' ? (
                  <ProductComparisonTable changes={comparison.changes} />
                ) : (
                  <BomComparisonTable
                    components={comparison?.components}
                    operations={comparison?.operations}
                  />
                )}
              </div>
            )}
          </div>
        </div>

        {/* Right - Action & Info Panel */}
        <div className="space-y-6">
          {/* ECO Metadata */}
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              ECO Details
            </h3>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-[var(--text-muted)]">Type</span>
                <StatusBadge status={eco.ecoType || eco.type} size="sm" />
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--text-muted)]">Stage</span>
                <StatusBadge status={eco.currentStageName || eco.stage || eco.status} size="sm" />
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--text-muted)]">Version Update</span>
                <StatusBadge status={eco.versionUpdate ? 'yes' : 'no'} size="sm" />
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--text-muted)]">Effective Date</span>
                <span className="text-[var(--text-primary)]">
                  {eco.effectiveDate ? dayjs(eco.effectiveDate).format('MMM D, YYYY') : 'Not set'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--text-muted)]">Created By</span>
                <span className="text-[var(--text-primary)]">{eco.createdByName || eco.createdBy}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--text-muted)]">Created At</span>
                <span className="text-[var(--text-primary)]">
                  {dayjs(eco.createdAt).format('MMM D, YYYY')}
                </span>
              </div>
            </div>
          </div>

          {/* Stage Actions */}
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              Actions
            </h3>

            {isApplied ? (
              <div className="flex items-center gap-2 p-3 bg-[var(--green-dim)] rounded-lg">
                <Check size={20} className="text-[var(--green)]" />
                <div>
                  <p className="font-medium text-[var(--green)]">Applied</p>
                  <p className="text-xs text-[var(--text-secondary)]">
                    Changes have been applied successfully
                  </p>
                </div>
              </div>
            ) : (
              <div className="space-y-3">
                {canSubmit && (
                  <button
                    onClick={handleSubmitForReview}
                    disabled={isSubmitting}
                    className="btn btn-primary w-full"
                  >
                    <Send size={18} />
                    Submit for Approval
                  </button>
                )}

                {canApproveEco && (
                  <>
                    <button
                      onClick={() => setShowApproveModal(true)}
                      disabled={isSubmitting}
                      className="btn btn-success w-full"
                    >
                      <Check size={18} />
                      Approve
                    </button>
                    <button
                      onClick={() => setShowRejectModal(true)}
                      disabled={isSubmitting}
                      className="btn btn-danger w-full"
                    >
                      <X size={18} />
                      Reject / Send Back
                    </button>
                  </>
                )}

                {!canSubmit && !canApproveEco && (
                  <p className="text-sm text-[var(--text-muted)] text-center">
                    No actions available for your role at this stage.
                  </p>
                )}
              </div>
            )}
          </div>

          {/* Activity Log */}
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-5">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              Activity Log
            </h3>
            <ActivityLog activities={activities} />
          </div>
        </div>
      </div>

      {/* Approve Modal */}
      <ConfirmModal
        isOpen={showApproveModal}
        onClose={() => setShowApproveModal(false)}
        onConfirm={handleApprove}
        title="Approve ECO"
        message="Are you sure you want to approve this Engineering Change Order? This action will progress the ECO to the next stage."
        confirmText="Approve"
        variant="primary"
        isLoading={isSubmitting}
      />

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setShowRejectModal(false)}
          />
          <div className="relative bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-xl p-6 w-full max-w-md mx-4 shadow-2xl">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              Reject ECO
            </h3>
            <div className="mb-4">
              <label className="block text-sm font-medium text-[var(--text-primary)] mb-1.5">
                Reason for rejection
              </label>
              <textarea
                className="input h-24 resize-none"
                placeholder="Please provide a reason..."
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
              />
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => setShowRejectModal(false)}
                disabled={isSubmitting}
                className="btn btn-secondary flex-1"
              >
                Cancel
              </button>
              <button
                onClick={handleReject}
                disabled={isSubmitting || !rejectReason.trim()}
                className="btn btn-danger flex-1"
              >
                {isSubmitting ? 'Rejecting...' : 'Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EcoDetailPage;
