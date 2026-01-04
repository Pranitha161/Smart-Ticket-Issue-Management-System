import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SlaRulesList } from './sla-rules-list';

describe('SlaRulesList', () => {
  let component: SlaRulesList;
  let fixture: ComponentFixture<SlaRulesList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SlaRulesList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SlaRulesList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
