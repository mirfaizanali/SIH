import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span [class]="badgeClass()">{{ status() }}</span>`
})
export class StatusBadgeComponent {
  status = input<string>('');

  badgeClass = computed(() => {
    const s = this.status().toUpperCase();
    if (['ACTIVE', 'APPROVED', 'ACCEPTED', 'COMPLETED', 'VERIFIED'].includes(s)) return 'badge badge-success';
    if (['REJECTED', 'WITHDRAWN', 'CANCELLED', 'TERMINATED'].includes(s)) return 'badge badge-danger';
    if (['SUBMITTED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'SCHEDULED'].includes(s)) return 'badge badge-info';
    if (['UNDER_REVIEW', 'ONGOING', 'REVISION_REQUESTED', 'DRAFT'].includes(s)) return 'badge badge-warning';
    return 'badge badge-neutral';
  });
}
