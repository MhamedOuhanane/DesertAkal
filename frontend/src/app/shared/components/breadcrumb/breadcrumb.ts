import { Component, computed, inject } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { NavigationService } from '../../../core/services/navigation-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { BreadcrumbItem } from '../../../core/models/breadcrumb.models';

@Component({
    selector: 'app-breadcrumb',
    imports: [MatIcon, RouterLink],
    templateUrl: './breadcrumb.html',
    styles: ``,
})
export class Breadcrumb {
    private readonly router = inject(Router);
    private readonly activatedRoute = inject(ActivatedRoute);
    readonly navService = inject(NavigationService);

    private readonly routeChange = toSignal(
        this.router.events.pipe(
            filter((e) => e instanceof NavigationEnd),
            startWith(null),
            map(() => this.buildBreadcrumbs(this.activatedRoute.root)),
        ),
        { initialValue: [] as BreadcrumbItem[] },
    );

    readonly breadcrumbs = computed(() => this.routeChange());

    private buildBreadcrumbs(
        route: ActivatedRoute,
        url: string = '',
        crumbs: BreadcrumbItem[] = [],
    ): BreadcrumbItem[] {
        const children = route.children;

        for (const child of children) {
            const segments = child.snapshot.url.map((s) => s.path);

            if (segments.length > 0) {
                url += '/' + segments.join('/');
            }

            const data = child.snapshot.data;
            if (data['breadcrumb']) {
                crumbs.push({
                    label: data['breadcrumb'],
                    path: url === '' ? '/dashboard' : url,
                    icon: data['breadcrumbIcon'] ?? undefined,
                });
            }

            return this.buildBreadcrumbs(child, url, crumbs);
        }

        return crumbs;
    }
}
