import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';
import * as L from 'leaflet';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit, AfterViewInit, OnDestroy {
  window = window;
  order: any = null;
  loading = true;
  error = '';
  
  private map: L.Map | null = null;
  private warehouseCoords: L.LatLngExpression = [33.8938, 35.5018]; // Beirut
  private destinationCoords: L.LatLngExpression | null = null;
  
  private truckIcon = L.icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/754/754848.png',
    iconSize: [40, 40],
    iconAnchor: [20, 20]
  });

  private homeIcon = L.icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/609/609803.png',
    iconSize: [30, 30],
    iconAnchor: [15, 30]
  });

  private warehouseIcon = L.icon({
    iconUrl: 'https://cdn-icons-png.flaticon.com/512/2312/2312675.png',
    iconSize: [35, 35],
    iconAnchor: [17, 35]
  });

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchOrder(id);
    } else {
      this.error = 'Invalid order ID.';
      this.loading = false;
    }
  }

  ngAfterViewInit(): void {
    // Map will be initialized after order data is loaded and geocoded
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  fetchOrder(id: string): void {
    this.http.get<any>(`${environment.apiUrl}/orders/${id}`).subscribe({
      next: (data) => {
        this.order = data;
        this.loading = false;
        this.geocodeAndInitMap();
      },
      error: (err) => {
        console.error('Failed to load order detailed:', err);
        this.error = 'Unable to load order details. Please ensure the order exists.';
        this.loading = false;
      }
    });
  }

  private geocodeAndInitMap() {
    if (!this.order) return;

    const address = `${this.order.shippingStreet}, ${this.order.shippingCity}, ${this.order.shippingCountry}`;
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=1`;

    this.http.get<any[]>(url).subscribe({
      next: (results) => {
        if (results && results.length > 0) {
          this.destinationCoords = [parseFloat(results[0].lat), parseFloat(results[0].lon)];
        } else {
          // Fallback to city center if specific address not found
          this.geocodeCityFallback();
          return;
        }
        this.initMap();
      },
      error: () => {
        this.geocodeCityFallback();
      }
    });
  }

  private geocodeCityFallback() {
    const cityUrl = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.order.shippingCity + ', Lebanon')}&limit=1`;
    this.http.get<any[]>(cityUrl).subscribe({
      next: (results) => {
        if (results && results.length > 0) {
          this.destinationCoords = [parseFloat(results[0].lat), parseFloat(results[0].lon)];
        }
        this.initMap();
      },
      error: () => this.initMap() // Init map anyway (might only show warehouse)
    });
  }

  private initMap() {
    setTimeout(() => {
      const mapEl = document.getElementById('order-map');
      if (!mapEl || this.map) return;

      this.map = L.map('order-map', {
        zoomControl: false,
        attributionControl: false
      }).setView(this.warehouseCoords, 10);

      L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19
      }).addTo(this.map);

      L.marker(this.warehouseCoords, { icon: this.warehouseIcon }).addTo(this.map)
        .bindPopup('Cedar & Co. Warehouse');

      if (this.destinationCoords) {
        L.marker(this.destinationCoords, { icon: this.homeIcon }).addTo(this.map)
          .bindPopup('Delivery Destination');

        // Draw Route
        const line = L.polyline([this.warehouseCoords, this.destinationCoords], {
          color: '#12372A',
          weight: 3,
          dashArray: '10, 10',
          opacity: 0.5
        }).addTo(this.map);

        this.map.fitBounds(line.getBounds(), { padding: [50, 50] });

        // Add Truck based on status
        this.addTruckIcon();
      }
    }, 100);
  }

  private addTruckIcon() {
    if (!this.map || !this.destinationCoords) return;

    let progress = 0;
    const status = this.order.status;

    if (['CONFIRMED', 'PROCESSING'].includes(status)) progress = 0.1;
    else if (status === 'SHIPPED') progress = 0.5;
    else if (status === 'DELIVERED') progress = 1.0;

    if (progress > 0) {
      const lat = (this.warehouseCoords as number[])[0] + ((this.destinationCoords as number[])[0] - (this.warehouseCoords as number[])[0]) * progress;
      const lng = (this.warehouseCoords as number[])[1] + ((this.destinationCoords as number[])[1] - (this.warehouseCoords as number[])[1]) * progress;
      
      L.marker([lat, lng], { icon: this.truckIcon }).addTo(this.map)
        .bindPopup(progress === 1 ? 'Arrived!' : 'En route...');
    }
  }
}
