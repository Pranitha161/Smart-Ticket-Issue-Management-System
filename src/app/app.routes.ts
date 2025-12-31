import { Routes } from '@angular/router';

export const routes: Routes = [
    { path: '', redirectTo: 'tickets', pathMatch: 'full' },
    {
        path: 'signup',
        loadComponent: () => import('../app/modules/auth/signup/signup').then(m => m.Signup),
    },
    {
        path:'login',
        loadComponent:()=>import('../app/modules/auth/login/login').then(m=>m.Login)
    },
    { path: 'tickets', loadChildren: () => import('./modules/tickets/tickets-module').then(m => m.TicketsModule) }
];
