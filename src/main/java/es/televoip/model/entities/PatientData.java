package es.televoip.model.entities;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientData {
	private String phoneNumber;
	private String name;
	private List<ClinicalData> data;
	
}