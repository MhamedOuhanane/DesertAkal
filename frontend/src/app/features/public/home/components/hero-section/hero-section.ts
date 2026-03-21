import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { ScreenService } from '../../../../../core/services/screen-service';

@Component({
    selector: 'app-hero-section',
    imports: [RouterLink, MatIcon],
    templateUrl: './hero-section.html',
})
export class HeroSection {
    protected readonly screenService = inject(ScreenService);
}
