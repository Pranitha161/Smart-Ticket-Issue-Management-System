import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastMessage {
  text: string;
  type: 'success' | 'error';
}

@Injectable({ providedIn: 'root' })
export class Toast {
  private toastSubject = new Subject<ToastMessage | null>();
  toastState$ = this.toastSubject.asObservable();

  show(text: string, type: 'success' | 'error' = 'success') {
    console.log('Service received message:', text);
    this.toastSubject.next({ text, type });

    // Auto-hide after 3 seconds
    setTimeout(() => this.toastSubject.next(null), 2000);
  }
}