import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-collections',
  templateUrl: './collections.component.html',
  styleUrls: ['./collections.component.scss']
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
