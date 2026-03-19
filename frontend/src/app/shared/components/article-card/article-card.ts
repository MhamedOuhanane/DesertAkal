import { DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { Article } from '../../../core/models/article.models';

@Component({
    selector: 'app-article-card',
    standalone: true,
    imports: [DatePipe, MatIcon],
    template: `
        <div
            class="card group cursor-pointer overflow-hidden transition-all hover:border-primary/30 hover:shadow-md"
            (click)="viewClick.emit(article().uuid)"
        >
            @if (article().coverImage) {
                <div class="relative h-40 overflow-hidden bg-main-bg">
                    <img
                        [src]="article().coverImage"
                        alt="Cover"
                        class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                        (error)="$any($event.target).style.display = 'none'"
                    />
                    <div class="absolute bottom-2 left-2 flex items-center gap-2">
                        <span
                            class="flex items-center gap-1 rounded-lg bg-black/60 px-2 py-1 text-[11px] font-medium text-white backdrop-blur-sm"
                        >
                            <mat-icon style="font-size: 12px; width: 12px; height: 12px"
                                >chat_bubble_outline</mat-icon
                            >
                            {{ article().commentCount }}
                        </span>
                        <span
                            class="flex items-center gap-1 rounded-lg bg-black/60 px-2 py-1 text-[11px] font-medium text-white backdrop-blur-sm"
                        >
                            <mat-icon style="font-size: 12px; width: 12px; height: 12px"
                                >favorite_border</mat-icon
                            >
                            {{ article().reactionCount }}
                        </span>
                    </div>
                </div>
            }
            <div class="p-4">
                @if (showAuthor()) {
                    <div class="mb-2 flex items-center gap-2">
                        <div
                            class="flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-[8px] font-bold text-primary"
                        >
                            {{ article().userName.charAt(0) || '?' }}
                        </div>
                        <span class="text-xs font-semibold text-text-primary">{{
                            article().userName
                        }}</span>
                        <span class="text-[10px] text-text-tertiary">{{
                            article().createdAt | date: 'MMM d'
                        }}</span>
                    </div>
                }
                <p class="line-clamp-3 text-sm leading-relaxed text-text-secondary">
                    {{ article().content }}
                </p>
            </div>
            @if (showActions()) {
                <div class="border-t border-border px-4 py-2">
                    <ng-content select="[actions]" />
                </div>
            }
        </div>
    `,
})
export class ArticleCard {
    article = input.required<Article>();
    showAuthor = input(true);
    showActions = input(false);
    viewClick = output<string>();
}
