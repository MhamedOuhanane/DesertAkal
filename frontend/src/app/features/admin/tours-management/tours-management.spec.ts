import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ToursManagement } from './tours-management';

describe('ToursManagement', () => {
  let component: ToursManagement;
  let fixture: ComponentFixture<ToursManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToursManagement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ToursManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
