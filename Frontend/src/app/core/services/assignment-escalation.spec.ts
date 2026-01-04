import { TestBed } from '@angular/core/testing';

import { AssignmentEscalation } from './assignment-escalation';

describe('AssignmentEscalation', () => {
  let service: AssignmentEscalation;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AssignmentEscalation);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
