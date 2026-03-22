import { X } from 'lucide-react';
import clsx from 'clsx';

const SlideDrawer = ({ isOpen, onClose, title, children, width = 'md' }) => {
  if (!isOpen) return null;

  const widthClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
    '2xl': 'max-w-2xl',
  };

  return (
    <div className="fixed inset-0 z-50">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Drawer */}
      <div
        className={clsx(
          'absolute right-0 top-0 h-full w-full bg-[var(--bg-surface)] border-l border-[var(--bg-border)] shadow-2xl',
          'transform transition-transform duration-300',
          widthClasses[width],
          isOpen ? 'translate-x-0' : 'translate-x-full'
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[var(--bg-border)]">
          <h3 className="text-lg font-semibold text-[var(--text-primary)]">{title}</h3>
          <button
            onClick={onClose}
            className="p-1.5 rounded hover:bg-[var(--bg-elevated)] text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 overflow-y-auto h-[calc(100%-65px)]">{children}</div>
      </div>
    </div>
  );
};

export default SlideDrawer;
