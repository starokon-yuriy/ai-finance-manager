import React from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine
} from 'recharts';

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="chart-tooltip">
        <p className="tooltip-date">{label}</p>
        {payload.map((entry, index) => (
          <p key={index} style={{ color: entry.color }} className="tooltip-value">
            {entry.name}: ${Number(entry.value).toFixed(2)}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

function BalanceChart({ data, loading, error }) {
  if (loading) {
    return (
      <div className="chart-container">
        <div className="chart-loading">Loading chart data...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="chart-container">
        <div className="chart-error">
          <span className="error-icon">⚠️</span>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!data || !data.balanceData || data.balanceData.length === 0) {
    return (
      <div className="chart-container">
        <div className="chart-empty">
          <span className="empty-icon">📊</span>
          <p>No data available for chart. Add some transactions first!</p>
        </div>
      </div>
    );
  }

  const chartData = data.balanceData.map(point => ({
    date: point.date,
    Income: Number(point.income),
    Expense: Number(point.expense),
    Balance: Number(point.balance)
  }));

  return (
    <div className="chart-container">
      <div className="chart-header">
        <h3>📈 Income vs Expenses Over Time</h3>
        <div className="chart-summary">
          <div className="summary-item income-summary">
            <span className="summary-label">Total Income</span>
            <span className="summary-value">${Number(data.totalIncome).toFixed(2)}</span>
          </div>
          <div className="summary-item expense-summary">
            <span className="summary-label">Total Expense</span>
            <span className="summary-value">${Number(data.totalExpense).toFixed(2)}</span>
          </div>
          <div className={`summary-item balance-summary ${Number(data.netBalance) >= 0 ? 'positive' : 'negative'}`}>
            <span className="summary-label">Net Balance</span>
            <span className="summary-value">${Number(data.netBalance).toFixed(2)}</span>
          </div>
        </div>
      </div>

      <div className="chart-wrapper">
        <ResponsiveContainer width="100%" height={400}>
          <AreaChart data={chartData} margin={{ top: 10, right: 30, left: 10, bottom: 5 }}>
            <defs>
              <linearGradient id="colorIncome" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="colorExpense" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#667eea" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#667eea" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 12, fill: '#6b7280' }}
              tickLine={{ stroke: '#d1d5db' }}
            />
            <YAxis
              tick={{ fontSize: 12, fill: '#6b7280' }}
              tickLine={{ stroke: '#d1d5db' }}
              tickFormatter={(value) => `$${value}`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{ paddingTop: '20px' }}
              iconType="circle"
            />
            <ReferenceLine y={0} stroke="#9ca3af" strokeDasharray="3 3" />
            <Area
              type="monotone"
              dataKey="Income"
              stroke="#10b981"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#colorIncome)"
              dot={{ r: 3, fill: '#10b981' }}
              activeDot={{ r: 6, stroke: '#10b981', strokeWidth: 2, fill: 'white' }}
            />
            <Area
              type="monotone"
              dataKey="Expense"
              stroke="#ef4444"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#colorExpense)"
              dot={{ r: 3, fill: '#ef4444' }}
              activeDot={{ r: 6, stroke: '#ef4444', strokeWidth: 2, fill: 'white' }}
            />
            <Area
              type="monotone"
              dataKey="Balance"
              stroke="#667eea"
              strokeWidth={2.5}
              fillOpacity={1}
              fill="url(#colorBalance)"
              dot={{ r: 3, fill: '#667eea' }}
              activeDot={{ r: 6, stroke: '#667eea', strokeWidth: 2, fill: 'white' }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {data.chartEmbedUrl && (
        <div className="datawrapper-embed">
          <h4>📊 Datawrapper Interactive Chart</h4>
          <iframe
            key={data.chartEmbedUrl}
            title="Datawrapper Balance Chart"
            src={data.chartEmbedUrl}
            width="100%"
            height="400"
            style={{ border: 'none', borderRadius: '8px', overflow: 'hidden' }}
            allowFullScreen
          />
        </div>
      )}
    </div>
  );
}

export default BalanceChart;

