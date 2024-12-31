package es.televoip.listener;

import es.televoip.model.entities.PatientData;

@FunctionalInterface
public interface PatientSelectionListener {
	
	void onPatientSelected(PatientData patient);
	
}
