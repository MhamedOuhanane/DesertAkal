import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GuideForm } from './guide-form';

describe('GuideForm', () => {
    let component: GuideForm;
    let fixture: ComponentFixture<GuideForm>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [GuideForm],
        }).compileComponents();

        fixture = TestBed.createComponent(GuideForm);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
