import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
   {
        path: 'signup',
        loadComponent: () => import('./signup/signup').then(m => m.Signup),
    },
    {
        path:'login',
        loadComponent:()=>import('./login/login').then(m=>m.Login)
    },
     {
        path:'admin-panel',
        loadComponent:()=>import('./admin-panel/admin-panel').then(m=>m.AdminPanel)
    },
    {
      path:'category-panel',
      loadComponent:()=>import('./category-panel/category-panel').then(m=>m.CategoryPanel)
    }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule {
  
 }
