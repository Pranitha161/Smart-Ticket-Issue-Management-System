import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [ 
  { path: '', loadComponent:()=>import('./ticket-list/ticket-list').then(m=>m.TicketList) }, 
   { path: 'create', loadComponent: () => import('./ticket-form/ticket-form').then(m => m.TicketForm) },
   { path: ':id', loadComponent:()=>import('./ticket-details/ticket-details').then(m=>m.TicketDetails) } 
  ];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TicketsRoutingModule { }
