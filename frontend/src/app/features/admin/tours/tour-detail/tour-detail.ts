import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { TourFind } from '../../../../core/models/tour.model';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { TourService } from '../../../../core/services/tour-service';

@Component({
    selector: 'app-tour-detail',
    imports: [RouterLink, DatePipe, DecimalPipe],
    templateUrl: './tour-detail.html',
})
export class TourDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private tourService = inject(TourService);

    tour = signal<TourFind | null>(null);
    isLoading = signal(true);
    expandedCity = signal<number | null>(null);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/dashboard/tours']);
            return;
        }
        await this.loadTour(uuid);
    }

    private async loadTour(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.tourService.findOne(uuid));
            this.tour.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Tour not found.');
            this.router.navigate(['/dashboard/tours']);
        } finally {
            this.isLoading.set(false);
        }
    }

    toggleCity(index: number): void {
        this.expandedCity.update((current) => (current === index ? null : index));
    }

    getCoverImage(city: any): string {
        const cover = city.images?.find((img: any) => img.isCover);
        return cover?.image || city.images?.[0]?.image || 'assets/defaults/city-placeholder.jpg';
    }
}
