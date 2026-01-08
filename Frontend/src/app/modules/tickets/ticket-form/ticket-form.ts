import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TicketService } from '../../../core/services/ticket';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth';
import { RouterLink } from '@angular/router';
import { CategoryDto } from '../../../core/services/category';
import { LookupService } from '../../../core/services/lookup-service';
import { Toast } from '../../../core/services/toast';

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ticket-form.html',
  styleUrl: './ticket-form.css',
})
export class TicketForm implements OnInit {
  ticketForm!: FormGroup;
  successMessage = ''; errorMessage = '';
  categories: CategoryDto[] = [];
  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private authService: AuthService,
    private toast: Toast,
    private lookup: LookupService,
    private cd: ChangeDetectorRef) { }
  ngOnInit(): void {
    this.ticketForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      categoryId: ['', Validators.required],
      priority: [{ value: 'MEDIUM', disabled: true }, Validators.required],
      createdBy: [this.authService.userId()]
    });
    this.categories = this.lookup.getCategoryList();
    console.log(this.categories);
    this.ticketForm.get('categoryId')?.valueChanges.subscribe(catId => {
      const priority = this.lookup.getPriorityForCategory(catId);
      const priorityControl = this.ticketForm.get('priority');
      if (priorityControl) {
        priorityControl.enable({ emitEvent: false });
        priorityControl.setValue(priority, { emitEvent: false });
        priorityControl.disable({ emitEvent: false });
      }
    });

  }
  getPriorityClass() {
    const val = this.ticketForm.get('priority')?.value;
    return val ? `priority-${val}` : '';
  }
  onSubmit(): void {
    
    if (this.ticketForm.valid) {
      const payload = { ...this.ticketForm.value, priority: this.ticketForm.get('priority')?.value || 'MEDIUM', status: 'OPEN' };
      this.ticketService.createTicket(payload).subscribe({
        next: (res) => {
          console.log(res);
          this.toast.show(res.message || 'Ticket created successfully!', 'success');
          this.ticketForm.reset({ priority: 'MEDIUM', status: 'OPEN' });
          this.cd.detectChanges();
        },
        error: (err) => {
          const errorMsg = err.error?.message || 'Failed to create ticket. Please try again.';
          this.toast.show(errorMsg, 'error');
          this.cd.detectChanges();
        }
      });
    }
  }
}
