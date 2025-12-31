import { TestBed } from '@angular/core/testing';

import { SlaRule } from './sla-rule';

describe('SlaRule', () => {
  let service: SlaRule;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SlaRule);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
