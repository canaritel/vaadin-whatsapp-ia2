package es.televoip.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import es.televoip.model.entities.ClinicalData;
import es.televoip.model.entities.PatientData;
import lombok.Data;

@Data
@Component
public class ChatDataManager {
    private Map<String, PatientData> patientsData = new HashMap<>();
    private PatientData currentUser;

    // Método existente
    public void addPatientData(String phoneNumber, PatientData data) {
        patientsData.put(phoneNumber, data);
    }

    // Método para añadir datos de demo
    public void addDemoPatient(String phoneNumber, String name, List<ClinicalData> data) {
        PatientData patient = PatientData.builder()
            .phoneNumber(phoneNumber)
            .name(name)
            .data(new ArrayList<>(data)) // Asegura que la lista es mutable
            .build();
        addPatientData(phoneNumber, patient);
    }

    // Obtener todos los pacientes
    public List<PatientData> getAllPatients() {
        return new ArrayList<>(patientsData.values());
    }
    
    // Obtener todos los datos clínicos de un paciente
    public List<ClinicalData> getAllClinicalData(String phoneNumber) {
        PatientData patient = patientsData.get(phoneNumber);
        if (patient != null && patient.getData() != null) {
            return new ArrayList<>(patient.getData()); // Retorna una copia para evitar modificaciones externas
        }
        return new ArrayList<>();
    }

    // Obtener paciente por teléfono
    public PatientData getPatient(String phoneNumber) {
        return patientsData.get(phoneNumber);
    }

    // Obtener datos clínicos por categoría
    public List<ClinicalData> getClinicalData(String phoneNumber, String category) {
       PatientData patient = patientsData.get(phoneNumber);
       if (patient == null || patient.getData() == null) {
           return new ArrayList<>();
       }
       return patient.getData().stream()
           .filter(data -> data.getCategory().equalsIgnoreCase(category))
           .collect(Collectors.toList());
   }

    // Actualizar usuario actual
    public void setCurrentUser(String phoneNumber) {
        this.currentUser = patientsData.get(phoneNumber);
    }

    // Obtener usuario actual
    public PatientData getCurrentUser() {
        return currentUser;
    }

    // Agregar nuevo dato clínico
    public void addClinicalData(String phoneNumber, ClinicalData data) {
        PatientData patient = patientsData.get(phoneNumber);
        if (patient != null) {
            if (patient.getData() == null) {
                patient.setData(new ArrayList<>());
            }
            patient.getData().add(data);
        }
    }

    // Actualizar dato clínico existente
    public void updateClinicalData(ClinicalData updatedData) {
        PatientData patient = getCurrentUser();
        if (patient != null && patient.getData() != null) {
            // Buscar el dato clínico por categoría y título
            for (int i = 0; i < patient.getData().size(); i++) {
                ClinicalData data = patient.getData().get(i);
                if (data.getCategory().equalsIgnoreCase(updatedData.getCategory()) && data.getTitle().equalsIgnoreCase(updatedData.getTitle())) {
                    patient.getData().set(i, updatedData);
                    break;
                }
            }
        }
    }

    // Eliminar dato clínico
    public void deleteClinicalData(String category, String title) {
        PatientData patient = getCurrentUser();
        if (patient != null && patient.getData() != null) {
            patient.getData().removeIf(data -> 
                data.getCategory().equalsIgnoreCase(category) &&
                data.getTitle().equalsIgnoreCase(title)
            );
        }
    }

    // Verificar si existe un paciente
    public boolean hasPatient(String phoneNumber) {
        return patientsData.containsKey(phoneNumber);
    }
    
}
