import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrioritySummary } from './priority-summary';

describe('PrioritySummary', () => {
  let component: PrioritySummary;
  let fixture: ComponentFixture<PrioritySummary>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrioritySummary]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrioritySummary);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
