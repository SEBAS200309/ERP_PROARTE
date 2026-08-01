import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AnimatedButtonComponent } from './animated-button.component';

describe('AnimatedButtonComponent', () => {
  let fixture: ComponentFixture<AnimatedButtonComponent>;
  let component: AnimatedButtonComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnimatedButtonComponent, NoopAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(AnimatedButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function getButton(): HTMLButtonElement {
    return fixture.debugElement.query(By.css('button')).nativeElement;
  }

  describe('rendering', () => {
    it('should render the button element', () => {
      const btn = getButton();
      expect(btn).toBeTruthy();
    });

    it('should default to button type', () => {
      const btn = getButton();
      expect(btn.type).toBe('button');
    });

    it('should set submit type when configured', () => {
      fixture.componentRef.setInput('type', 'submit');
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.type).toBe('submit');
    });

    it('should apply aria-label when provided', () => {
      fixture.componentRef.setInput('ariaLabel', 'Guardar registro');
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.getAttribute('aria-label')).toBe('Guardar registro');
    });
  });

  describe('variants', () => {
    it('should apply primary class by default', () => {
      const btn = getButton();
      expect(btn.classList).toContain('btn-primary');
    });

    it('should apply secondary class', () => {
      fixture.componentRef.setInput('variant', 'secondary');
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.classList).toContain('btn-secondary');
    });

    it('should apply danger class', () => {
      fixture.componentRef.setInput('variant', 'danger');
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.classList).toContain('btn-danger');
    });

    it('should apply ghost class', () => {
      fixture.componentRef.setInput('variant', 'ghost');
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.classList).toContain('btn-ghost');
    });
  });

  describe('disabled state', () => {
    it('should disable the native button when disabled input is true', () => {
      fixture.componentRef.setInput('disabled', true);
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.disabled).toBe(true);
    });

    it('should not emit buttonClick when disabled', () => {
      fixture.componentRef.setInput('disabled', true);
      fixture.detectChanges();

      let emitted = false;
      component.buttonClick.subscribe(() => (emitted = true));

      const btn = getButton();
      btn.click();
      expect(emitted).toBe(false);
    });
  });

  describe('loading state', () => {
    it('should disable the button when loading', () => {
      fixture.componentRef.setInput('loading', true);
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.disabled).toBe(true);
    });

    it('should show spinner when loading', () => {
      fixture.componentRef.setInput('loading', true);
      fixture.detectChanges();
      const spinner = fixture.debugElement.query(By.css('.btn-spinner'));
      expect(spinner).not.toBeNull();
    });

    it('should hide content when loading', () => {
      fixture.componentRef.setInput('loading', true);
      fixture.detectChanges();
      const content = fixture.debugElement.query(By.css('.btn-content'));
      expect(content.nativeElement.classList).toContain('hidden');
    });

    it('should set aria-busy when loading', () => {
      fixture.componentRef.setInput('loading', true);
      fixture.detectChanges();
      const btn = getButton();
      expect(btn.getAttribute('aria-busy')).toBe('true');
    });

    it('should not emit buttonClick when loading', () => {
      fixture.componentRef.setInput('loading', true);
      fixture.detectChanges();

      let emitted = false;
      component.buttonClick.subscribe(() => (emitted = true));

      const btn = getButton();
      btn.click();
      expect(emitted).toBe(false);
    });
  });

  describe('click handling', () => {
    it('should emit buttonClick on click', () => {
      let emitted = false;
      component.buttonClick.subscribe(() => (emitted = true));

      const btn = getButton();
      btn.click();
      expect(emitted).toBe(true);
    });
  });
});
