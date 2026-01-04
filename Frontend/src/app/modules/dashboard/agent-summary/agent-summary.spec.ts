import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentSummary } from './agent-summary';

describe('AgentSummary', () => {
  let component: AgentSummary;
  let fixture: ComponentFixture<AgentSummary>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgentSummary]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AgentSummary);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
