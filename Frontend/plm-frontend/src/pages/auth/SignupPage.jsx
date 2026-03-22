import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, CheckCircle, Shield, GitBranch, Clock } from 'lucide-react';
import toast from 'react-hot-toast';
import { PasswordStrengthMeter } from '../../components/auth';
import authApi from '../../api/authApi';

const signupSchema = z
  .object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    email: z.string().email('Please enter a valid email address'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[0-9]/, 'Password must contain at least one number')
      .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

const SignupPage = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(signupSchema),
  });

  const password = watch('password', '');

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      await authApi.signup({
        name: data.name,
        email: data.email,
        password: data.password,
      });
      setIsSubmitted(true);
      toast.success('Account created successfully!');
    } catch (error) {
      // Error is handled by axios interceptor
    } finally {
      setIsLoading(false);
    }
  };

  // Show pending activation message after successful signup
  if (isSubmitted) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'var(--bg-base)', padding: '2rem' }}>
        <div className="pending-activation">
          <div className="pending-activation-icon">
            <Clock size={32} />
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
            Account Pending Activation
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            Your account is pending admin approval. You'll be notified once activated.
          </p>
          <Link to="/login" className="btn btn-primary">
            Back to Login
          </Link>
        </div>
      </div>
    );
  }

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
            Join the PLM<br />
            <span>Engineering Platform</span>
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
            Create your account
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
            Get started with PLM today
          </p>

          <form onSubmit={handleSubmit(onSubmit)}>
            {/* Name */}
            <div className="form-group">
              <label htmlFor="name" className="form-label">
                Full Name
              </label>
              <input
                id="name"
                type="text"
                autoComplete="name"
                className="input"
                placeholder="John Doe"
                {...register('name')}
              />
              {errors.name && (
                <p className="form-error">{errors.name.message}</p>
              )}
            </div>

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
                  autoComplete="new-password"
                  className="input"
                  style={{ paddingRight: '2.5rem' }}
                  placeholder="Create a strong password"
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
              <PasswordStrengthMeter password={password} />
              {errors.password && (
                <p className="form-error">{errors.password.message}</p>
              )}
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label htmlFor="confirmPassword" className="form-label">
                Confirm Password
              </label>
              <div className="input-wrapper">
                <input
                  id="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  className="input"
                  style={{ paddingRight: '2.5rem' }}
                  placeholder="Confirm your password"
                  {...register('confirmPassword')}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="input-icon-right"
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="form-error">{errors.confirmPassword.message}</p>
              )}
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={isLoading}
              className="btn btn-primary"
              style={{ width: '100%', height: '2.75rem' }}
            >
              {isLoading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>

          {/* Login link */}
          <p style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--accent)', fontWeight: 500 }}>
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignupPage;
