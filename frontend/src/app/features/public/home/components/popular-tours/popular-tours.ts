import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { Tour } from '../../../../../core/models/tour.model';
import { CityTour } from '../../../../../core/models/city-tour.model';

@Component({
    selector: 'app-popular-tours',
    imports: [RouterLink, DecimalPipe, MatIcon],
    templateUrl: './popular-tours.html',
})
export class PopularTours {
    tours = input<Tour[]>([]);
    loading = input(true);

    getCityNames(tour: Tour): string {
        if (!tour.cityTours?.length) return '';
        return tour.cityTours
            .map((ct: CityTour) => ct.cityName || '')
            .filter(Boolean)
            .slice(0, 3)
            .join(' · ');
    }
}
