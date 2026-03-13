import { Component } from '@angular/core';
@Component({ 
  selector: 'app-about', 
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.scss']
})
export class AboutComponent {
  values = [
    { title: 'Artisan Craft', desc: 'Every piece is made with traditional Lebanese embroidery and modern techniques.' },
    { title: 'Sustainable Style', desc: 'We prioritize ethical production and locally sourced materials.' },
    { title: 'Lebanese Pride', desc: 'Fashion that celebrates our culture — proudly made in Lebanon.' }
  ];
}
