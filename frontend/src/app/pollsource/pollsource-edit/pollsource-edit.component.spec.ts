import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PollsourceEditComponent} from './pollsource-edit.component';

describe('PollsourceEditComponent', () => {
  let component: PollsourceEditComponent;
  let fixture: ComponentFixture<PollsourceEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PollsourceEditComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PollsourceEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
