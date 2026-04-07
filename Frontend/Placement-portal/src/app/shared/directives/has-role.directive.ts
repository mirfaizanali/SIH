import { Directive, Input, inject, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Directive({ selector: '[appHasRole]', standalone: true })
export class HasRoleDirective {
  private authService = inject(AuthService);
  private viewContainer = inject(ViewContainerRef);
  private templateRef = inject(TemplateRef<unknown>);

  @Input() set appHasRole(role: string | string[]) {
    const roles = Array.isArray(role) ? role : [role];
    const userRole = this.authService.userRole();
    this.viewContainer.clear();
    if (userRole && roles.includes(userRole)) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }
}
