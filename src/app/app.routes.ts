import { Routes } from '@angular/router';

export const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    {
        path: 'signup',
        loadComponent: () => import('../app/modules/auth/signup/signup').then(m => m.Signup),
    },
    {
        path:'login',
        loadComponent:()=>import('../app/modules/auth/login/login').then(m=>m.Login)
    }
];
