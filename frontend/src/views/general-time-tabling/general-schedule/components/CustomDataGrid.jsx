import React, { useState, useMemo } from 'react';

const CustomDataGrid = () => {

  const data = [
    {id: 1, name: 'Alice', role: 'Admin'},
    {id: 2, name: 'Bob', role: 'User'},
    {id: 3, name: 'Charlie', role: 'Moderator'},
    {id: 4, name: 'David', role: 'User'},
    {id: 5, name: 'Eve', role: 'Admin'},    
  ];  
  // Use a Set for O(1) lookup and easy toggling
  const [selectedIds, setSelectedIds] = useState(new Set());

  // Derived state for the "Select All" logic
  const isAllSelected = data.length > 0 && selectedIds.size === data.length;
  const isAnySelected = selectedIds.size > 0;
  const isIndeterminate = isAnySelected && !isAllSelected;

  // Toggle a single row
  const toggleRow = (id) => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedIds(newSelected);
  };

  // Toggle all rows
  const toggleAll = () => {
    if (isAllSelected) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(data.map((item) => item.id)));
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #ddd', backgroundColor: '#f5f5f5' }}>
            <th style={{ padding: '12px' }}>
              <input
                type="checkbox"
                checked={isAllSelected}
                ref={(el) => el && (el.indeterminate = isIndeterminate)}
                onChange={toggleAll}
              />
            </th>
            <th style={{ padding: '12px' }}>ID</th>
            <th style={{ padding: '12px' }}>Name</th>
            <th style={{ padding: '12px' }}>Role</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr 
              key={row.id} 
              style={{ 
                borderBottom: '1px solid #eee',
                backgroundColor: selectedIds.has(row.id) ? '#f0f7ff' : 'transparent' 
              }}
            >
              <td style={{ padding: '12px' }}>
                <input
                  type="checkbox"
                  checked={selectedIds.has(row.id)}
                  onChange={() => toggleRow(row.id)}
                />
              </td>
              <td style={{ padding: '12px' }}>{row.id}</td>
              <td style={{ padding: '12px' }}>{row.name}</td>
              <td style={{ padding: '12px' }}>{row.role}</td>
            </tr>
          ))}
        </tbody>
      </table>
      
      <div style={{ marginTop: '10px', color: '#666' }}>
        {selectedIds.size} row(s) selected
      </div>
    </div>
  );
};

export default CustomDataGrid;