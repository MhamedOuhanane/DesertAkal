import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-main-layout',
    imports: [RouterOutlet],
    host: {
        class: 'block',
    },
    templateUrl: './main-layout.html',
    styleUrl: './main-layout.scss',
})
export class MainLayout {}
