import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryPanel } from '../category-panel/category-panel';
import { SlaRulesList } from '../../sla-rules/sla-rules-list/sla-rules-list';
import { UserManagement } from '../user-management/user-management';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, CategoryPanel, SlaRulesList, UserManagement],
  templateUrl: './admin-panel.html',
  styleUrls: ['./admin-panel.css']
})
export class AdminPanel {
  activeTab: string = 'users';
  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

}