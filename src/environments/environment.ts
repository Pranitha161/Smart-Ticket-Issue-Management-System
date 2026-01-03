export const environment = {
  production: false,
  apiGatewayUrl: 'http://localhost:8765',
  endpoints: {
    auth: '/auth-user-service',
    tickets: '/ticket-service',
    assignments: '/assignment-escalation-service',
    dashboards: '/dashboard-service',
    notifications: '/notification-service'
  }
};
