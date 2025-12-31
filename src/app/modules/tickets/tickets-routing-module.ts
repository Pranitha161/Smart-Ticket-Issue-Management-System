import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TicketList } from './ticket-list/ticket-list';

const routes: Routes = [ 
  { path: '', component: TicketList }, 
   { path: 'create', loadComponent: () => import('./ticket-form/ticket-form').then(m => m.TicketForm) }
  //  { path: ':id', component: TicketDetailComponent } 
  ];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TicketsRoutingModule { }
