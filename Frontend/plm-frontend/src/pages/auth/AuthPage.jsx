import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, CheckCircle, Shield, GitBranch, Mail } from 'lucide-react';
import toast from 'react-hot-toast';
import clsx from 'clsx';
import { PasswordStrengthMeter } from '../../components/auth';
import useAuthStore from '../../context/authStore';
import authApi from '../../api/authApi';

const loginSchema = z.object({
  loginId: z.string().min(1, 'Login ID is required'),
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().optional(),
});

const signupSchema = z
  .object({
    loginId: z
      .string()
      .min(6, 'Login ID must be at least 6 characters')
      .max(12, 'Login ID must be at most 12 characters')
      .regex(/^ECV/, 'Login ID must start with ECV'),
    email: z.string().email('Please enter a valid email address'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/[0-9]/, 'Password must contain at least one number')
      .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

const AuthPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuthStore();

  const isSignupRoute = location.pathname === '/signup';
  const [activeTab, setActiveTab] = useState(isSignupRoute ? 'signup' : 'login');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const from = location.state?.from?.pathname || '/dashboard';

  // Login form
  const loginForm = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      loginId: '',
      password: '',
      rememberMe: false,
    },
  });

  // Signup form
  const signupForm = useForm({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      loginId: 'ECV',
      email: '',
      password: '',
      confirmPassword: '',
    },
  });

  const signupPassword = signupForm.watch('password', '');

  const handleLogin = async (data) => {
    setIsLoading(true);
    try {
      const response = await authApi.login(data.loginId, data.password);
      // Map backend response to auth store format
      const user = {
        id: response.userId,
        loginId: response.loginId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        name: `${response.firstName || ''} ${response.lastName || ''}`.trim() || response.loginId,
        role: response.role,
        isVerified: response.isVerified,
      };
      login({
        user,
        token: response.accessToken,
        role: response.role,
      });
      toast.success('Welcome back!');
      navigate(from, { replace: true });
    } catch (error) {
      toast.error(error.response?.data?.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSignup = async (data) => {
    setIsLoading(true);
    try {
      await authApi.signup({
        loginId: data.loginId,
        email: data.email,
        password: data.password,
        confirmPassword: data.confirmPassword,
      });
      setIsSubmitted(true);
      toast.success('Account created successfully!');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Signup failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setShowPassword(false);
    setShowConfirmPassword(false);
    navigate(tab === 'signup' ? '/signup' : '/login', { replace: true });
  };

  // Show verification email sent message after successful signup
  if (isSubmitted) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: 'var(--bg-base)',
          padding: '2rem',
        }}
      >
        <div style={{ maxWidth: '28rem', textAlign: 'center' }}>
          <div style={{
            width: '5rem',
            height: '5rem',
            borderRadius: '50%',
            backgroundColor: 'var(--accent-dim)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 1.5rem',
            color: 'var(--accent)',
          }}>
            <Mail size={40} />
          </div>
          <h2
            style={{
              fontSize: '1.5rem',
              fontWeight: 700,
              color: 'var(--text-primary)',
              marginBottom: '0.75rem',
            }}
          >
            Check Your Email
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', lineHeight: 1.6 }}>
            We've sent a verification link to your email address. Please click the link to verify your account and start using Ecova.
          </p>
          <button
            onClick={() => {
              setIsSubmitted(false);
              setActiveTab('login');
              navigate('/login', { replace: true });
            }}
            className="btn btn-primary"
            style={{ width: '100%', height: '2.75rem' }}
          >
            Back to Login
          </button>
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
              <span>E</span>
            </div>
            <span className="auth-logo-text">Ecova</span>
          </div>

          {/* Tagline */}
          <h1 className="auth-tagline">
            Engineering Changes,
            <br />
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
              <span>E</span>
            </div>
            <span className="mobile-logo-text">Ecova</span>
          </div>

          {/* Tabs */}
          <div className="auth-tabs">
            <button
              type="button"
              className={clsx('auth-tab', activeTab === 'login' && 'active')}
              onClick={() => handleTabChange('login')}
            >
              Login
            </button>
            <button
              type="button"
              className={clsx('auth-tab', activeTab === 'signup' && 'active')}
              onClick={() => handleTabChange('signup')}
            >
              Sign Up
            </button>
          </div>

          {/* Login Form */}
          {activeTab === 'login' && (
            <form onSubmit={loginForm.handleSubmit(handleLogin)}>
              {/* Login ID */}
              <div className="form-group">
                <label htmlFor="loginId" className="form-label">
                  Login ID
                </label>
                <input
                  id="loginId"
                  type="text"
                  autoComplete="username"
                  className="input"
                  placeholder="ECV123456"
                  {...loginForm.register('loginId')}
                />
                {loginForm.formState.errors.loginId && (
                  <p className="form-error">{loginForm.formState.errors.loginId.message}</p>
                )}
              </div>

              {/* Password */}
              <div className="form-group">
                <label htmlFor="loginPassword" className="form-label">
                  Password
                </label>
                <div className="input-wrapper">
                  <input
                    id="loginPassword"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    className="input"
                    style={{ paddingRight: '2.5rem' }}
                    placeholder="Enter your password"
                    {...loginForm.register('password')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="input-icon-right"
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {loginForm.formState.errors.password && (
                  <p className="form-error">{loginForm.formState.errors.password.message}</p>
                )}
              </div>

              {/* Remember me & Forgot password */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <label className="remember-me-label">
                  <input
                    type="checkbox"
                    {...loginForm.register('rememberMe')}
                  />
                  <span>Remember me</span>
                </label>
                <Link
                  to="/forgot-password"
                  style={{ fontSize: '0.9375rem', color: 'var(--accent)' }}
                >
                  Forgot Password?
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
          )}

          {/* Signup Form */}
          {activeTab === 'signup' && (
            <form onSubmit={signupForm.handleSubmit(handleSignup)}>
              {/* Login ID */}
              <div className="form-group">
                <label htmlFor="signupLoginId" className="form-label">
                  Login ID
                </label>
                <input
                  id="signupLoginId"
                  type="text"
                  autoComplete="username"
                  className="input"
                  placeholder="ECV123456"
                  {...signupForm.register('loginId')}
                />
                <p className="form-hint">Must start with ECV (6-12 characters)</p>
                {signupForm.formState.errors.loginId && (
                  <p className="form-error">{signupForm.formState.errors.loginId.message}</p>
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
                  {...signupForm.register('email')}
                />
                {signupForm.formState.errors.email && (
                  <p className="form-error">{signupForm.formState.errors.email.message}</p>
                )}
              </div>

              {/* Password */}
              <div className="form-group">
                <label htmlFor="signupPassword" className="form-label">
                  Password
                </label>
                <div className="input-wrapper">
                  <input
                    id="signupPassword"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    className="input"
                    style={{ paddingRight: '2.5rem' }}
                    placeholder="Create a strong password"
                    {...signupForm.register('password')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="input-icon-right"
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                <PasswordStrengthMeter password={signupPassword} />
              </div>

              {/* Confirm Password */}
              <div className="form-group">
                <label htmlFor="confirmPassword" className="form-label">
                  Re-enter Password
                </label>
                <div className="input-wrapper">
                  <input
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    className="input"
                    style={{ paddingRight: '2.5rem' }}
                    placeholder="Confirm your password"
                    {...signupForm.register('confirmPassword')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="input-icon-right"
                  >
                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {signupForm.formState.errors.confirmPassword && (
                  <p className="form-error">{signupForm.formState.errors.confirmPassword.message}</p>
                )}
              </div>

              {/* Submit */}
              <button
                type="submit"
                disabled={isLoading}
                className="btn btn-primary"
                style={{ width: '100%', height: '2.75rem', marginTop: '0.5rem' }}
              >
                {isLoading ? 'Creating account...' : 'Create Account'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
