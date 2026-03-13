import { Component } from '@angular/core';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  templateUrl: './toast-container.component.html'
})
export class ToastContainerComponent {
  toasts$ = this.toast.toasts$;
  constructor(private toast: ToastService) {}
  remove(id: number): void { this.toast.remove(id); }
}
