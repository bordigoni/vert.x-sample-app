import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PollsourceListComponent} from './pollsource-list.component';

describe('PollsourceListComponent', () => {
  let component: PollsourceListComponent;
  let fixture: ComponentFixture<PollsourceListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PollsourceListComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PollsourceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
