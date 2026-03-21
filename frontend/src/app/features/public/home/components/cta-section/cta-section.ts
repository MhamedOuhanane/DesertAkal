import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { IsAuthenticated } from '../../../../../shared/directives';

@Component({
    selector: 'app-cta-section',
    imports: [RouterLink, MatIcon, IsAuthenticated],
    templateUrl: './cta-section.html',
})
export class CtaSection {}
