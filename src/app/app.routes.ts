import { Routes } from '@angular/router';

export const routes: Routes = [
    { path: '', redirectTo: 'tickets', pathMatch: 'full' },
    {
        path: 'auth',
        loadChildren: () => import('./modules/auth/auth-module').then(m => m.AuthModule),
    },
    { path: 'tickets', loadChildren: () => import('./modules/tickets/tickets-module').then(m => m.TicketsModule) }
];
