import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SlaRule } from '../../../core/services/sla-rule';
import { SlaRuleModel } from '../../../shared/models/sla-rule.model';
import { Toast } from '../../../core/services/toast';
import { LookupService } from '../../../core/services/lookup-service';

@Component({
  selector: 'app-sla-rules-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './sla-rules-list.html',
  styleUrls: ['./sla-rules-list.css']
})
export class SlaRulesList implements OnInit {
  private slaRuleService = inject(SlaRule);
  private toast = inject(Toast);
  private lookup = inject(LookupService);
  private cd = inject(ChangeDetectorRef);
  private fb = inject(FormBuilder);

  slaRules: SlaRuleModel[] = [];
  form!: FormGroup;
  editingRule: SlaRuleModel | null = null;
  ruleToDelete: SlaRuleModel | null = null; // Staging for custom confirmation

  ngOnInit(): void {
    this.loadRules();
    this.initForm();
  }

  initForm(): void {
    this.form = this.fb.group({
      priority: ['', Validators.required],
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
      error: (err) => this.toast.show(err.message, 'error')
    });
  }

  editRule(rule: SlaRuleModel): void {
    this.editingRule = rule;
    this.form.patchValue(rule);
    // Disable priority field if it's a baseline system rule
    if (['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(rule.priority.toUpperCase())) {
      this.form.get('priority')?.disable();
    } else {
      this.form.get('priority')?.enable();
    }
  }

  saveRule(): void {
    if (this.form.invalid) return;

    const updated: SlaRuleModel = { ...this.editingRule, ...this.form.getRawValue() };
    const request = updated.id 
      ? this.slaRuleService.updateRule(updated.id, updated)
      : this.slaRuleService.addRule(updated);

    request.subscribe({
      next: () => {
        this.toast.show(`SLA Rule for ${updated.priority} updated successfully`, 'success');
        this.lookup.refreshSlas(); // Sync categories dropdown
        this.loadRules();
        this.cancelEdit();
      },
      error: (err) => this.toast.show(err.error?.message || err.message, 'error')
    });
  }

  // staged deletion logic
  confirmDelete(rule: SlaRuleModel): void {
    if (['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(rule.priority.toUpperCase())) {
      this.toast.show('System baseline rules cannot be deleted', 'error');
      return;
    }
    this.ruleToDelete = rule;
  }

  executeDelete(): void {
    if (this.ruleToDelete?.id) {
      this.slaRuleService.deleteRule(this.ruleToDelete.id).subscribe({
        next: () => {
          this.toast.show('SLA Rule removed', 'success');
          this.lookup.refreshSlas();
          this.ruleToDelete = null;
          this.loadRules();
        },
        error: (err) => this.toast.show(err.message, 'error')
      });
    }
  }

  cancelEdit(): void {
    this.editingRule = null;
    this.form.reset();
  }
}