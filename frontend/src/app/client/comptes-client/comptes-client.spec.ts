import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComptesClient } from './comptes-client';

describe('ComptesClient', () => {
  let component: ComptesClient;
  let fixture: ComponentFixture<ComptesClient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComptesClient]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComptesClient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
