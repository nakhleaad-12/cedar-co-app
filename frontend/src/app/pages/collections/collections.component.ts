import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-collections',
  templateUrl: './collections.component.html',
  styles: [`.collections-list { display: flex; flex-direction: column; gap: 2rem; } .collection-row { display: flex; gap: 2rem; align-items: center; padding: 2rem; background: var(--card-bg); border: 1px solid var(--border); border-radius: var(--radius); } .collection-season-badge { font-size: 0.72rem; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; color: var(--terracotta); writing-mode: vertical-lr; padding: 0.5rem; } .collection-info h2 { font-family: var(--font-head); font-size: 1.5rem; margin-bottom: 0.5rem; } .collection-info p { color: var(--muted); margin-bottom: 1rem; } .loading-row { display: flex; justify-content: center; padding: 4rem; }`]
})
export class CollectionsComponent implements OnInit {
  collections: any[] = [];
  loading = true;
  constructor(private http: HttpClient) {}
  ngOnInit(): void {
    this.http.get<any[]>(`${environment.apiUrl}/collections`).subscribe({
      next: c => { this.collections = c; this.loading = false; },
      error: () => this.loading = false
    });
  }
}
