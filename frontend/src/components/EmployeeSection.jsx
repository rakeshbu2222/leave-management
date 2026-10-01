import { useState } from 'react';
import { api } from '../api';

const emptyForm = { name: '', email: '', department: '', phoneNumber: '' };

export default function EmployeeSection({ employees, runAction }) {
  const [form, setForm] = useState(emptyForm);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = { ...form, phoneNumber: form.phoneNumber.trim() || null };
    const ok = await runAction(() => api.createEmployee(form), 'Employee added');
    if (ok) setForm(emptyForm);
  }

  return (
    <section className="card">
      <h2>Employees</h2>

      <form onSubmit={handleSubmit} className="row">
        <input name="name" placeholder="Name" value={form.name} onChange={handleChange} />
        <input name="email" placeholder="Email" value={form.email} onChange={handleChange} />
        <input name="department" placeholder="Department" value={form.department} onChange={handleChange} />
        <input name="phoneNumber" placeholder="Phone (10 digits, optional)" value={form.phoneNumber} onChange={handleChange} />
        <button type="submit">Add Employee</button>
      </form>

      <table>
        <thead>
          <tr><th>ID</th><th>Name</th><th>Email</th><th>Department</th><th>Phone</th><th>Leave Balance</th></tr>
        </thead>
        <tbody>
          {employees.map((emp) => (
            <tr key={emp.id}>
              <td>{emp.id}</td>
              <td>{emp.name}</td>
              <td>{emp.email}</td>
              <td>{emp.department}</td>
              <td>{emp.phoneNumber ?? '-'}</td>
              <td>{emp.leaveBalance}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
