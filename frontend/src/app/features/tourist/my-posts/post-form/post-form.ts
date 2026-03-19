import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ArticleService } from '../../../../core/services/article-service';
import { Article } from '../../../../core/models/article.models';

@Component({
    selector: 'app-post-form',
    imports: [ReactiveFormsModule, RouterLink, MatIcon],
    templateUrl: './post-form.html',
})
export class PostForm implements OnInit {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private articleService = inject(ArticleService);

    form!: FormGroup;
    isEditMode = signal(false);
    isLoading = signal(true);
    isSubmitting = signal(false);
    articleUuid = signal<string | null>(null);
    existingArticle = signal<Article | null>(null);

    coverImageFile = signal<File | null>(null);
    coverImagePreview = signal<string | null>(null);
    existingCoverImage = signal<string | null>(null);
    coverImageRemoved = signal(false);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');

        this.form = this.fb.group({
            content: [
                '',
                [Validators.required, Validators.minLength(10), Validators.maxLength(5000)],
            ],
        });

        if (uuid) {
            this.isEditMode.set(true);
            this.articleUuid.set(uuid);
            await this.loadArticle(uuid);
        } else {
            this.isLoading.set(false);
        }
    }

    private async loadArticle(uuid: string): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.articleService.find(uuid));
            const article = res.data!;
            this.existingArticle.set(article);

            this.form.patchValue({
                content: article.content,
            });

            if (article.coverImage) {
                this.existingCoverImage.set(article.coverImage);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Article not found');
            this.router.navigate(['/tourist/dashboard/posts']);
        } finally {
            this.isLoading.set(false);
        }
    }

    onCoverImageSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.[0]) return;

        const file = input.files[0];
        if (!file.type.startsWith('image/')) {
            toast.error('Please select an image');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            toast.error('Image must be less than 5MB');
            return;
        }

        this.coverImageFile.set(file);
        this.coverImageRemoved.set(false);

        const reader = new FileReader();
        reader.onload = () => {
            this.coverImagePreview.set(reader.result as string);
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    removeCoverImage(): void {
        this.coverImageFile.set(null);
        this.coverImagePreview.set(null);

        if (this.isEditMode()) {
            this.coverImageRemoved.set(true);
        }
    }

    get displayImage(): string | null {
        if (this.coverImagePreview()) return this.coverImagePreview();
        if (!this.coverImageRemoved() && this.existingCoverImage())
            return this.existingCoverImage();
        return null;
    }

    get hasImageChange(): boolean {
        return this.coverImageFile() !== null || this.coverImageRemoved();
    }

    get hasContentChange(): boolean {
        if (!this.existingArticle()) return true;
        return this.form.value.content !== this.existingArticle()!.content;
    }

    get hasAnyChange(): boolean {
        return this.hasContentChange || this.hasImageChange;
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        if (!this.isEditMode() && !this.coverImageFile()) {
            toast.error('Please add a cover image');
            return;
        }

        if (this.isEditMode() && !this.hasAnyChange) {
            toast.info('No changes detected');
            return;
        }

        this.isSubmitting.set(true);

        try {
            if (this.isEditMode()) {
                await this.updateArticle();
            } else {
                await this.createArticle();
            }
            this.router.navigate(['/tourist/dashboard/posts']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to save post');
        } finally {
            this.isSubmitting.set(false);
        }
    }

    private async createArticle(): Promise<void> {
        const formData = new FormData();

        const articleBlob = new Blob([JSON.stringify({ content: this.form.value.content })], {
            type: 'application/json',
        });
        formData.append('article', articleBlob);
        formData.append('coverImage', this.coverImageFile()!);

        await firstValueFrom(this.articleService.create(formData));
        toast.success('Post published!');
    }

    private async updateArticle(): Promise<void> {
        const uuid = this.articleUuid()!;

        if (this.hasImageChange && !this.hasContentChange) {
            if (this.coverImageFile()) {
                await firstValueFrom(this.articleService.updateImage(uuid, this.coverImageFile()!));
                toast.success('Cover image updated');
            }
            return;
        }

        if (this.hasContentChange && !this.hasImageChange) {
            const formData = new FormData();
            const articleBlob = new Blob(
                [
                    JSON.stringify({
                        content: this.form.value.content,
                    }),
                ],
                { type: 'application/json' },
            );
            formData.append('article', articleBlob);

            await firstValueFrom(this.articleService.update(uuid, formData));
            toast.success('Post updated');
            return;
        }

        if (this.hasContentChange && this.hasImageChange) {
            const formData = new FormData();
            const articleBlob = new Blob(
                [
                    JSON.stringify({
                        content: this.form.value.content,
                    }),
                ],
                { type: 'application/json' },
            );
            formData.append('article', articleBlob);

            if (this.coverImageFile()) {
                formData.append('coverImage', this.coverImageFile()!);
            }

            await firstValueFrom(this.articleService.update(uuid, formData));
            toast.success('Post updated');
        }
    }

    get contentLength(): number {
        return this.form.get('content')?.value?.length || 0;
    }
}
