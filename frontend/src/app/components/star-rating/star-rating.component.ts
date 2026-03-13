import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  templateUrl: './star-rating.component.html',
  styleUrls: ['./star-rating.component.scss']
})
export class StarRatingComponent {
  @Input() rating = 0;
  stars = [1,2,3,4,5];
  get fullStars(): number { return Math.floor(this.rating); }
  get hasHalf(): boolean { return (this.rating % 1) >= 0.5; }
}
