import { Component, Input, Output, EventEmitter } from '@angular/core';
import { UploadService } from '../../services/upload.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-image-upload',
  template: `
    <div class="upload-area" (click)="fileInput.click()">
      <input #fileInput type="file" accept="image/*" (change)="onFileSelected($event)" style="display: none">
      <mat-icon *ngIf="!previewUrl">cloud_upload</mat-icon>
      <img *ngIf="previewUrl" [src]="previewImageUrl" class="preview" alt="Preview">
      <span class="hint">{{ hint }}</span>
    </div>
    <button *ngIf="previewUrl || currentUrl" mat-button color="warn" type="button" (click)="clear()">Remove</button>
  `,
  styles: [`
    .upload-area {
      border: 2px dashed #ccc; border-radius: 8px; padding: 24px; text-align: center; cursor: pointer;
      min-height: 120px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
    }
    .upload-area:hover { border-color: #999; background: #fafafa; }
    .preview { max-width: 200px; max-height: 150px; object-fit: contain; }
    .hint { color: #666; font-size: 12px; }
  `]
})
export class ImageUploadComponent {
  @Input() currentUrl = '';
  @Input() hint = 'Click or drop image (JPEG, PNG, max 5MB)';
  @Output() urlChange = new EventEmitter<string>();

  previewUrl = '';
  uploading = false;

  get previewImageUrl(): string {
    if (!this.previewUrl) return '';
    if (this.previewUrl.startsWith('http')) return this.previewUrl;
    const base = environment.apiUrl.replace(/\/api\/?$/, '');
    return base + (this.previewUrl.startsWith('/') ? this.previewUrl : '/' + this.previewUrl);
  }

  constructor(private uploadService: UploadService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    if (this.currentUrl) this.previewUrl = this.currentUrl;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input?.files?.[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      this.snackBar.open('File too large (max 5MB)', 'OK');
      return;
    }
    this.uploading = true;
    this.uploadService.uploadMenuImage(file).subscribe({
      next: res => {
        this.previewUrl = res.url;
        this.urlChange.emit(res.url);
        this.uploading = false;
      },
      error: () => { this.snackBar.open('Upload failed', 'OK'); this.uploading = false; }
    });
    input.value = '';
  }

  clear(): void {
    this.previewUrl = '';
    this.urlChange.emit('');
  }
}
