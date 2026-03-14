import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Language, LanguageFilters } from '../../../../core/models/language.model';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { LanguageDialog } from '../language-dialog/language-dialog';
import { LanguageService } from '../../../../core/services/language-service';

@Component({
    selector: 'app-language-list',
    standalone: true,
    imports: [FormsModule, MatIcon, PaginationComponent, DeleteDialog, LanguageDialog],
    templateUrl: './language-list.html',
})
export class LanguageList {
    private languageService = inject(LanguageService);

    pagination = signal<Pagination<Language> | null>(null);
    languages = computed<Language[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<LanguageFilters>({
        page: 0,
        size: 9,
        sortBy: 'name',
        order: 'asc',
        search: '',
    });

    showFormDialog = signal(false);
    languageToEdit = signal<Language | null>(null);
    isSubmitting = signal(false);

    showDeleteDialog = signal(false);
    languageToDelete = signal<Language | null>(null);
    isDeleting = signal(false);

    private searchSubject = new Subject<string>();

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.patchQuery({ search, page: 0 });
            });

        effect(() => {
            this.loadLanguages();
        });
    }

    private patchQuery(patch: Partial<LanguageFilters>): void {
        this.query.update((prev) => ({ ...prev, ...patch }));
    }

    async loadLanguages(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.languageService.findAll(this.query()));
            if (res.data) {
                this.pagination.set(res.data);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load languages');
        } finally {
            this.isLoading.set(false);
        }
    }

    onSearch(value: string): void {
        this.searchSubject.next(value);
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        const newOrder = sortBy === field && order === 'asc' ? 'desc' : 'asc';
        this.patchQuery({ sortBy: field, order: newOrder });
        this.loadLanguages();
    }

    goToPage(page: number): void {
        this.patchQuery({ page });
        this.loadLanguages();
    }

    openCreateDialog(): void {
        this.languageToEdit.set(null);
        this.showFormDialog.set(true);
    }

    openEditDialog(language: Language): void {
        this.languageToEdit.set(language);
        this.showFormDialog.set(true);
    }

    closeFormDialog(): void {
        this.showFormDialog.set(false);
        this.languageToEdit.set(null);
    }

    async onSave(data: { name: string; code: string }): Promise<void> {
        this.isSubmitting.set(true);
        try {
            const editing = this.languageToEdit();

            if (editing) {
                const dto: any = {};
                if (data.name !== editing.name) dto.name = data.name;
                if (data.code !== editing.code) dto.code = data.code;

                if (Object.keys(dto).length === 0) {
                    toast.info('No changes detected');
                    this.closeFormDialog();
                    return;
                }

                const res = await firstValueFrom(this.languageService.update(editing.uuid, dto));
                toast.success(res.message || 'Language updated successfully');
            } else {
                const res = await firstValueFrom(this.languageService.create(data));
                toast.success(res.message || 'Language created successfully');
            }

            this.closeFormDialog();
            await this.loadLanguages();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Operation failed');
        } finally {
            this.isSubmitting.set(false);
        }
    }

    openDeleteDialog(language: Language, event: Event): void {
        event.stopPropagation();
        this.languageToDelete.set(language);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const language = this.languageToDelete();
        if (!language) return;

        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.languageService.delete(language.uuid));
            toast.success(`Language "${language.name}" deleted`);
            this.showDeleteDialog.set(false);
            this.languageToDelete.set(null);
            await this.loadLanguages();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete language');
            this.showDeleteDialog.set(false);
        } finally {
            this.isDeleting.set(false);
        }
    }

    cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.languageToDelete.set(null);
    }

    getSortIcon(field: string): string {
        if (this.query().sortBy !== field) return '';
        return this.query().order === 'asc' ? 'arrow_upward' : 'arrow_downward';
    }
}
