import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GuideList } from './guide-list';

describe('GuideList', () => {
    let component: GuideList;
    let fixture: ComponentFixture<GuideList>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [GuideList],
        }).compileComponents();

        fixture = TestBed.createComponent(GuideList);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
