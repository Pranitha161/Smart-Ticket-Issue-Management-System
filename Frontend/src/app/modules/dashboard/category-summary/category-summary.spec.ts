import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategorySummary } from './category-summary';

describe('CategorySummary', () => {
  let component: CategorySummary;
  let fixture: ComponentFixture<CategorySummary>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategorySummary]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategorySummary);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
