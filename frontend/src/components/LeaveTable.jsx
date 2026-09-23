import { api } from '../api';

export default function LeaveTable({ leaves, runAction }) {
  return (
    <section className="card">
      <h2>Leave Requests</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th><th>Employee</th><th>From</th><th>To</th><th>Days</th>
            <th>Reason</th><th>Status</th><th>Action</th>
          </tr>
        </thead>
        <tbody>
          {leaves.map((leave) => (
            <tr key={leave.id}>
              <td>{leave.id}</td>
              <td>{leave.employeeName}</td>
              <td>{leave.startDate}</td>
              <td>{leave.endDate}</td>
              <td>{leave.days}</td>
              <td>{leave.reason}</td>
              <td><span className={`badge ${leave.status.toLowerCase()}`}>{leave.status}</span></td>
              <td>
                {leave.status === 'PENDING' && (
                  <>
                    <button onClick={() => runAction(() => api.approveLeave(leave.id), 'Leave approved')}>
                      Approve
                    </button>
                    <button className="secondary"
                      onClick={() => runAction(() => api.rejectLeave(leave.id), 'Leave rejected')}>
                      Reject
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
