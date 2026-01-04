import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TicketsRoutingModule } from './tickets-routing-module';
import { TicketForm } from './ticket-form/ticket-form';
import { ReactiveFormsModule } from '@angular/forms';


@NgModule({
  declarations: [
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule, 
    TicketsRoutingModule
  ]
})
export class TicketsModule { }
