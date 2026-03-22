import axiosInstance from './axiosInstance';

export const reportApi = {
  // Dashboard stats aggregated from multiple sources
  getDashboardStats: async (role) => {
    try {
      // Aggregate stats based on role
      const stats = {};

      if (role === 'ENGINEERING_USER' || role === 'ENGINEERING') {
        // Get user's ECOs
        const myEcos = await axiosInstance.get('/ecos/my');
        const ecoList = myEcos.data?.ecos || myEcos.data?.content || myEcos.data || [];
        const ecoArray = Array.isArray(ecoList) ? ecoList : [];
        // Backend uses: DRAFT, IN_PROGRESS, APPROVED, APPLIED, CANCELLED
        stats.myOpenEcos = ecoArray.filter(e => e.status !== 'APPLIED' && e.status !== 'CANCELLED').length;
        stats.drafts = ecoArray.filter(e => e.status === 'DRAFT').length;
        stats.awaitingApproval = ecoArray.filter(e => e.status === 'IN_PROGRESS').length;
        stats.approvedThisMonth = ecoArray.filter(e => e.status === 'APPROVED' || e.status === 'APPLIED').length;
      } else if (role === 'APPROVER') {
        const pending = await axiosInstance.get('/ecos/pending-approvals');
        const pendingList = pending.data?.ecos || pending.data?.content || pending.data || [];
        const pendingArray = Array.isArray(pendingList) ? pendingList : [];
        stats.pendingReview = pendingArray.length;
        stats.approvedToday = 0;
        stats.rejected = 0;
        stats.totalReviewed = 0;
      } else if (role === 'ADMIN') {
        const [usersRes, productsRes] = await Promise.all([
          axiosInstance.get('/users/all').catch(() => ({ data: [] })),
          axiosInstance.get('/products').catch(() => ({ data: { products: [], totalElements: 0 } })),
        ]);
        const users = Array.isArray(usersRes.data) ? usersRes.data : [];
        const pendingUsers = users.filter(u => !u.isActive || u.status === 'PENDING');
        stats.totalUsers = users.length;
        stats.pendingSignups = pendingUsers.length;
        stats.totalProducts = productsRes.data?.totalElements || productsRes.data?.products?.length || 0;
        stats.activeEcos = 0;
      } else {
        // OPERATIONS role
        const productsRes = await axiosInstance.get('/products/active').catch(() => ({ data: { products: [], totalElements: 0 } }));
        stats.activeProducts = productsRes.data?.totalElements || productsRes.data?.products?.length || 0;
        stats.activeBoms = 0;
        stats.recentUpdates = 0;
      }

      return stats;
    } catch (error) {
      console.error('Failed to get dashboard stats:', error);
      throw error;
    }
  },

  // ECO Report - get all ECOs with summary info
  getEcoReport: async (params = {}) => {
    const response = await axiosInstance.get('/reports/eco', { params });
    return response.data;
  },

  // Product Version History
  getProductVersionHistory: async (productId) => {
    const response = await axiosInstance.get(`/reports/products/${productId}/versions`);
    return response.data;
  },

  // BOM Change History for a product
  getBomChangeHistory: async (productId) => {
    const response = await axiosInstance.get(`/reports/products/${productId}/bom-changes`);
    return response.data;
  },

  // Archived Products list
  getArchivedProducts: async () => {
    const response = await axiosInstance.get('/reports/products/archived');
    return response.data;
  },

  // Product-BOM Matrix
  getProductBomMatrix: async () => {
    const response = await axiosInstance.get('/reports/product-bom-matrix');
    return response.data;
  },

  // Audit Logs
  getEcoAuditLogs: async (ecoId) => {
    const response = await axiosInstance.get(`/reports/audit-logs/eco/${ecoId}`);
    return response.data;
  },

  getUserAuditLogs: async (userId) => {
    const response = await axiosInstance.get(`/reports/audit-logs/user/${userId}`);
    return response.data;
  },

  searchAuditLogs: async (params = {}) => {
    const response = await axiosInstance.get('/reports/audit-logs', { params });
    return response.data;
  },

  getRecentActivity: async () => {
    const response = await axiosInstance.get('/reports/audit-logs/recent');
    return response.data;
  },
};

export default reportApi;
