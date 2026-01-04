import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TicketService } from '../../../core/services/ticket';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth';
import { RouterLink } from '@angular/router';
import { Category, CategoryDto } from '../../../core/services/category';
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
    private toast:Toast,
    private lookup: LookupService, 
    private cd: ChangeDetectorRef) { }
  ngOnInit(): void {
    this.ticketForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      categoryId: ['', Validators.required],
      priority: ['MEDIUM', Validators.required],
      createdBy: [this.authService.userId()]
    });
    this.categories = this.lookup.getCategoryList();
    
  }
  getPriorityClass() {
  const val = this.ticketForm.get('priority')?.value;
  return val ? `priority-${val}` : '';
}
  onSubmit(): void {
    
    if (this.ticketForm.valid) {
      this.ticketService.createTicket(this.ticketForm.value).subscribe({
        next: (res) => {
          this.toast.show(res.message||'Ticket created successfully!', 'success');
          this.ticketForm.reset({ priority: 'MEDIUM', status: 'OPEN' });
          
        },
        error: (err) => {
          const errorMsg = err.error?.message ||'Failed to create ticket. Please try again.'; 
          this.toast.show(errorMsg, 'error');
         
        }
      });
    }
  }
}
