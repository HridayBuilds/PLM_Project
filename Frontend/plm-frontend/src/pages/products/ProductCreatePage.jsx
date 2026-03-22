import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ChevronRight, Package, Upload, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { productApi, fileApi } from '../../api';

const productSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  description: z.string().max(1000).optional(),
  salePrice: z.coerce.number().min(0, 'Sale price must be positive'),
  costPrice: z.coerce.number().min(0, 'Cost price must be positive'),
});

const ProductCreatePage = () => {
  const navigate = useNavigate();
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
    resolver: zodResolver(productSchema),
    defaultValues: {
      name: '',
      description: '',
      salePrice: 0,
      costPrice: 0,
    },
  });

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

      const response = await productApi.createProduct({
        name: data.name,
        description: data.description,
        salePrice: data.salePrice,
        costPrice: data.costPrice,
        attachmentIds: attachmentIds,
      });
      toast.success('Product created successfully');
      navigate(`/products/${response.id}`);
    } catch (error) {
      toast.dismiss('upload-toast');
      toast.error(error.response?.data?.message || 'Failed to create product');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/products" className="hover:text-[var(--text-primary)]">
          Products
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">New Product</span>
      </nav>

      <h1 className="text-2xl font-bold text-[var(--text-primary)] mb-6">Create New Product</h1>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="form-section">
              <div className="form-section-header">
                <h2 className="form-section-title">
                  <Package size={18} />
                  Product Information
                </h2>
                <p className="form-section-description">
                  Basic details about the new product
                </p>
              </div>
              <div className="form-section-body">
                <div className="form-group">
                  <label className="form-label">
                    Product Name <span className="text-[var(--red)]">*</span>
                  </label>
                  <input
                    type="text"
                    className="input"
                    placeholder="e.g., Office Chair Pro"
                    {...register('name')}
                  />
                  {errors.name && <p className="form-error">{errors.name.message}</p>}
                </div>

                <div className="form-group">
                  <label className="form-label">Description</label>
                  <textarea
                    className="input min-h-[100px]"
                    placeholder="Product details and specifications..."
                    {...register('description')}
                  />
                  {errors.description && <p className="form-error">{errors.description.message}</p>}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="form-group">
                    <label className="form-label">
                      Cost Price (INR) <span className="text-[var(--red)]">*</span>
                    </label>
                    <input
                      type="number"
                      className="input"
                      placeholder="0.00"
                      {...register('costPrice')}
                    />
                    {errors.costPrice && <p className="form-error">{errors.costPrice.message}</p>}
                  </div>

                  <div className="form-group">
                    <label className="form-label">
                      Sale Price (INR) <span className="text-[var(--red)]">*</span>
                    </label>
                    <input
                      type="number"
                      className="input"
                      placeholder="0.00"
                      {...register('salePrice')}
                    />
                    {errors.salePrice && <p className="form-error">{errors.salePrice.message}</p>}
                  </div>
                </div>
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
                  Upload product specifications, images, or documents.
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
          <Link to="/products" className="btn btn-ghost">
            Cancel
          </Link>
          <button type="submit" disabled={isLoading} className="btn btn-primary">
            {isLoading ? 'Saving...' : 'Create Product'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProductCreatePage;
