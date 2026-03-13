import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { CityFind } from '../../../../core/models/city.model';
import { Image } from '../../../../core/models/image.model';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { CityService } from '../../../../core/services/city-service';

@Component({
    selector: 'app-city-detail',
    imports: [RouterLink, DatePipe, DecimalPipe, MatIcon, DeleteDialog],
    templateUrl: './city-detail.html',
})
export class CityDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private cityService = inject(CityService);

    city = signal<CityFind | null>(null);
    isLoading = signal(true);
    selectedImage = signal<Image | null>(null);
    showLightbox = signal(false);

    showDeleteDialog = signal(false);
    isDeleting = signal(false);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/dashboard/cities']);
            return;
        }
        await this.loadCity(uuid);
    }

    private async loadCity(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.cityService.find(uuid));
            this.city.set(res.data!);
        } catch (err: any) {
            toast.error(err?.error?.message || 'City not found');
            this.router.navigate(['/dashboard/cities']);
        } finally {
            this.isLoading.set(false);
        }
    }

    getCoverImage(): string {
        const city = this.city();
        if (!city) return 'assets/defaults/city-placeholder.jpg';
        const cover = city.images?.find((img) => img.isCover);
        return cover?.image || city.images?.[0]?.image || 'assets/defaults/city-placeholder.jpg';
    }

    openLightbox(image: Image): void {
        this.selectedImage.set(image);
        this.showLightbox.set(true);
    }

    closeLightbox(): void {
        this.showLightbox.set(false);
        this.selectedImage.set(null);
    }

    navigateImage(direction: number): void {
        const city = this.city();
        const current = this.selectedImage();
        if (!city || !current) return;

        const images = city.images;
        const currentIndex = images.findIndex((img) => img.uuid === current.uuid);
        const newIndex = (currentIndex + direction + images.length) % images.length;
        this.selectedImage.set(images[newIndex]);
    }

    openDeleteDialog(): void {
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        if (!this.city()) return;
        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.cityService.delete(this.city()!.uuid));
            toast.success('City deleted successfully');
            this.router.navigate(['/dashboard/cities']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete city');
        } finally {
            this.isDeleting.set(false);
            this.showDeleteDialog.set(false);
        }
    }
}
