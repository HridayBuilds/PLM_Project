import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircle, XCircle, Loader2 } from 'lucide-react';
import authApi from '../../api/authApi';

const VerifyEmailPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState('verifying'); // verifying, success, error
  const [message, setMessage] = useState('');
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Invalid verification link. No token provided.');
      return;
    }

    const verifyEmail = async () => {
      try {
        const response = await authApi.verifyEmail(token);
        setStatus('success');
        setMessage(response.message || 'Your email has been verified successfully!');
      } catch (error) {
        setStatus('error');
        setMessage(error.response?.data?.message || 'Verification failed. The link may be expired or invalid.');
      }
    };

    // Small delay to show the verifying animation
    const timer = setTimeout(() => {
      verifyEmail();
    }, 1500);

    return () => clearTimeout(timer);
  }, [token]);

  // Countdown and redirect after success
  useEffect(() => {
    if (status === 'success' && countdown > 0) {
      const timer = setTimeout(() => {
        setCountdown(countdown - 1);
      }, 1000);
      return () => clearTimeout(timer);
    } else if (status === 'success' && countdown === 0) {
      navigate('/login', { replace: true });
    }
  }, [status, countdown, navigate]);

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'var(--bg-base)',
      padding: '2rem',
    }}>
      <div style={{
        maxWidth: '400px',
        width: '100%',
        textAlign: 'center',
      }}>
        {/* Logo */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '0.75rem',
          marginBottom: '2.5rem',
        }}>
          <div style={{
            width: '3rem',
            height: '3rem',
            backgroundColor: 'var(--accent)',
            borderRadius: '0.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}>
            <span style={{ color: 'white', fontWeight: 700, fontSize: '1.25rem' }}>E</span>
          </div>
          <span style={{ fontSize: '1.875rem', fontWeight: 700, color: 'var(--text-primary)' }}>
            Ecova
          </span>
        </div>

        {/* Status Card */}
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--bg-border)',
          borderRadius: '12px',
          padding: '2.5rem 2rem',
        }}>
          {/* Icon */}
          <div style={{
            width: '5rem',
            height: '5rem',
            borderRadius: '50%',
            margin: '0 auto 1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: status === 'verifying'
              ? 'var(--accent-dim)'
              : status === 'success'
                ? 'var(--green-dim)'
                : 'var(--red-dim)',
          }}>
            {status === 'verifying' && (
              <Loader2
                size={40}
                style={{
                  color: 'var(--accent)',
                  animation: 'spin 1s linear infinite',
                }}
              />
            )}
            {status === 'success' && (
              <CheckCircle size={40} style={{ color: 'var(--green)' }} />
            )}
            {status === 'error' && (
              <XCircle size={40} style={{ color: 'var(--red)' }} />
            )}
          </div>

          {/* Title */}
          <h2 style={{
            fontSize: '1.5rem',
            fontWeight: 700,
            color: 'var(--text-primary)',
            marginBottom: '0.75rem',
          }}>
            {status === 'verifying' && 'Verifying Your Email'}
            {status === 'success' && 'Email Verified!'}
            {status === 'error' && 'Verification Failed'}
          </h2>

          {/* Message */}
          <p style={{
            color: 'var(--text-secondary)',
            marginBottom: '1.5rem',
            lineHeight: 1.6,
          }}>
            {status === 'verifying' && 'Please wait while we verify your email address...'}
            {status === 'success' && message}
            {status === 'error' && message}
          </p>

          {/* Countdown or Button */}
          {status === 'success' && (
            <div style={{
              backgroundColor: 'var(--bg-elevated)',
              borderRadius: '8px',
              padding: '1rem',
              marginBottom: '1.5rem',
            }}>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                Redirecting to login in
              </p>
              <p style={{
                fontSize: '2rem',
                fontWeight: 700,
                color: 'var(--accent)',
                marginTop: '0.25rem',
              }}>
                {countdown}s
              </p>
            </div>
          )}

          {/* Action Button */}
          {status === 'success' && (
            <button
              onClick={() => navigate('/login', { replace: true })}
              className="btn btn-primary"
              style={{ width: '100%', height: '2.75rem' }}
            >
              Go to Login Now
            </button>
          )}

          {status === 'error' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <button
                onClick={() => navigate('/login', { replace: true })}
                className="btn btn-primary"
                style={{ width: '100%', height: '2.75rem' }}
              >
                Go to Login
              </button>
              <button
                onClick={() => navigate('/signup', { replace: true })}
                className="btn btn-secondary"
                style={{ width: '100%', height: '2.75rem' }}
              >
                Create New Account
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Spinner Animation */}
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default VerifyEmailPage;
