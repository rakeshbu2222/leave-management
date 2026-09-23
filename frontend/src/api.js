// All backend calls live in ONE file. Components never call fetch() directly.
const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(BASE_URL + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    // Our backend always returns { message, details } on error (see GlobalExceptionHandler)
    const details = data?.details?.length ? ' - ' + data.details.join(', ') : '';
    throw new Error((data?.message || 'Request failed') + details);
  }
  return data;
}

export const api = {
  getEmployees: () => request('/employees'),
  createEmployee: (employee) =>
    request('/employees', { method: 'POST', body: JSON.stringify(employee) }),

  getLeaves: (employeeId) => request('/leaves' + (employeeId ? `?employeeId=${employeeId}` : '')),
  applyLeave: (leave) => request('/leaves', { method: 'POST', body: JSON.stringify(leave) }),
  approveLeave: (id) => request(`/leaves/${id}/approve`, { method: 'PUT' }),
  rejectLeave: (id) => request(`/leaves/${id}/reject`, { method: 'PUT' }),
};
