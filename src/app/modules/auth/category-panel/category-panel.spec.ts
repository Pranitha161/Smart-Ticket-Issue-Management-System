import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryPanel } from './category-panel';

describe('CategoryPanel', () => {
  let component: CategoryPanel;
  let fixture: ComponentFixture<CategoryPanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryPanel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoryPanel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
