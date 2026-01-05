import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentBoard } from './agent-board';

describe('AgentBoard', () => {
  let component: AgentBoard;
  let fixture: ComponentFixture<AgentBoard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgentBoard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AgentBoard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
