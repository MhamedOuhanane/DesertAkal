import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GuideDetail } from './guide-detail';

describe('GuideDetail', () => {
    let component: GuideDetail;
    let fixture: ComponentFixture<GuideDetail>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [GuideDetail],
        }).compileComponents();

        fixture = TestBed.createComponent(GuideDetail);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
