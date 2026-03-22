import { useMemo } from 'react';

const PasswordStrengthMeter = ({ password }) => {
  const strength = useMemo(() => {
    if (!password) return { score: 0, label: '', color: '', width: '0%' };

    let score = 0;

    // Length checks
    if (password.length >= 8) score += 1;
    if (password.length >= 12) score += 1;

    // Character type checks
    if (/[A-Z]/.test(password)) score += 1;
    if (/[a-z]/.test(password)) score += 1;
    if (/[0-9]/.test(password)) score += 1;
    if (/[^A-Za-z0-9]/.test(password)) score += 1;

    // Map score to strength level
    if (score <= 2) return { score: 1, label: 'Weak', color: 'var(--red)', width: '25%' };
    if (score <= 3) return { score: 2, label: 'Fair', color: 'var(--yellow)', width: '50%' };
    if (score <= 4) return { score: 3, label: 'Good', color: 'var(--yellow)', width: '75%' };
    return { score: 4, label: 'Strong', color: 'var(--green)', width: '100%' };
  }, [password]);

  if (!password) return null;

  return (
    <div style={{
      marginTop: '0.75rem',
      display: 'flex',
      alignItems: 'center',
      gap: '0.75rem',
    }}>
      {/* Slider track */}
      <div style={{
        flex: 1,
        height: '6px',
        backgroundColor: 'var(--bg-border)',
        borderRadius: '3px',
        overflow: 'hidden',
      }}>
        {/* Slider fill */}
        <div
          style={{
            height: '100%',
            width: strength.width,
            backgroundColor: strength.color,
            borderRadius: '3px',
            transition: 'all 0.3s ease',
          }}
        />
      </div>

      {/* Label */}
      <span
        style={{
          fontSize: '0.75rem',
          fontWeight: 500,
          color: strength.color,
          minWidth: '50px',
          textAlign: 'right',
          transition: 'color 0.3s ease',
        }}
      >
        {strength.label}
      </span>
    </div>
  );
};

export default PasswordStrengthMeter;
