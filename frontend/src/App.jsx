import { useEffect, useState } from 'react';
import { api } from './api';
import EmployeeSection from './components/EmployeeSection';
import LeaveForm from './components/LeaveForm';
import LeaveTable from './components/LeaveTable';

export default function App() {
  const [employees, setEmployees] = useState([]);
  const [leaves, setLeaves] = useState([]);
  const [message, setMessage] = useState(null); // { type: 'success' | 'error', text }

  // Reload both lists from the backend
  async function loadData() {
    try {
      setEmployees(await api.getEmployees());
      setLeaves(await api.getLeaves());
    } catch (err) {
      setMessage({ type: 'error', text: 'Cannot reach backend. Is Spring Boot running on port 8080?' });
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  // Wraps every action: call API, show success/error message, refresh lists
  async function runAction(action, successText) {
    try {
      await action();
      setMessage({ type: 'success', text: successText });
      await loadData();
      return true;
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
      return false;
    }
  }

  return (
    <div className="container">
      <h1>Leave Management System</h1>

      {message && <div className={`alert ${message.type}`}>{message.text}</div>}

      <EmployeeSection employees={employees} runAction={runAction} />
      <LeaveForm employees={employees} runAction={runAction} />
      <LeaveTable leaves={leaves} runAction={runAction} />
    </div>
  );
}
