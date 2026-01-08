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
  ruleToDelete: SlaRuleModel | null = null;
  isNewRule = false;

  ngOnInit(): void {
    this.initForm();
    this.loadRules();
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

  openCreateModal() {
    this.isNewRule = true;

    this.editingRule = {
      priority: '' as any,
      responseMinutes: 0,
      resolutionMinutes: 0
    } as SlaRuleModel;

    this.form.reset();
    this.form.get('priority')?.enable();
  }

  editRule(rule: SlaRuleModel): void {
    this.isNewRule = false;
    this.editingRule = rule;
    this.form.patchValue(rule);

    if (['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(rule.priority.toUpperCase())) {
      this.form.get('priority')?.disable();
    } else {
      this.form.get('priority')?.enable();
    }
  }


  saveRule(): void {
    if (this.form.invalid) return;

    const formData = this.form.getRawValue() as {
      priority: string;
      responseMinutes: number;
      resolutionMinutes: number;
    };
    const payload: SlaRuleModel = { ...this.editingRule, ...formData, priority: formData.priority.trim().toUpperCase() as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
 };


    const request = (!this.isNewRule && payload.id)
      ? this.slaRuleService.updateRule(payload.id, payload)
      : this.slaRuleService.addRule(payload);

    request.subscribe({
      next: () => {
        const msg = this.isNewRule ? 'created' : 'updated';
        this.toast.show(`SLA Rule for ${payload.priority} ${msg} successfully`, 'success');
        this.lookup.refreshSlas();
        this.loadRules();
        this.cancelEdit();
      },
      error: (err) => this.toast.show(err.error?.message || err.message, 'error')
    });
  }

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
    this.isNewRule = false;
    this.form.reset();
  }
}