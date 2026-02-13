import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-selector',
  template: `
    <button mat-icon-button [matMenuTriggerFor]="langMenu" aria-label="Select language">
      <mat-icon>language</mat-icon>
    </button>
    <mat-menu #langMenu="matMenu">
      <button mat-menu-item *ngFor="let lang of languages" (click)="switchLanguage(lang.code)">
        <span>{{ lang.flag }} {{ lang.name }}</span>
      </button>
    </mat-menu>
  `,
  styles: []
})
export class LanguageSelectorComponent {
  languages = [
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'hi', name: 'हिन्दी', flag: '🇮🇳' },
    { code: 'es', name: 'Español', flag: '🇪🇸' }
  ];

  constructor(private translate: TranslateService) {}

  switchLanguage(lang: string): void {
    this.translate.use(lang);
    localStorage.setItem('preferred-language', lang);
  }
}
