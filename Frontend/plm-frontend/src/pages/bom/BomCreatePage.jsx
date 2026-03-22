import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ChevronRight, GitBranch, Upload, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { bomApi, productApi, fileApi } from '../../api';

const bomSchema = z.object({
  productId: z.string().min(1, 'Please select a product'),
  reference: z.string().min(2, 'Reference must be at least 2 characters').max(8, 'Reference cannot exceed 8 characters'),
  quantity: z.coerce.number().min(1, 'Quantity must be at least 1'),
});


const BomCreatePage = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState([]);

  const handleFileChange = (e) => {
    if (e.target.files) {
      setSelectedFiles([...selectedFiles, ...Array.from(e.target.files)]);
    }
  };

  const removeFile = (index) => {
    setSelectedFiles(selectedFiles.filter((_, i) => i !== index));
  };

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(bomSchema),
    defaultValues: {
      productId: '',
      reference: '',
      quantity: 1,
    },
  });

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await productApi.getProducts({ status: 'ACTIVE' });
        const productList = Array.isArray(response)
          ? response
          : (response?.products || response?.data || response?.content || []);
        setProducts(Array.isArray(productList) ? productList : []);
      } catch (err) {
        console.error('Failed to load products for BOM creation', err);
      }
    };
    fetchProducts();
  }, []);

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      // Upload files first
      const attachmentIds = [];
      if (selectedFiles.length > 0) {
        toast.loading('Uploading files...', { id: 'upload-toast' });
        for (const file of selectedFiles) {
          const fileRes = await fileApi.uploadFile(file);
          attachmentIds.push(fileRes.id);
        }
        toast.dismiss('upload-toast');
      }

      const response = await bomApi.createBom({
        productId: data.productId,
        reference: data.reference,
        quantity: data.quantity,
        components: [], // Let user add components from the update/detail page later
        operations: [],
        attachmentIds: attachmentIds,
      });
      toast.success('Bill of Materials created successfully');
      navigate(`/bom/${response.id}`);
    } catch (error) {
      toast.dismiss('upload-toast');
      toast.error(error.response?.data?.message || 'Failed to create BOM');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/bom" className="hover:text-[var(--text-primary)]">
          Bill of Materials
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">New BoM</span>
      </nav>

      <h1 className="text-2xl font-bold text-[var(--text-primary)] mb-6">Create New BoM</h1>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="form-section">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <GitBranch size={18} />
                  BoM Information
                </h2>
                <p className="form-section-description">
                  Link this Bill of Materials to an active product
                </p>
              </div>
              <div className="form-section-body">
                <div className="form-group">
                  <label className="form-label">
                    Product <span className="text-[var(--red)]">*</span>
                  </label>
                  <select className="input" {...register('productId')}>
                    <option value="">Select a product</option>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                  {errors.productId && <p className="form-error">{errors.productId.message}</p>}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="form-group pb-2">
                    <label className="form-label">
                      Quantity <span className="text-[var(--red)]">*</span>
                    </label>
                    <input
                      type="number"
                      className="input"
                      min="1"
                      {...register('quantity')}
                    />
                    {errors.quantity && <p className="form-error">{errors.quantity.message}</p>}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="form-group pb-2">
                    <label className="form-label">
                      Reference Name <span className="text-[var(--red)]">*</span>
                    </label>
                    <input
                      type="text"
                      className="input"
                      placeholder="e.g., BOM-0000"
                      {...register('reference')}
                    />
                    {errors.reference && <p className="form-error">{errors.reference.message}</p>}
                  </div>

                  <div className="form-group pb-2">
                     <label className="form-label text-[var(--text-muted)]">
                       Version
                     </label>
                     <input
                       type="text"
                       className="input bg-[var(--bg-tertiary)] text-[var(--text-muted)] border-[var(--border)]"
                       value="1"
                       readOnly
                       disabled
                     />
                     <p className="text-[10px] text-[var(--text-muted)] mt-1 ml-1 leading-tight">Numeric, read-only field.<br/>Updates only when ECO of this BoM is applied.</p>
                  </div>
                </div>
                
                <p className="text-sm text-[var(--text-muted)] mt-4">
                   Note: You can add specific components and operations to this BoM from the subsequent detail view after creation.
                </p>
              </div>
            </div>
          </div>
          
          <div className="space-y-6">
            <div className="form-section">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <Upload size={18} />
                  Attachments
                </h2>
                <p className="form-section-description">
                  Upload CAD files, spec sheets, or BoM documentation.
                </p>
              </div>
              <div className="form-section-body">
                <div className="border-2 border-dashed border-[var(--bg-border)] rounded-lg p-6 text-center hover:bg-[var(--bg-hover)] transition-colors">
                  <input
                    type="file"
                    multiple
                    onChange={handleFileChange}
                    className="hidden"
                    id="file-upload"
                  />
                  <label
                    htmlFor="file-upload"
                    className="cursor-pointer flex flex-col items-center gap-2"
                  >
                    <div className="w-10 h-10 rounded-full bg-[var(--bg-secondary)] flex items-center justify-center text-[var(--text-secondary)]">
                      <Upload size={20} />
                    </div>
                    <span className="text-sm font-medium text-[var(--text-primary)]">
                      Click to upload files
                    </span>
                    <span className="text-xs text-[var(--text-tertiary)]">
                      Any file type, max 10MB each
                    </span>
                  </label>
                </div>

                {selectedFiles.length > 0 && (
                  <div className="mt-4 space-y-2">
                    <h3 className="text-sm font-medium text-[var(--text-secondary)] mb-2">
                      Selected Files ({selectedFiles.length})
                    </h3>
                    <ul className="space-y-2">
                      {selectedFiles.map((file, i) => (
                        <li
                          key={i}
                          className="flex items-center justify-between p-3 rounded-lg bg-[var(--bg-secondary)] border border-[var(--bg-border)] text-sm"
                        >
                          <div className="flex items-center gap-3 overflow-hidden">
                            <span className="truncate flex-1 font-medium title-text" title={file.name}>
                              {file.name}
                            </span>
                            <span className="text-[var(--text-tertiary)] text-xs flex-shrink-0">
                              {(file.size / 1024 / 1024).toFixed(2)} MB
                            </span>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeFile(i)}
                            className="text-[var(--text-tertiary)] hover:text-[var(--red)] transition-colors p-1"
                          >
                            <X size={16} />
                          </button>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="sticky bottom-0 bg-[var(--bg-base)] border-t border-[var(--bg-border)] -mx-6 px-6 py-4 mt-6 flex items-center justify-between">
          <Link to="/bom" className="btn btn-ghost">
            Cancel
          </Link>
          <button type="submit" disabled={isLoading} className="btn btn-primary">
            {isLoading ? 'Saving...' : 'Create BoM'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BomCreatePage;
