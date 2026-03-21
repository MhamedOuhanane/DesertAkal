import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { Guide } from '../../../../../core/models/guide.model';

@Component({
    selector: 'app-top-guides',
    imports: [RouterLink, DecimalPipe, MatIcon],
    templateUrl: './top-guides-section.html',
})
export class TopGuidesSection {
    guides = input<Guide[]>([]);
    loading = input(true);

    getLanguageNames(guide: Guide): string {
        if (!guide.languages?.length) return '';
        return guide.languages
            .map((l) => l.name)
            .slice(0, 3)
            .join(', ');
    }
}
