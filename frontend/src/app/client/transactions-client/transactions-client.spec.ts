import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionsClient } from './transactions-client';

describe('TransactionsClient', () => {
  let component: TransactionsClient;
  let fixture: ComponentFixture<TransactionsClient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionsClient]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransactionsClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
