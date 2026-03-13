import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent {
  email = '';
  subscribed = false;

  subscribe(e: Event): void {
    e.preventDefault();
    if (this.email) { this.subscribed = true; this.email = ''; }
  }
}
