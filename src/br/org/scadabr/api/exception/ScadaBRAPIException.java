package br.org.scadamy.api.exception;

import br.org.scadamy.api.vo.APIError;

public class ScadaBRAPIException extends Exception {
	private APIError error;

	public ScadaBRAPIException(APIError error) {
		this.error = error;
	}

	public void setError(APIError error) {
		this.error = error;
	}

	public APIError getError() {
		return error;
	}

}
