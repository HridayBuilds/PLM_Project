import { AlertTriangle, X } from 'lucide-react';
import clsx from 'clsx';

const ConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'danger',
  isLoading = false,
}) => {
  if (!isOpen) return null;

  const variantClasses = {
    danger: 'btn-danger',
    warning: 'bg-[var(--yellow)] text-white hover:bg-[#d97706]',
    primary: 'btn-primary',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-xl p-6 w-full max-w-md mx-4 shadow-2xl">
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1 text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors"
        >
          <X size={20} />
        </button>

        {/* Icon */}
        <div className="flex justify-center mb-4">
          <div
            className={clsx(
              'w-12 h-12 rounded-full flex items-center justify-center',
              variant === 'danger'
                ? 'bg-[var(--red-dim)]'
                : variant === 'warning'
                ? 'bg-[var(--yellow-dim)]'
                : 'bg-[var(--accent-dim)]'
            )}
          >
            <AlertTriangle
              size={24}
              className={clsx(
                variant === 'danger'
                  ? 'text-[var(--red)]'
                  : variant === 'warning'
                  ? 'text-[var(--yellow)]'
                  : 'text-[var(--accent)]'
              )}
            />
          </div>
        </div>

        {/* Content */}
        <h3 className="text-lg font-semibold text-[var(--text-primary)] text-center mb-2">
          {title}
        </h3>
        <p className="text-[var(--text-secondary)] text-center mb-6">{message}</p>

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            disabled={isLoading}
            className="btn btn-secondary flex-1"
          >
            {cancelText}
          </button>
          <button
            onClick={onConfirm}
            disabled={isLoading}
            className={clsx('btn flex-1', variantClasses[variant])}
          >
            {isLoading ? 'Loading...' : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
