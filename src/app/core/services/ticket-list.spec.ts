import { TestBed } from '@angular/core/testing';

import { ticketListService } from './ticket-list';

describe('TicketList', () => {
  let service: ticketListService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ticketListService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
