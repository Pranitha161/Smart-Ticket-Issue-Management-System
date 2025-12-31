import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SlaRule } from '../../../core/services/sla-rule';
import { SlaRuleModel } from '../../../shared/models/sla-rule.model';

@Component({
  selector: 'app-sla-rules-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './sla-rules-list.html',
  styleUrls: ['./sla-rules-list.css']
})
export class SlaRulesList implements OnInit {
  slaRules: SlaRuleModel[] = [];
  form!: FormGroup;
  editingRule: SlaRuleModel | null = null;

  constructor(
    private slaRuleService: SlaRule,
    private cd: ChangeDetectorRef,
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    this.loadRules();
    this.form = this.fb.group({
      priority: [{ value: '', disabled: true }, Validators.required],
      responseMinutes: ['', [Validators.required, Validators.min(1)]],
      resolutionMinutes: ['', [Validators.required, Validators.min(1)]],
    });
  }

  loadRules(): void {
    this.slaRuleService.getAllRules().subscribe({
      next: (rules) => {
        this.slaRules = rules;
        this.cd.detectChanges();
      },
      error: (err) => console.error('Error loading SLA rules', err)
    });
  }
  editRule(rule: SlaRuleModel): void 
  { 
    this.editingRule = rule; 
    this.form.patchValue(rule); 
    this.form.get('priority')?.disable(); 
  }

  cancelEdit(): void { this.editingRule = null; this.form.reset(); this.form.get('priority')?.enable();}

  saveRule(): void {
    if (!this.editingRule || this.form.invalid) return;

    const updated: SlaRuleModel = {
      ...this.editingRule,
      ...this.form.getRawValue()
    };

    this.slaRuleService.updateRule(this.editingRule.id!, updated).subscribe(() => {
      this.loadRules();
      this.cancelEdit();
    });
  }

  deleteRule(id: string, priority: string): void {
    if (['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(priority)) {
      alert('Cannot delete baseline SLA rule');
      return;
    }
    this.slaRuleService.deleteRule(id).subscribe(() => this.loadRules());
  }
}
