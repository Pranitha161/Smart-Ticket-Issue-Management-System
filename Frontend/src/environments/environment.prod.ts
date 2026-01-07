export const environment = {
  production: true,
  apiGatewayUrl: 'https://api.smartticket.com/api',
  endpoints: {
    auth: '/auth-user-service',
    tickets: '/ticket-service',
    assignments: '/assignment-escalation-service',
    dashboards: '/dashboard-service',
    notifications: '/notification-service'
  }
};
