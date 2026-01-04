import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SlaCompliance } from './sla-compliance';

describe('SlaCompliance', () => {
  let component: SlaCompliance;
  let fixture: ComponentFixture<SlaCompliance>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SlaCompliance]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SlaCompliance);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
