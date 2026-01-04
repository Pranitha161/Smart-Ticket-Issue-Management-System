import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EscalationSummary } from './escalation-summary';

describe('EscalationSummary', () => {
  let component: EscalationSummary;
  let fixture: ComponentFixture<EscalationSummary>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EscalationSummary]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EscalationSummary);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
