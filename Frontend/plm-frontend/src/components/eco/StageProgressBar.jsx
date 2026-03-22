import clsx from 'clsx';
import { Check } from 'lucide-react';

const StageProgressBar = ({ stages, currentStage }) => {
  const currentIndex = stages.findIndex((s) => s.name === currentStage);

  return (
    <div className="flex items-center w-full">
      {stages.map((stage, index) => {
        const isCompleted = index < currentIndex;
        const isCurrent = index === currentIndex;
        const isLast = index === stages.length - 1;

        return (
          <div key={stage.id} className="flex items-center flex-1">
            {/* Stage node */}
            <div className="flex flex-col items-center">
              <div
                className={clsx(
                  'w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium border-2 transition-colors',
                  isCompleted
                    ? 'bg-[var(--green)] border-[var(--green)] text-white'
                    : isCurrent
                    ? 'bg-[var(--accent)] border-[var(--accent)] text-white'
                    : 'bg-[var(--bg-elevated)] border-[var(--bg-border)] text-[var(--text-muted)]'
                )}
              >
                {isCompleted ? (
                  <Check size={16} />
                ) : (
                  index + 1
                )}
              </div>
              <span
                className={clsx(
                  'mt-2 text-xs font-medium whitespace-nowrap',
                  isCurrent
                    ? 'text-[var(--accent)]'
                    : isCompleted
                    ? 'text-[var(--green)]'
                    : 'text-[var(--text-muted)]'
                )}
              >
                {stage.name}
              </span>
            </div>

            {/* Connector line */}
            {!isLast && (
              <div
                className={clsx(
                  'flex-1 h-0.5 mx-2 transition-colors',
                  isCompleted
                    ? 'bg-[var(--green)]'
                    : 'bg-[var(--bg-border)]'
                )}
              />
            )}
          </div>
        );
      })}
    </div>
  );
};

export default StageProgressBar;
