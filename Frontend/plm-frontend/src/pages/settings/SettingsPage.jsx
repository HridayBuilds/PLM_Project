import { useState, useEffect } from 'react';
import {
  Plus,
  Edit,
  Trash2,
  Users,
  GitBranch,
  Check,
  X,
  ChevronRight,
} from 'lucide-react';
import clsx from 'clsx';
import toast from 'react-hot-toast';
import {
  SectionHeader,
  DataTable,
  StatusBadge,
  ConfirmModal,
} from '../../components/ui';
import { stageApi, userApi } from '../../api';
import { ROLES, getRoleDisplayName, getRoleBadgeColor } from '../../utils/roleGuards';
import dayjs from 'dayjs';

const SettingsPage = () => {
  const [activeTab, setActiveTab] = useState('stages');
  const [stages, setStages] = useState([]);
  const [users, setUsers] = useState([]);
  const [pendingUsers, setPendingUsers] = useState([]);
  const [approvers, setApprovers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Stage modal
  const [showStageModal, setShowStageModal] = useState(false);
  const [editingStage, setEditingStage] = useState(null);
  const [stageForm, setStageForm] = useState({
    name: '',
    order: 1,
    isFinalStage: false,
    requiresApproval: false,
    approverIds: [],
  });

  // Confirm modal
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    setIsLoading(true);
    try {
      if (activeTab === 'stages') {
        const [stagesData, approversData] = await Promise.all([
          stageApi.getStages(),
          userApi.getApprovers().catch(() => []),
        ]);
        setStages(stagesData.data || stagesData);
        setApprovers(approversData.data || approversData || []);
      } else {
        const [usersData, pendingData] = await Promise.all([
          userApi.getUsers(),
          userApi.getPendingUsers(),
        ]);
        setUsers(usersData.data || usersData);
        setPendingUsers(pendingData.data || pendingData);
      }
    } catch (error) {
      // Mock data
      if (activeTab === 'stages') {
        setStages([
          { id: 1, name: 'New', order: 1, isFinalStage: false, requiresApproval: false, approvers: [] },
          { id: 2, name: 'Approval', order: 2, isFinalStage: false, requiresApproval: true, approvers: ['Sarah', 'Mike'] },
          { id: 3, name: 'Done', order: 3, isFinalStage: true, requiresApproval: false, approvers: [] },
        ]);
        setApprovers([]);
      } else {
        setUsers([
          { id: 1, name: 'John Doe', email: 'john@company.com', role: 'ENGINEERING', status: 'Active', joinedAt: new Date().toISOString() },
          { id: 2, name: 'Jane Smith', email: 'jane@company.com', role: 'APPROVER', status: 'Active', joinedAt: new Date().toISOString() },
          { id: 3, name: 'Bob Wilson', email: 'bob@company.com', role: 'OPERATIONS', status: 'Active', joinedAt: new Date().toISOString() },
        ]);
        setPendingUsers([
          { id: 4, name: 'Alice Johnson', email: 'alice@company.com', createdAt: new Date().toISOString() },
        ]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveStage = async () => {
    try {
      if (editingStage) {
        await stageApi.updateStage(editingStage.id, stageForm);
        toast.success('Stage updated');
      } else {
        await stageApi.createStage(stageForm);
        toast.success('Stage created');
      }
      setShowStageModal(false);
      loadData();
    } catch (error) {
      // Error handled by interceptor
    }
  };

  const handleDeleteStage = async () => {
    try {
      await stageApi.deleteStage(deleteTarget.id);
      toast.success('Stage deleted');
      setShowDeleteModal(false);
      loadData();
    } catch (error) {
      // Error handled by interceptor
    }
  };

  const handleActivateUser = async (userId, role) => {
    try {
      await userApi.activateUser(userId, role);
      toast.success('User activated');
      loadData();
    } catch (error) {
      // Error handled by interceptor
    }
  };

  const handleUpdateRole = async (userId, newRole) => {
    try {
      await userApi.updateUserRole(userId, newRole);
      toast.success('Role updated');
      loadData();
    } catch (error) {
      // Error handled by interceptor
    }
  };

  const openStageModal = (stage = null) => {
    setEditingStage(stage);
    if (stage) {
      setStageForm({
        name: stage.name,
        order: stage.order,
        isFinalStage: stage.isFinalStage,
        requiresApproval: stage.requiresApproval,
        approverIds: stage.approverIds || [],
      });
    } else {
      setStageForm({
        name: '',
        order: stages.length + 1,
        isFinalStage: false,
        requiresApproval: false,
        approverIds: [],
      });
    }
    setShowStageModal(true);
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-[var(--text-primary)] mb-6">Settings</h1>

      {/* Tab navigation */}
      <div className="flex gap-1 p-1 bg-[var(--bg-elevated)] rounded-lg mb-6 w-fit">
        <button
          onClick={() => setActiveTab('stages')}
          className={clsx(
            'flex items-center gap-2 px-4 py-2 rounded text-sm font-medium transition-colors',
            activeTab === 'stages'
              ? 'bg-[var(--accent)] text-white'
              : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
          )}
        >
          <GitBranch size={16} />
          ECO Stages
        </button>
        <button
          onClick={() => setActiveTab('users')}
          className={clsx(
            'flex items-center gap-2 px-4 py-2 rounded text-sm font-medium transition-colors',
            activeTab === 'users'
              ? 'bg-[var(--accent)] text-white'
              : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
          )}
        >
          <Users size={16} />
          User Management
        </button>
      </div>

      {activeTab === 'stages' ? (
        <div>
          {/* Stage Pipeline Visualizer */}
          <div className="bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg p-6 mb-6">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              Stage Pipeline
            </h3>
            <div className="flex items-center gap-2 overflow-x-auto pb-2">
              {stages
                .sort((a, b) => a.order - b.order)
                .map((stage, index) => (
                  <div key={stage.id} className="flex items-center">
                    <div className="px-4 py-2 bg-[var(--bg-elevated)] border border-[var(--bg-border)] rounded-lg text-sm font-medium text-[var(--text-primary)] whitespace-nowrap">
                      {stage.name}
                      {stage.isFinalStage && (
                        <span className="ml-2 text-xs text-[var(--green)]">(Final)</span>
                      )}
                    </div>
                    {index < stages.length - 1 && (
                      <ChevronRight size={20} className="text-[var(--text-muted)] mx-1" />
                    )}
                  </div>
                ))}
            </div>
          </div>

          {/* Stages Table */}
          <SectionHeader
            title="Stages Configuration"
            action={
              <button onClick={() => openStageModal()} className="btn btn-primary">
                <Plus size={18} />
                Add Stage
              </button>
            }
          />

          <DataTable
            columns={[
              { key: 'name', header: 'Stage Name' },
              { key: 'order', header: 'Order', align: 'center' },
              {
                key: 'isFinalStage',
                header: 'Is Final Stage',
                align: 'center',
                render: (v) => <StatusBadge status={v ? 'yes' : 'no'} />,
              },
              {
                key: 'requiresApproval',
                header: 'Requires Approval',
                align: 'center',
                render: (v) => <StatusBadge status={v ? 'yes' : 'no'} />,
              },
              {
                key: 'approvers',
                header: 'Approvers Assigned',
                render: (v) => v?.join(', ') || '—',
              },
              {
                key: 'actions',
                header: 'Actions',
                sortable: false,
                render: (_, row) => (
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => openStageModal(row)}
                      className="p-1.5 hover:bg-[var(--bg-elevated)] rounded text-[var(--text-secondary)]"
                    >
                      <Edit size={16} />
                    </button>
                    <button
                      onClick={() => {
                        setDeleteTarget(row);
                        setShowDeleteModal(true);
                      }}
                      className="p-1.5 hover:bg-[var(--red-dim)] rounded text-[var(--red)]"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ),
              },
            ]}
            data={stages}
            isLoading={isLoading}
            pagination={false}
          />
        </div>
      ) : (
        <div className="space-y-8">
          {/* Pending Activations */}
          {pendingUsers.length > 0 && (
            <div>
              <SectionHeader
                title="Pending Activations"
                count={pendingUsers.length}
              />
              <div className="bg-[var(--yellow-dim)] border border-[var(--yellow)] rounded-lg p-4 mb-4">
                <p className="text-sm text-[var(--text-primary)]">
                  {pendingUsers.length} user(s) waiting for activation
                </p>
              </div>
              <DataTable
                columns={[
                  { key: 'name', header: 'Name' },
                  { key: 'email', header: 'Email' },
                  {
                    key: 'createdAt',
                    header: 'Requested At',
                    render: (v) => dayjs(v).fromNow(),
                  },
                  {
                    key: 'actions',
                    header: 'Action',
                    sortable: false,
                    render: (_, row) => (
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleActivateUser(row.id, 'ENGINEERING_USER')}
                          className="btn btn-success btn-sm"
                        >
                          <Check size={14} />
                          Activate
                        </button>
                        <select
                          onChange={(e) => {
                            if (e.target.value) {
                              handleActivateUser(row.id, e.target.value);
                            }
                          }}
                          className="input py-1 px-2 text-sm w-32"
                        >
                          <option value="">Select Role</option>
                          <option value="ENGINEERING_USER">Engineering</option>
                          <option value="APPROVER">Approver</option>
                          <option value="OPERATIONS_USER">Operations</option>
                          <option value="ADMIN">Admin</option>
                        </select>
                      </div>
                    ),
                  },
                ]}
                data={pendingUsers}
                pagination={false}
              />
            </div>
          )}

          {/* All Users */}
          <div>
            <SectionHeader
              title="All Users"
              count={users.length}
            />
            <DataTable
              columns={[
                { key: 'name', header: 'Name' },
                { key: 'email', header: 'Email' },
                {
                  key: 'role',
                  header: 'Role',
                  render: (v, row) => (
                    <select
                      value={v}
                      onChange={(e) => handleUpdateRole(row.id, e.target.value)}
                      className={clsx(
                        'px-2 py-1 text-xs font-medium rounded border-0 cursor-pointer',
                        `badge-${getRoleBadgeColor(v)}`
                      )}
                    >
                      <option value="ENGINEERING_USER">Engineering</option>
                      <option value="APPROVER">Approver</option>
                      <option value="OPERATIONS_USER">Operations</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  ),
                },
                {
                  key: 'status',
                  header: 'Status',
                  render: (v) => <StatusBadge status={v} />,
                },
                {
                  key: 'joinedAt',
                  header: 'Joined',
                  render: (v) => dayjs(v).format('MMM D, YYYY'),
                },
                {
                  key: 'actions',
                  header: 'Actions',
                  sortable: false,
                  render: (_, row) => (
                    <button
                      onClick={() => {
                        setDeleteTarget(row);
                        setShowDeleteModal(true);
                      }}
                      className="text-sm text-[var(--red)] hover:underline"
                    >
                      Deactivate
                    </button>
                  ),
                },
              ]}
              data={users}
              isLoading={isLoading}
            />
          </div>
        </div>
      )}

      {/* Stage Modal */}
      {showStageModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setShowStageModal(false)}
          />
          <div className="relative bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-xl p-6 w-full max-w-md mx-4 shadow-2xl">
            <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-4">
              {editingStage ? 'Edit Stage' : 'Add Stage'}
            </h3>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[var(--text-primary)] mb-1.5">
                  Stage Name
                </label>
                <input
                  type="text"
                  className="input"
                  value={stageForm.name}
                  onChange={(e) => setStageForm((p) => ({ ...p, name: e.target.value }))}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-[var(--text-primary)] mb-1.5">
                  Order
                </label>
                <input
                  type="number"
                  min="1"
                  className="input"
                  value={stageForm.order}
                  onChange={(e) => setStageForm((p) => ({ ...p, order: parseInt(e.target.value) }))}
                />
              </div>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={stageForm.isFinalStage}
                  onChange={(e) => setStageForm((p) => ({ ...p, isFinalStage: e.target.checked }))}
                  className="w-4 h-4 rounded"
                />
                <span className="text-sm text-[var(--text-primary)]">Is Final Stage</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={stageForm.requiresApproval}
                  onChange={(e) => setStageForm((p) => ({ ...p, requiresApproval: e.target.checked }))}
                  className="w-4 h-4 rounded"
                />
                <span className="text-sm text-[var(--text-primary)]">Requires Approval</span>
              </label>

              {stageForm.requiresApproval && (
                <div>
                  <label className="block text-sm font-medium text-[var(--text-primary)] mb-1.5">
                    Assign Approvers
                  </label>
                  <div className="space-y-2 max-h-40 overflow-y-auto border border-[var(--bg-border)] rounded-lg p-2">
                    {approvers.length === 0 ? (
                      <p className="text-sm text-[var(--text-muted)] py-2 text-center">
                        No approvers available. Assign users the Approver role first.
                      </p>
                    ) : (
                      approvers.map((approver) => (
                        <label key={approver.id} className="flex items-center gap-2 cursor-pointer p-1 hover:bg-[var(--bg-elevated)] rounded">
                          <input
                            type="checkbox"
                            checked={stageForm.approverIds.includes(approver.id)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setStageForm((p) => ({
                                  ...p,
                                  approverIds: [...p.approverIds, approver.id],
                                }));
                              } else {
                                setStageForm((p) => ({
                                  ...p,
                                  approverIds: p.approverIds.filter((id) => id !== approver.id),
                                }));
                              }
                            }}
                            className="w-4 h-4 rounded"
                          />
                          <span className="text-sm text-[var(--text-primary)]">
                            {approver.name || `${approver.firstName} ${approver.lastName}`}
                          </span>
                          <span className="text-xs text-[var(--text-muted)]">
                            ({approver.email})
                          </span>
                        </label>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowStageModal(false)}
                className="btn btn-secondary flex-1"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveStage}
                className="btn btn-primary flex-1"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirm Modal */}
      <ConfirmModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={activeTab === 'stages' ? handleDeleteStage : () => {}}
        title={activeTab === 'stages' ? 'Delete Stage' : 'Deactivate User'}
        message={
          activeTab === 'stages'
            ? `Are you sure you want to delete "${deleteTarget?.name}"? This action cannot be undone.`
            : `Are you sure you want to deactivate "${deleteTarget?.name}"?`
        }
        confirmText={activeTab === 'stages' ? 'Delete' : 'Deactivate'}
        variant="danger"
      />
    </div>
  );
};

export default SettingsPage;
