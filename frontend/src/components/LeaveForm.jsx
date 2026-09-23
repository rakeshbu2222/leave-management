import { useState } from 'react';
import { api } from '../api';

const emptyForm = { employeeId: '', startDate: '', endDate: '', reason: '' };

export default function LeaveForm({ employees, runAction }) {
  const [form, setForm] = useState(emptyForm);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = { ...form, employeeId: form.employeeId ? Number(form.employeeId) : null };
    const ok = await runAction(() => api.applyLeave(payload), 'Leave applied');
    if (ok) setForm(emptyForm);
  }

  return (
    <section className="card">
      <h2>Apply Leave</h2>
      <form onSubmit={handleSubmit} className="row">
        <select name="employeeId" value={form.employeeId} onChange={handleChange}>
          <option value="">Select employee</option>
          {employees.map((emp) => (
            <option key={emp.id} value={emp.id}>{emp.name}</option>
          ))}
        </select>
        <input type="date" name="startDate" value={form.startDate} onChange={handleChange} />
        <input type="date" name="endDate" value={form.endDate} onChange={handleChange} />
        <input name="reason" placeholder="Reason" value={form.reason} onChange={handleChange} />
        <button type="submit">Apply</button>
      </form>
    </section>
  );
}
