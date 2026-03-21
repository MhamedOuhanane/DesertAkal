import { inject, Injectable } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ScreenService {
    private readonly breakpointObserver = inject(BreakpointObserver);

    readonly isMobile = toSignal(
        this.breakpointObserver.observe('(max-width: 767px)').pipe(map((res) => res.matches)),
        { initialValue: false },
    );

    readonly isTablet = toSignal(
        this.breakpointObserver
            .observe('(min-width: 768px) and (max-width: 1023px)')
            .pipe(map((res) => res.matches)),
        { initialValue: false },
    );

    readonly isWeb = toSignal(
        this.breakpointObserver.observe('(min-width: 1024px)').pipe(map((res) => res.matches)),
        { initialValue: true },
    );

    readonly highlights = [
        { icon: 'landscape', value: '12+', label: 'Regions Covered' },
        { icon: 'groups', value: '50+', label: 'Local Guides' },
        { icon: 'tour', value: '100+', label: 'Unique Tours' },
        { icon: 'star', value: '4.8', label: 'Average Rating' },
    ];
}
