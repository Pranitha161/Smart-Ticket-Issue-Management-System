import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role-guard';
import { Dashboard } from './dashboard/dashboard';
import { Report } from './report/report';

const routes: Routes = [
  { path: '',  
    component: Dashboard, 
    // canActivate: [roleGuard], 
    // data: { roles: ['ADMIN', 'MANAGER'] }
   }
   ,{
    path:'report',
    component:Report
   }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }
