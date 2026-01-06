import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotificationPanel } from './notification-panel/notification-panel';

const routes: Routes = [
 {
     path:'',
     component: NotificationPanel
    }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule { }
