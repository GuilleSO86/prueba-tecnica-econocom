import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SsoCallbackComponent } from './sso-callback.component';

describe('SsoCallbackComponent', () => {
  let component: SsoCallbackComponent;
  let fixture: ComponentFixture<SsoCallbackComponent>;

  beforeEach(() => {

    TestBed.configureTestingModule({
      declarations: [SsoCallbackComponent]
    });

    fixture = TestBed.createComponent(SsoCallbackComponent);

    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    
    expect(component).toBeTruthy();
  });
});
