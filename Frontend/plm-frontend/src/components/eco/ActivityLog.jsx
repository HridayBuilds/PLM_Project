import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import {
  Plus,
  Send,
  Check,
  X,
  Edit,
  User,
  ArrowRight,
} from 'lucide-react';
import clsx from 'clsx';

dayjs.extend(relativeTime);

const activityIcons = {
  created: Plus,
  submitted: Send,
  approved: Check,
  rejected: X,
  edited: Edit,
  moved: ArrowRight,
  default: User,
};

const activityColors = {
  created: 'blue',
  submitted: 'yellow',
  approved: 'green',
  rejected: 'red',
  edited: 'accent',
  moved: 'accent',
  default: 'muted',
};

const ActivityLog = ({ activities }) => {
  if (!activities || activities.length === 0) {
    return (
      <div className="text-center py-8 text-[var(--text-muted)]">
        No activity yet
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {activities.map((activity, index) => {
        const Icon = activityIcons[activity.type] || activityIcons.default;
        const color = activityColors[activity.type] || activityColors.default;

        return (
          <div key={index} className="flex gap-3">
            {/* Icon */}
            <div
              className={clsx(
                'w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0',
                `bg-[var(--${color}-dim)]`
              )}
            >
              <Icon size={14} className={`text-[var(--${color})]`} />
            </div>

            {/* Content */}
            <div className="flex-1 min-w-0">
              <p className="text-sm text-[var(--text-primary)]">
                {activity.message}
              </p>
              <p className="text-xs text-[var(--text-muted)] mt-0.5">
                {activity.user} · {dayjs(activity.timestamp).fromNow()}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default ActivityLog;
