import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path:'',
    loadComponent:()=>import('./sla-rules-list/sla-rules-list').then(m=>m.SlaRulesList)
  },
  {
  path: 'sla-rules/new',
  loadComponent: () =>
    import('./sla-rules-list/sla-rules-list')
      .then(m => m.SlaRulesList)
},
{
  path: 'sla-rules/edit/:id',
  loadComponent: () =>
    import('./sla-rules-list/sla-rules-list')
      .then(m => m.SlaRulesList)
}

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SlaRulesRoutingModule { }
