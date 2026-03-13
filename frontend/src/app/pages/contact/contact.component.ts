import { Component } from '@angular/core';
@Component({ selector: 'app-contact', templateUrl: './contact.component.html' })
export class ContactComponent {
  name = ''; email = ''; message = ''; sent = false;
  sendMessage(e: Event): void { e.preventDefault(); this.sent = true; this.name = this.email = this.message = ''; }
}
