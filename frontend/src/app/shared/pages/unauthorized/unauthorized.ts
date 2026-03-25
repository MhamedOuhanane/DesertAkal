import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-unauthorized',
    standalone: true,
    imports: [RouterLink, MatIconModule],
    templateUrl: './unauthorized.html',
    styleUrl: './unauthorized.scss',
})
export class Unauthorized {}
