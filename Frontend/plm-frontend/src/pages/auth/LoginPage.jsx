import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, CheckCircle, Shield, GitBranch } from 'lucide-react';
import toast from 'react-hot-toast';
import useAuthStore from '../../context/authStore';
import authApi from '../../api/authApi';

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const from = location.state?.from?.pathname || '/dashboard';

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      const response = await authApi.login(data.email, data.password);
      login({
        user: response.user,
        token: response.token,
        role: response.user?.role,
      });
      toast.success('Welcome back!');
      navigate(from, { replace: true });
    } catch (error) {
      // Error is handled by axios interceptor
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      {/* Left Panel - Branding */}
      <div className="auth-branding">
        <div className="auth-branding-content">
          {/* Logo */}
          <div className="auth-logo">
            <div className="auth-logo-icon">
              <span>P</span>
            </div>
            <span className="auth-logo-text">PLM</span>
          </div>

          {/* Tagline */}
          <h1 className="auth-tagline">
            Engineering Changes,<br />
            <span>Executed with Control</span>
          </h1>

          {/* Features */}
          <div className="auth-features">
            <div className="auth-feature">
              <div className="auth-feature-icon blue">
                <CheckCircle size={20} />
              </div>
              <div>
                <p className="auth-feature-title">Versioned Changes</p>
                <p className="auth-feature-desc">Track every modification with complete history</p>
              </div>
            </div>

            <div className="auth-feature">
              <div className="auth-feature-icon yellow">
                <Shield size={20} />
              </div>
              <div>
                <p className="auth-feature-title">Approval Workflows</p>
                <p className="auth-feature-desc">Multi-stage approval process with role-based access</p>
              </div>
            </div>

            <div className="auth-feature">
              <div className="auth-feature-icon green">
                <GitBranch size={20} />
              </div>
              <div>
                <p className="auth-feature-title">Full Traceability</p>
                <p className="auth-feature-desc">Complete audit trail for compliance</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Right Panel - Form */}
      <div className="auth-form-panel">
        <div className="auth-form-container">
          {/* Mobile logo */}
          <div className="mobile-logo">
            <div className="mobile-logo-icon">
              <span>P</span>
            </div>
            <span className="mobile-logo-text">PLM</span>
          </div>

          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
            Welcome back
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
            Sign in to your PLM workspace
          </p>

          <form onSubmit={handleSubmit(onSubmit)}>
            {/* Email */}
            <div className="form-group">
              <label htmlFor="email" className="form-label">
                Email
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                className="input"
                placeholder="you@company.com"
                {...register('email')}
              />
              {errors.email && (
                <p className="form-error">{errors.email.message}</p>
              )}
            </div>

            {/* Password */}
            <div className="form-group">
              <label htmlFor="password" className="form-label">
                Password
              </label>
              <div className="input-wrapper">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  className="input"
                  style={{ paddingRight: '2.5rem' }}
                  placeholder="Enter your password"
                  {...register('password')}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="input-icon-right"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.password && (
                <p className="form-error">{errors.password.message}</p>
              )}
            </div>

            {/* Forgot password */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1.25rem' }}>
              <Link
                to="/forgot-password"
                style={{ fontSize: '0.875rem', color: 'var(--accent)' }}
              >
                Forgot password?
              </Link>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={isLoading}
              className="btn btn-primary"
              style={{ width: '100%', height: '2.75rem' }}
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          {/* Sign up link */}
          <p style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Don't have an account?{' '}
            <Link to="/signup" style={{ color: 'var(--accent)', fontWeight: 500 }}>
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
