import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { User, Shield, Key, Eye, EyeOff, Save, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import useAuthStore from '../../context/authStore';
import authApi from '../../api/authApi';

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/[0-9]/, 'Password must contain at least one number')
      .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
    confirmNewPassword: z.string(),
  })
  .refine((data) => data.newPassword === data.confirmNewPassword, {
    message: 'Passwords do not match',
    path: ['confirmNewPassword'],
  });

const profileSchema = z.object({
  firstName: z.string().optional(),
  lastName: z.string().optional(),
  phone: z.string().optional(),
});

const ProfilePage = () => {
  const { user, updateUser } = useAuthStore();
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // Profile form
  const profileForm = useForm({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phone: user?.phone || '',
    },
  });

  // Password form
  const passwordForm = useForm({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmNewPassword: '',
    },
  });

  const handleUpdateProfile = async (data) => {
    setIsUpdatingProfile(true);
    try {
      await authApi.updateProfile(data);
      updateUser({
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
        name: `${data.firstName} ${data.lastName}`.trim(),
      });
      toast.success('Profile updated successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const handleChangePassword = async (data) => {
    setIsChangingPassword(true);
    try {
      await authApi.changePassword(
        data.currentPassword,
        data.newPassword,
        data.confirmNewPassword
      );
      toast.success('Password changed successfully');
      passwordForm.reset();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to change password');
    } finally {
      setIsChangingPassword(false);
    }
  };

  const sectionStyle = {
    backgroundColor: 'var(--bg-surface)',
    border: '1px solid var(--bg-border)',
    borderRadius: '8px',
    marginBottom: '1.5rem',
    overflow: 'hidden',
  };

  const sectionHeaderStyle = {
    padding: '1rem 1.25rem',
    borderBottom: '1px solid var(--bg-border)',
    backgroundColor: 'var(--bg-elevated)',
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
  };

  const sectionTitleStyle = {
    fontSize: '1rem',
    fontWeight: 600,
    color: 'var(--text-primary)',
  };

  const sectionBodyStyle = {
    padding: '1.25rem',
  };

  const infoRowStyle = {
    display: 'flex',
    alignItems: 'center',
    padding: '0.75rem 0',
    borderBottom: '1px solid var(--bg-border)',
  };

  const infoLabelStyle = {
    width: '140px',
    fontSize: '0.875rem',
    color: 'var(--text-muted)',
    flexShrink: 0,
  };

  const infoValueStyle = {
    fontSize: '0.875rem',
    color: 'var(--text-primary)',
    fontWeight: 500,
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
          Profile Settings
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9375rem' }}>
          Manage your account information and security settings
        </p>
      </div>

      {/* Account Information (Read-only) */}
      <div style={sectionStyle}>
        <div style={sectionHeaderStyle}>
          <Shield size={20} style={{ color: 'var(--accent)' }} />
          <span style={sectionTitleStyle}>Account Information</span>
        </div>
        <div style={sectionBodyStyle}>
          <div style={infoRowStyle}>
            <span style={infoLabelStyle}>Login ID</span>
            <span style={{ ...infoValueStyle, fontFamily: 'var(--font-mono)' }}>{user?.loginId || 'N/A'}</span>
          </div>
          <div style={infoRowStyle}>
            <span style={infoLabelStyle}>Email</span>
            <span style={infoValueStyle}>{user?.email || 'N/A'}</span>
          </div>
          <div style={{ ...infoRowStyle, borderBottom: 'none' }}>
            <span style={infoLabelStyle}>Role</span>
            <span style={{
              padding: '0.25rem 0.625rem',
              fontSize: '0.75rem',
              fontWeight: 500,
              borderRadius: '4px',
              backgroundColor: 'var(--accent-dim)',
              color: 'var(--accent)',
              textTransform: 'capitalize',
            }}>
              {user?.role?.toLowerCase().replace('role_', '') || 'User'}
            </span>
          </div>
          {/* Role Change Info */}
          <div style={{
            marginTop: '1rem',
            padding: '0.75rem',
            backgroundColor: 'var(--yellow-dim)',
            borderRadius: '6px',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '0.75rem',
          }}>
            <AlertCircle size={18} style={{ color: 'var(--yellow)', flexShrink: 0, marginTop: '2px' }} />
            <div style={{ fontSize: '0.8125rem', color: 'var(--text-primary)' }}>
              <strong>Need to change your role?</strong>
              <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                Role changes require administrator approval. Contact your admin to request a role change.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Personal Information */}
      <div style={sectionStyle}>
        <div style={sectionHeaderStyle}>
          <User size={20} style={{ color: 'var(--accent)' }} />
          <span style={sectionTitleStyle}>Personal Information</span>
        </div>
        <div style={sectionBodyStyle}>
          <form onSubmit={profileForm.handleSubmit(handleUpdateProfile)}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              {/* First Name */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label htmlFor="firstName" className="form-label">First Name</label>
                <input
                  id="firstName"
                  type="text"
                  className="input"
                  placeholder="Enter your first name"
                  {...profileForm.register('firstName')}
                />
                {profileForm.formState.errors.firstName && (
                  <p className="form-error">{profileForm.formState.errors.firstName.message}</p>
                )}
              </div>

              {/* Last Name */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label htmlFor="lastName" className="form-label">Last Name</label>
                <input
                  id="lastName"
                  type="text"
                  className="input"
                  placeholder="Enter your last name"
                  {...profileForm.register('lastName')}
                />
                {profileForm.formState.errors.lastName && (
                  <p className="form-error">{profileForm.formState.errors.lastName.message}</p>
                )}
              </div>
            </div>

            {/* Phone */}
            <div className="form-group" style={{ marginBottom: '1.25rem' }}>
              <label htmlFor="phone" className="form-label">Phone Number (Optional)</label>
              <input
                id="phone"
                type="tel"
                className="input"
                placeholder="+1 (555) 000-0000"
                {...profileForm.register('phone')}
              />
            </div>

            <button
              type="submit"
              disabled={isUpdatingProfile}
              className="btn btn-primary"
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
            >
              <Save size={16} />
              {isUpdatingProfile ? 'Saving...' : 'Save Changes'}
            </button>
          </form>
        </div>
      </div>

      {/* Change Password */}
      <div style={sectionStyle}>
        <div style={sectionHeaderStyle}>
          <Key size={20} style={{ color: 'var(--accent)' }} />
          <span style={sectionTitleStyle}>Change Password</span>
        </div>
        <div style={sectionBodyStyle}>
          <form onSubmit={passwordForm.handleSubmit(handleChangePassword)}>
            {/* Current Password */}
            <div className="form-group" style={{ marginBottom: '1rem' }}>
              <label htmlFor="currentPassword" className="form-label">Current Password</label>
              <div className="input-wrapper">
                <input
                  id="currentPassword"
                  type={showCurrentPassword ? 'text' : 'password'}
                  className="input"
                  style={{ paddingRight: '2.5rem' }}
                  placeholder="Enter your current password"
                  {...passwordForm.register('currentPassword')}
                />
                <button
                  type="button"
                  onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                  className="input-icon-right"
                >
                  {showCurrentPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {passwordForm.formState.errors.currentPassword && (
                <p className="form-error">{passwordForm.formState.errors.currentPassword.message}</p>
              )}
            </div>

            {/* New Password */}
            <div className="form-group" style={{ marginBottom: '1rem' }}>
              <label htmlFor="newPassword" className="form-label">New Password</label>
              <div className="input-wrapper">
                <input
                  id="newPassword"
                  type={showNewPassword ? 'text' : 'password'}
                  className="input"
                  style={{ paddingRight: '2.5rem' }}
                  placeholder="Enter new password"
                  {...passwordForm.register('newPassword')}
                />
                <button
                  type="button"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                  className="input-icon-right"
                >
                  {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {passwordForm.formState.errors.newPassword && (
                <p className="form-error">{passwordForm.formState.errors.newPassword.message}</p>
              )}
              <p className="form-hint" style={{ marginTop: '0.375rem' }}>
                Must be 8+ characters with uppercase, lowercase, number, and special character
              </p>
            </div>

            {/* Confirm New Password */}
            <div className="form-group" style={{ marginBottom: '1.25rem' }}>
              <label htmlFor="confirmNewPassword" className="form-label">Confirm New Password</label>
              <div className="input-wrapper">
                <input
                  id="confirmNewPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  className="input"
                  style={{ paddingRight: '2.5rem' }}
                  placeholder="Confirm new password"
                  {...passwordForm.register('confirmNewPassword')}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="input-icon-right"
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {passwordForm.formState.errors.confirmNewPassword && (
                <p className="form-error">{passwordForm.formState.errors.confirmNewPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isChangingPassword}
              className="btn btn-primary"
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
            >
              <Key size={16} />
              {isChangingPassword ? 'Changing...' : 'Change Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
