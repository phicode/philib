package ch.bind.philib.validation;

public class ValidatableDummy implements Validatable {

	private boolean generateError;
	private int counter;

	public boolean isGenerateError() {
		return generateError;
	}

	public void setGenerateError(boolean generateError) {
		this.generateError = generateError;
	}

	@Override
	public void validate(ValidationResults results) {
		if (isGenerateError())
			results.addError("error " + (counter++));
	}

}
