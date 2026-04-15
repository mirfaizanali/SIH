import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Profile } from './profile';
import { ApiService } from '../../../core/services/api.service';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { MatDialogModule } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('Profile Component (Student)', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let apiServiceMock: any;

  beforeEach(async () => {
    apiServiceMock = {
      get: jasmine.createSpy('get').and.returnValue(of({
        data: {
          fullName: 'Test Student',
          email: 'test@student.com',
          preferredLocations: 'Mumbai, Pune',
          preferredJobTypes: 'FULL_TIME'
        }
      })),
      put: jasmine.createSpy('put').and.returnValue(of({
        data: {
          preferredLocations: 'Mumbai, Pune'
        }
      }))
    };

    await TestBed.configureTestingModule({
      imports: [Profile, MatDialogModule, NoopAnimationsModule],
      providers: [
        { provide: ApiService, useValue: apiServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize edit form with data from API including preferred locations', () => {
    expect(component.editForm().preferredLocations).toBe('Mumbai, Pune');
    expect(component.editForm().preferredJobTypes).toBe('FULL_TIME');
  });

  it('should update preferred location without hardcoding', () => {
    component.updateField('preferredLocations', 'Bangalore');
    expect(component.editForm().preferredLocations).toBe('Bangalore');
  });
});
