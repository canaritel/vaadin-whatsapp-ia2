package es.televoip.model.enums;

public enum PatientStatus {
	ACTIVE("active"),
	SUSPENDED("suspended"),
	INACTIVE("inactive");

	private final String value;

	PatientStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
}