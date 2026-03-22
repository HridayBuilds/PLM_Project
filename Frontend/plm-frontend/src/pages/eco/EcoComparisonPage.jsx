import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import {
  ChevronRight,
  ArrowUp,
  ArrowDown,
  Minus,
  Plus,
  Trash2,
  Clock,
  Package,
  Wrench,
  TrendingUp,
  TrendingDown,
  DollarSign,
} from 'lucide-react';
import clsx from 'clsx';
import { ecoApi } from '../../api';
import dayjs from 'dayjs';

const EcoComparisonPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [eco, setEco] = useState(null);
  const [comparison, setComparison] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadComparison();
  }, [id]);

  const loadComparison = async () => {
    setIsLoading(true);
    try {
      const [ecoData, comparisonData] = await Promise.all([
        ecoApi.getEcoById(id),
        ecoApi.getComparison(id),
      ]);
      setEco(ecoData);
      setComparison(comparisonData);
    } catch (error) {
      console.error('Failed to load comparison:', error);
      // Mock data for development
      setEco({
        id,
        reference: 'ECO-2024-001',
        title: 'Update Chair Frame Material',
        ecoType: 'BOM',
        productName: 'Office Chair Pro',
        currentStage: { name: 'In Progress' },
        versionUpdate: true,
        effectiveDate: dayjs().add(7, 'day').toISOString(),
        createdBy: { firstName: 'John', lastName: 'Doe' },
        createdAt: dayjs().subtract(2, 'day').toISOString(),
      });
      setComparison({
        components: [
          {
            id: 1,
            name: 'Steel Frame',
            version1Qty: 4,
            version2Qty: 6,
            unit: 'pcs',
            changeType: 'UPDATE',
          },
          {
            id: 2,
            name: 'Cushion Foam',
            version1Qty: 2,
            version2Qty: 2,
            unit: 'pcs',
            changeType: 'UNCHANGED',
          },
          {
            id: 3,
            name: 'Arm Rest',
            version1Qty: 2,
            version2Qty: 4,
            unit: 'pcs',
            changeType: 'UPDATE',
          },
          {
            id: 4,
            name: 'Wheel Caster',
            version1Qty: 5,
            version2Qty: 5,
            unit: 'pcs',
            changeType: 'UNCHANGED',
          },
          {
            id: 5,
            name: 'Support Bracket',
            version1Qty: 0,
            version2Qty: 2,
            unit: 'pcs',
            changeType: 'ADD',
          },
          {
            id: 6,
            name: 'Old Connector',
            version1Qty: 4,
            version2Qty: 0,
            unit: 'pcs',
            changeType: 'REMOVE',
          },
        ],
        operations: [
          {
            id: 1,
            name: 'Assembly Phase A',
            workCenter: 'Assembly Line 1',
            version1Duration: 45,
            version2Duration: 60,
            changeType: 'UPDATE',
          },
          {
            id: 2,
            name: 'Quality QC Check',
            workCenter: 'QC Station',
            version1Duration: 15,
            version2Duration: 20,
            changeType: 'UPDATE',
          },
          {
            id: 3,
            name: 'Packaging',
            workCenter: 'Packing Area',
            version1Duration: 10,
            version2Duration: 10,
            changeType: 'UNCHANGED',
          },
        ],
        summary: {
          totalPartsDelta: 4,
          productionCycleDelta: 20,
          estimatedCostImpact: 1250,
        },
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getDeltaBadge = (version1, version2, changeType) => {
    if (changeType === 'ADD') {
      return (
        <span className="delta-badge new">
          <Plus size={12} />
          New
        </span>
      );
    }
    if (changeType === 'REMOVE') {
      return (
        <span className="delta-badge removed">
          <Trash2 size={12} />
          Removed
        </span>
      );
    }
    if (version1 === version2 || changeType === 'UNCHANGED') {
      return (
        <span className="delta-badge unchanged">
          <Minus size={12} />
          Unchanged
        </span>
      );
    }
    const delta = version2 - version1;
    if (delta > 0) {
      return (
        <span className="delta-badge increase">
          <ArrowUp size={12} />+{delta}
        </span>
      );
    }
    return (
      <span className="delta-badge decrease">
        <ArrowDown size={12} />
        {delta}
      </span>
    );
  };

  const maxDuration = useMemo(() => {
    if (!comparison?.operations) return 60;
    return Math.max(
      ...comparison.operations.flatMap((op) => [op.version1Duration, op.version2Duration])
    );
  }, [comparison]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-pulse text-[var(--text-muted)]">Loading comparison...</div>
      </div>
    );
  }

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-[var(--text-secondary)] mb-4">
        <Link to="/eco" className="hover:text-[var(--text-primary)]">
          Change Orders
        </Link>
        <ChevronRight size={16} />
        <Link to={`/eco/${id}`} className="hover:text-[var(--text-primary)]">
          {eco?.reference || `ECO-${id}`}
        </Link>
        <ChevronRight size={16} />
        <span className="text-[var(--text-primary)]">Changes Comparison</span>
      </nav>

      {/* Header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[var(--text-primary)] mb-1">
            Changes Comparison
          </h1>
          <p className="text-[var(--text-secondary)]">
            {eco?.title} - {eco?.productName}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to={`/eco/${id}`} className="btn btn-secondary">
            View ECO Details
          </Link>
        </div>
      </div>

      {/* Version Labels */}
      <div className="flex items-center gap-4 mb-6">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-[var(--text-muted)]" />
          <span className="text-sm text-[var(--text-secondary)]">Version 1 (Current)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-[var(--accent)]" />
          <span className="text-sm text-[var(--text-secondary)]">Version 2 (Proposed)</span>
        </div>
      </div>

      {/* Product Attribute Changes */}
      {comparison?.type === 'Product' && (
        <div className="comparison-section">
          <div className="comparison-header">
            <h2 className="comparison-title">
              <Package size={18} className="text-[var(--accent)]" />
              Product Attribute Changes
            </h2>
          </div>
          {comparison.changes?.length > 0 ? (
            <table className="comparison-table w-full">
              <thead>
                <tr>
                  <th className="text-left py-3 px-4">Attribute</th>
                  <th className="text-center py-3 px-4">Version 1</th>
                  <th className="text-center py-3 px-4">Version 2</th>
                  <th className="text-center py-3 px-4">Change Type</th>
                </tr>
              </thead>
              <tbody>
                {comparison.changes.map((change, idx) => (
                  <tr key={idx} className="border-t border-[var(--bg-border)]">
                    <td className="font-medium text-[var(--text-primary)] py-3 px-4">{change.field}</td>
                    <td className="text-center text-[var(--text-muted)] py-3 px-4">{change.oldValue || '—'}</td>
                    <td className="text-center text-[var(--accent)] py-3 px-4">{change.newValue || '—'}</td>
                    <td className="text-center py-3 px-4">
                      <span className={clsx(
                        'delta-badge justify-center',
                        change.changeType === 'ADDED' ? 'new' : 
                        change.changeType === 'REMOVED' ? 'removed' : 'increase'
                      )}>
                        {change.changeType === 'MODIFIED' ? 'UPDATED' : change.changeType}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-6 text-center text-[var(--text-muted)] border-t border-[var(--bg-border)]">
              No product attributes were changed in this ECO.
            </div>
          )}
        </div>
      )}

      {/* Components Breakdown */}
      {comparison?.type !== 'Product' && (
      <div className="comparison-section">
        <div className="comparison-header">
          <h2 className="comparison-title">
            <Package size={18} className="text-[var(--accent)]" />
            Components Breakdown
          </h2>
        </div>
        <table className="comparison-table">
          <thead>
            <tr>
              <th>Part Name</th>
              <th className="text-center">Version 1</th>
              <th className="text-center">Version 2</th>
              <th className="text-center">Delta</th>
            </tr>
          </thead>
          <tbody>
            {comparison?.components?.map((component) => (
              <tr key={component.id}>
                <td className="font-medium text-[var(--text-primary)]">{component.name}</td>
                <td className="text-center text-[var(--text-muted)]">
                  {component.version1Qty} {component.unit}
                </td>
                <td className="text-center text-[var(--accent)]">
                  {component.version2Qty} {component.unit}
                </td>
                <td className="text-center">
                  {getDeltaBadge(
                    component.version1Qty,
                    component.version2Qty,
                    component.changeType
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      )}

      {/* Operations Timeline */}
      {comparison?.type !== 'Product' && (
      <div className="comparison-section">
        <div className="comparison-header">
          <h2 className="comparison-title">
            <Wrench size={18} className="text-[var(--accent)]" />
            Operations Timeline
          </h2>
        </div>
        <div className="timeline">
          {comparison?.operations?.map((operation) => (
            <div key={operation.id} className="timeline-item">
              <div className="timeline-item-header">
                <div>
                  <p className="timeline-item-title">{operation.name}</p>
                  <p className="text-xs text-[var(--text-muted)]">{operation.workCenter}</p>
                </div>
                <div className="timeline-duration">
                  <span className="timeline-duration-old">{operation.version1Duration} min</span>
                  <span className="timeline-duration-new">{operation.version2Duration} min</span>
                  {getDeltaBadge(
                    operation.version1Duration,
                    operation.version2Duration,
                    operation.changeType
                  )}
                </div>
              </div>
              <div className="timeline-bar">
                <div
                  className="timeline-bar-fill old"
                  style={{
                    width: `${(operation.version1Duration / maxDuration) * 100}%`,
                    position: 'absolute',
                  }}
                />
                <div
                  className="timeline-bar-fill new"
                  style={{
                    width: `${(operation.version2Duration / maxDuration) * 100}%`,
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
      )}

      {/* Impact Summary */}
      {comparison?.type !== 'Product' && (
      <div className="comparison-section">
        <div className="comparison-header">
          <h2 className="comparison-title">
            <TrendingUp size={18} className="text-[var(--accent)]" />
            Impact Summary
          </h2>
        </div>
        <div className="impact-cards">
          <div className="impact-card">
            <div
              className={clsx(
                'impact-card-value',
                comparison?.summary?.totalPartsDelta > 0
                  ? 'positive'
                  : comparison?.summary?.totalPartsDelta < 0
                    ? 'negative'
                    : 'neutral'
              )}
            >
              {comparison?.summary?.totalPartsDelta > 0 ? '+' : ''}
              {comparison?.summary?.totalPartsDelta || 0}
            </div>
            <p className="impact-card-label">Total Parts Delta</p>
          </div>
          <div className="impact-card">
            <div
              className={clsx(
                'impact-card-value',
                comparison?.summary?.productionCycleDelta > 0
                  ? 'negative'
                  : comparison?.summary?.productionCycleDelta < 0
                    ? 'positive'
                    : 'neutral'
              )}
            >
              {comparison?.summary?.productionCycleDelta > 0 ? '+' : ''}
              {comparison?.summary?.productionCycleDelta || 0} min
            </div>
            <p className="impact-card-label">Production Cycle Delta</p>
          </div>
          <div className="impact-card">
            <div
              className={clsx(
                'impact-card-value flex items-center justify-center gap-1',
                comparison?.summary?.estimatedCostImpact > 0
                  ? 'negative'
                  : comparison?.summary?.estimatedCostImpact < 0
                    ? 'positive'
                    : 'neutral'
              )}
            >
              <DollarSign size={20} />
              {comparison?.summary?.estimatedCostImpact > 0 ? '+' : ''}
              {comparison?.summary?.estimatedCostImpact?.toLocaleString() || 0}
            </div>
            <p className="impact-card-label">Estimated Cost Impact</p>
          </div>
        </div>
      </div>
      )}

      {/* Action Bar */}
      <div className="sticky bottom-0 bg-[var(--bg-base)] border-t border-[var(--bg-border)] -mx-6 px-6 py-4 mt-6 flex items-center justify-between">
        <Link to={`/eco/${id}`} className="btn btn-ghost">
          Back to ECO
        </Link>
        <div className="flex gap-3">
          <button className="btn btn-danger">Reject Changes</button>
          <button className="btn btn-success">Approve Changes</button>
        </div>
      </div>
    </div>
  );
};

export default EcoComparisonPage;
