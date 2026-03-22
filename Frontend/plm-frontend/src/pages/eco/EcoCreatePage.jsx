import { useState, useEffect, useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  ChevronRight,
  Package,
  GitBranch,
  Info,
  Calendar,
  User,
  FileText,
  CheckCircle,
} from 'lucide-react';
import toast from 'react-hot-toast';
import clsx from 'clsx';
import { productApi, bomApi, ecoApi } from '../../api';
import useAuthStore from '../../context/authStore';
import dayjs from 'dayjs';

const ecoSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  ecoType: z.enum(['PRODUCT', 'BOM'], { required_error: 'Please select an ECO type' }),
  productId: z.string().min(1, 'Please select a product'),
  bomId: z.string().optional(),
  effectiveDate: z.string().min(1, 'Effective date is required'),
  versionUpdate: z.boolean(),
});

const EcoCreatePage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [products, setProducts] = useState([]);
  const [boms, setBoms] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedBom, setSelectedBom] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(ecoSchema),
    defaultValues: {
      title: '',
      ecoType: '',
      productId: '',
      bomId: '',
      effectiveDate: dayjs().add(7, 'day').format('YYYY-MM-DD'),
      versionUpdate: true,
    },
  });

  const ecoType = watch('ecoType');
  const versionUpdate = watch('versionUpdate');
  const productId = watch('productId');
  const bomId = watch('bomId');
  const effectiveDate = watch('effectiveDate');
  const title = watch('title');

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    if (productId && ecoType === 'BOM') {
      loadBoms(productId);
    }
  }, [productId, ecoType]);

  useEffect(() => {
    if (bomId && boms.length > 0) {
      const bom = boms.find((b) => b.id.toString() === bomId);
      setSelectedBom(bom);
    }
  }, [bomId, boms]);

  const loadProducts = async () => {
    try {
      // Use /products/active endpoint for active products
      const data = await productApi.getActiveProducts();
      const productList = data.products || data.content || data.data || data;
      setProducts(Array.isArray(productList) ? productList : []);
    } catch (error) {
      console.error('Failed to load products:', error);
      // Don't use mock data - it causes issues with real UUIDs
      setProducts([]);
    }
  };

  const loadBoms = async (productId) => {
    try {
      const data = await bomApi.getBomsByProduct(productId);
      const bomList = data.boms || data.content || data.data || data;
      setBoms(Array.isArray(bomList) ? bomList : []);
    } catch (error) {
      console.error('Failed to load BOMs:', error);
      // Don't use mock data
      setBoms([]);
    }
  };

  const handleProductChange = (e) => {
    const prodId = e.target.value;
    setValue('productId', prodId);
    setValue('bomId', '');
    setSelectedBom(null);
    const product = products.find((p) => p.id.toString() === prodId);
    setSelectedProduct(product);
  };

  const onSubmit = async (data, isDraft = false) => {
    setIsLoading(true);
    try {
      const payload = {
        title: data.title,
        ecoType: data.ecoType,
        productId: data.productId,
        bomId: data.ecoType === 'BOM' ? data.bomId : null,
        effectiveDate: data.effectiveDate,
        versionUpdate: data.versionUpdate,
      };

      const response = await ecoApi.createEco(payload);

      if (isDraft) {
        toast.success('ECO saved as draft');
      } else {
        // Try to submit, but if it fails (e.g., no changes yet), still navigate
        try {
          await ecoApi.submitEco(response.id);
          toast.success('ECO submitted for review');
        } catch (submitError) {
          toast.success('ECO created as draft. Add changes before submitting for review.');
        }
      }

      navigate(`/eco/${response.id}`);
    } catch (error) {
      console.error('Failed to create ECO:', error);
      toast.error(error.response?.data?.message || 'Failed to create ECO');
    } finally {
      setIsLoading(false);
    }
  };

  // Quick summary values
  const summary = useMemo(() => {
    return {
      type: ecoType === 'BOM' ? 'Bill of Materials' : ecoType === 'PRODUCT' ? 'Product' : '-',
      product: selectedProduct?.name || '-',
      bom: selectedBom ? `v${selectedBom.version}` : '-',
      effectiveDate: effectiveDate ? dayjs(effectiveDate).format('MMM D, YYYY') : '-',
      versionUpdate: versionUpdate ? 'Yes' : 'No',
    };
  }, [ecoType, selectedProduct, selectedBom, effectiveDate, versionUpdate]);

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/eco" className="hover:text-[var(--text-primary)]">
          Change Orders
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">New ECO</span>
      </nav>

      <h1 className="text-2xl font-bold text-[var(--text-primary)] mb-6">Create New ECO</h1>

      <form onSubmit={handleSubmit((data) => onSubmit(data, false))}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Main Form */}
          <div className="lg:col-span-2 space-y-6">
            {/* Essential Information */}
            <div className="form-section">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <FileText size={18} />
                  Essential Information
                </h2>
                <p className="form-section-description">
                  Basic details about this engineering change order
                </p>
              </div>
              <div className="form-section-body">
                {/* Title */}
                <div className="form-group">
                  <label className="form-label">
                    Title <span className="text-[var(--red)]">*</span>
                  </label>
                  <input
                    type="text"
                    className="input"
                    placeholder="e.g., Update material specifications for Q2"
                    {...register('title')}
                  />
                  {errors.title && <p className="form-error">{errors.title.message}</p>}
                </div>

                {/* ECO Type */}
                <div className="form-group">
                  <label className="form-label">
                    ECO Type <span className="text-[var(--red)]">*</span>
                  </label>
                  <select className="input" {...register('ecoType')}>
                    <option value="">Select ECO type</option>
                    <option value="BOM">Bill of Materials (BoM)</option>
                    <option value="PRODUCT">Product</option>
                  </select>
                  {errors.ecoType && <p className="form-error">{errors.ecoType.message}</p>}
                </div>

                {/* User (Read-only) */}
                <div className="form-group">
                  <label className="form-label">Created By</label>
                  <div className="flex items-center gap-3 p-3 bg-[var(--bg-elevated)] rounded border border-[var(--bg-border)]">
                    <div className="w-8 h-8 rounded-full bg-[var(--accent-dim)] flex items-center justify-center">
                      <User size={16} className="text-[var(--accent)]" />
                    </div>
                    <span className="text-[var(--text-primary)]">
                      {user?.firstName} {user?.lastName || user?.loginId || 'Current User'}
                    </span>
                  </div>
                </div>

                {/* Product Selection */}
                <div className="form-group">
                  <label className="form-label">
                    Product Selection <span className="text-[var(--red)]">*</span>
                  </label>
                  <select
                    className="input"
                    {...register('productId')}
                    onChange={handleProductChange}
                  >
                    <option value="">Select a product</option>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                  {errors.productId && <p className="form-error">{errors.productId.message}</p>}
                </div>
              </div>
            </div>

            {/* Active Bill of Materials (only for BOM type) */}
            {ecoType === 'BOM' && productId && (
              <div className="form-section">
                <div className="form-section-header">
                  <h2 className="form-section-title">
                    <GitBranch size={18} />
                    Active Bill of Materials
                  </h2>
                  <p className="form-section-description">
                    Select the BOM version to modify
                  </p>
                </div>
                <div className="form-section-body">
                  {boms.length === 0 ? (
                    <div className="text-center py-6 text-[var(--text-secondary)]">
                      No BOMs found for this product
                    </div>
                  ) : (
                    <div className="bom-selector">
                      {boms.map((bom) => (
                        <label
                          key={bom.id}
                          className={clsx('bom-option', bomId === bom.id.toString() && 'selected')}
                        >
                          <input
                            type="radio"
                            value={bom.id}
                            className="sr-only"
                            {...register('bomId')}
                          />
                          <div className="bom-option-info">
                            <p className="bom-option-name">
                              {bom.reference || `BOM v${bom.version}`}
                            </p>
                            <p className="bom-option-meta">
                              {bom.componentsCount || 0} components, {bom.operationsCount || 0}{' '}
                              operations
                            </p>
                          </div>
                          <span
                            className={clsx(
                              'bom-option-badge',
                              bom.status === 'ACTIVE' ? 'badge-green' : 'badge-muted'
                            )}
                          >
                            {bom.status === 'ACTIVE' ? 'Active' : 'Draft'}
                          </span>
                        </label>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Scheduling */}
            <div className="form-section">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <Calendar size={18} />
                  Scheduling
                </h2>
                <p className="form-section-description">
                  When should this change take effect?
                </p>
              </div>
              <div className="form-section-body">
                <div className="form-group mb-0">
                  <label className="form-label">
                    Effective Date <span className="text-[var(--red)]">*</span>
                  </label>
                  <input type="date" className="input" {...register('effectiveDate')} />
                  {errors.effectiveDate && (
                    <p className="form-error">{errors.effectiveDate.message}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Version Update */}
            <div className="form-section">
              <div className="form-section-body">
                <div className="checkbox-custom">
                  <input type="checkbox" {...register('versionUpdate')} />
                  <div>
                    <p className="checkbox-custom-label">Create new version on approval</p>
                    <p className="checkbox-custom-description">
                      A new version will be created when this ECO is approved. The current version
                      will be archived for reference.
                    </p>
                  </div>
                </div>

                {/* Info banner */}
                <div
                  className={clsx(
                    'flex items-start gap-3 p-3 rounded-lg mt-4',
                    versionUpdate ? 'bg-[var(--blue-dim)]' : 'bg-[var(--yellow-dim)]'
                  )}
                >
                  <Info
                    size={18}
                    className={clsx(
                      'flex-shrink-0 mt-0.5',
                      versionUpdate ? 'text-[var(--blue)]' : 'text-[var(--yellow)]'
                    )}
                  />
                  <p className="text-sm text-[var(--text-primary)]">
                    {versionUpdate
                      ? 'A new version will be created. Existing version will be archived.'
                      : 'Changes will be applied to the current version directly.'}
                  </p>
                </div>
              </div>
            </div>
            {/* Proposed Changes (Empty State) */}
            <div className="form-section">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <GitBranch size={18} />
                  Proposed Changes & Attachments
                </h2>
                <p className="form-section-description">
                  Modifications to products, BOM components, and operations
                </p>
              </div>
              <div className="form-section-body">
                <div className="text-center py-8 rounded-lg border-2 border-dashed border-[var(--bg-border)] bg-[var(--bg-elevated)]">
                  <p className="text-[var(--text-primary)] font-medium mb-2">
                    Submit or Save Draft to Add Changes
                  </p>
                  <p className="text-sm text-[var(--text-secondary)]">
                    You must create the basic Change Order record first. Click "Save as Draft" below to unlock the product modification tools, routing operations, and file attachments.
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column - Quick Summary */}
          <div className="lg:col-span-1">
            <div className="form-section sticky top-20">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <CheckCircle size={18} />
                  Quick Summary
                </h2>
              </div>
              <div className="form-section-body p-0">
                <div className="quick-summary">
                  <div className="quick-summary-row">
                    <span className="quick-summary-label">ECO Type</span>
                    <span className="quick-summary-value">{summary.type}</span>
                  </div>
                  <div className="quick-summary-row">
                    <span className="quick-summary-label">Product</span>
                    <span className="quick-summary-value">{summary.product}</span>
                  </div>
                  {ecoType === 'BOM' && (
                    <div className="quick-summary-row">
                      <span className="quick-summary-label">BOM Version</span>
                      <span className="quick-summary-value">{summary.bom}</span>
                    </div>
                  )}
                  <div className="quick-summary-row">
                    <span className="quick-summary-label">Effective Date</span>
                    <span className="quick-summary-value">{summary.effectiveDate}</span>
                  </div>
                  <div className="quick-summary-row">
                    <span className="quick-summary-label">Version Update</span>
                    <span
                      className={clsx(
                        'quick-summary-value',
                        versionUpdate ? 'text-[var(--green)]' : 'text-[var(--yellow)]'
                      )}
                    >
                      {summary.versionUpdate}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Action Bar */}
        <div className="sticky bottom-0 bg-[var(--bg-base)] border-t border-[var(--bg-border)] -mx-6 px-6 py-4 mt-6 flex items-center justify-between">
          <div className="flex gap-3">
            <Link to="/eco" className="btn btn-ghost">
              Cancel
            </Link>
            <button
              type="button"
              onClick={handleSubmit((data) => onSubmit(data, true))}
              disabled={isLoading}
              className="btn btn-secondary"
            >
              Save as Draft
            </button>
          </div>
          <button type="submit" disabled={isLoading} className="btn btn-primary">
            {isLoading ? 'Submitting...' : 'Submit for Review'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default EcoCreatePage;
