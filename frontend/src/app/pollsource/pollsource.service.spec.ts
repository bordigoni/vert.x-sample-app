import {inject, TestBed} from '@angular/core/testing';

import {PollsourceService} from './pollsource.service';

describe('PollsourceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PollsourceService]
    });
  });

  it('should be created', inject([PollsourceService], (service: PollsourceService) => {
    expect(service).toBeTruthy();
  }));
});
