package edu.ualberta.med.biobank.validators;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;

public class NonEmptyString implements IValidator {
	
	private final String message;
	
	private final ControlDecoration controlDecoration;
	
	public NonEmptyString(String message, ControlDecoration controlDecoration) {
		super();
		this.message = message;
		this.controlDecoration = controlDecoration;
	}

	@Override
	public IStatus validate(Object value) {
		if (! (value instanceof String)) {
			throw new RuntimeException(
			"Not supposed to be called for non-strings.");
		}

		if (((String) value).length() != 0) {
			controlDecoration.hide();
			return Status.OK_STATUS;
		}
		
		controlDecoration.show();
		return ValidationStatus.error(message);
	}

}
