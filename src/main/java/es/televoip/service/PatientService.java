package es.televoip.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import es.televoip.model.entities.Category;
import es.televoip.model.entities.ClinicalData;
import es.televoip.model.entities.PatientData;
import es.televoip.repository.PatientRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {
   private final PatientRepository patientRepository;
   
   // TODO: Considerar mover la gestión del usuario actual a un servicio de sesión separado
   private PatientData currentUser;
   
   public PatientService(PatientRepository patientRepository) {
       this.patientRepository = patientRepository;
   }

   /**
    * Añade un nuevo paciente con los datos proporcionados
    * @param phoneNumber Número de teléfono del paciente
    * @param data Datos del paciente a guardar
    */
   public void addPatientData(String phoneNumber, PatientData data) {
       patientRepository.save(data);
   }

   /**
    * Añade un paciente de prueba con datos básicos
    * @param phoneNumber Número de teléfono del paciente
    * @param name Nombre del paciente
    * @param data Lista inicial de datos clínicos
    */
   public void addDemoPatient(String phoneNumber, String name, List<ClinicalData> data) {
       PatientData patient = PatientData.builder()
           .phoneNumber(phoneNumber)
           .name(name)
           .clinicalDataList(new ArrayList<>(data))
           .build();
       patientRepository.save(patient);
   }

   /**
    * Obtiene todos los pacientes registrados en el sistema
    * @return Lista de todos los pacientes
    */
   @Transactional
   public List<PatientData> getAllPatients() {
       return patientRepository.findAll();
   }
   
   /**
    * Obtiene todos los datos clínicos de un paciente específico
    * @param phoneNumber Número de teléfono del paciente
    * @return Lista de datos clínicos del paciente o lista vacía si no existe
    */
   @Transactional
   public List<ClinicalData> getAllClinicalData(String phoneNumber) {
       return patientRepository.findByPhoneNumber(phoneNumber)
           .map(PatientData::getClinicalDataList)
           .orElse(new ArrayList<>());
   }

   /**
    * Busca un paciente por su número de teléfono
    * @param phoneNumber Número de teléfono del paciente
    * @return PatientData si existe, null si no se encuentra
    */
   @Transactional
   public PatientData getPatient(String phoneNumber) {
       return patientRepository.findByPhoneNumber(phoneNumber).orElse(null);
   }

   /**
    * Obtiene los datos clínicos de un paciente filtrados por categoría
    * @param phoneNumber Número de teléfono del paciente
    * @param category ID de la categoría a filtrar
    * @return Lista filtrada de datos clínicos
    */
   @Transactional
   public List<ClinicalData> getClinicalData(String phoneNumber, String category) {
       return patientRepository.findByPhoneNumber(phoneNumber)
           .map(patient -> patient.getClinicalDataList().stream()
               .filter(data -> data.getCategory().getId().equals(category))
               .collect(Collectors.toList()))
           .orElse(new ArrayList<>());
   }
   
   @Transactional
   public PatientData getPatientWithClinicalData(String phoneNumber) {
       return patientRepository.findByPhoneNumberWithClinicalData(phoneNumber)
               .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
   }


   /**
    * Establece el usuario actual en el sistema
    * @param phoneNumber Número de teléfono del usuario a establecer como actual
    */
   public void setCurrentUser(String phoneNumber) {
       this.currentUser = patientRepository.findByPhoneNumber(phoneNumber).orElse(null);
   }

   /**
    * Obtiene el usuario actual del sistema
    * @return Usuario actual o null si no hay ninguno establecido
    */
   @Transactional
   public PatientData getCurrentUser() {
       return currentUser;
   }

   /**
    * Añade un nuevo dato clínico a un paciente
    * @param phoneNumber Número de teléfono del paciente
    * @param data Dato clínico a añadir
    */
   @Transactional
   public void addClinicalData(String phoneNumber, ClinicalData data) {
       patientRepository.findByPhoneNumber(phoneNumber).ifPresent(patient -> {
           if (patient.getClinicalDataList() == null) {
               patient.setClinicalDataList(new ArrayList<>());
           }
           if (data.getId() == null) {
               data.setId(UUID.randomUUID().toString());
           }
           patient.getClinicalDataList().add(data);
           patientRepository.save(patient);

           // Actualizar currentUser si es el paciente actual
           if (currentUser != null && currentUser.getPhoneNumber().equals(phoneNumber)) {
               currentUser = patientRepository.findByPhoneNumberWithClinicalData(phoneNumber).orElse(patient);
           }
       });
   }

   /**
    * Actualiza un dato clínico existente del paciente actual
    * @param updatedData Dato clínico actualizado
    */
   @Transactional
   public void updateClinicalData(ClinicalData updatedData) {
       if (currentUser == null) {
           return;
       }

       // Obtener el paciente actualizado con sus datos clínicos
       PatientData patient = patientRepository.findByPhoneNumberWithClinicalData(currentUser.getPhoneNumber())
           .orElseThrow(() -> new RuntimeException("Patient not found"));

       boolean updated = false;
       for (int i = 0; i < patient.getClinicalDataList().size(); i++) {
           ClinicalData data = patient.getClinicalDataList().get(i);
           // Cambiar la comparación para usar IDs
           if (data.getId().equals(updatedData.getId())) {
               patient.getClinicalDataList().set(i, updatedData);
               patientRepository.save(patient);
               updated = true;
               break;
           }
       }

       if (!updated) {
           System.err.println("No se encontró el dato clínico a actualizar");
       }

       // Actualizar currentUser con los datos frescos
       currentUser = patientRepository.findByPhoneNumberWithClinicalData(currentUser.getPhoneNumber())
           .orElse(currentUser);
   }

   /**
    * Elimina un dato clínico del paciente actual
    * @param category Categoría del dato a eliminar
    * @param title Título del dato clínico a eliminar
    */
   @Transactional
   public void deleteClinicalData(Category category, String title) {
       System.out.println("=== Iniciando proceso de eliminación ===");
       if (currentUser == null) {
           System.out.println("Error: No hay usuario actual seleccionado");
           return;
       }
       
       System.out.println("Intentando eliminar dato clínico:");
       System.out.println("- Categoría: " + category.getName());
       System.out.println("- Título: " + title);
       System.out.println("- Usuario: " + currentUser.getName());

       PatientData patient = patientRepository.findByPhoneNumberWithClinicalData(currentUser.getPhoneNumber())
           .orElseThrow(() -> {
               System.out.println("Error: No se encontró el paciente en la base de datos");
               return new RuntimeException("Patient not found");
           });

       System.out.println("Datos antes de eliminar:");
       System.out.println("- Cantidad de datos clínicos: " + patient.getClinicalDataList().size());

       // Encontrar y eliminar el dato clínico específico
       boolean removed = patient.getClinicalDataList().removeIf(data -> {
           boolean matches = data.getCategory().getId().equals(category.getId()) && 
                            data.getTitle().equals(title);
           if (matches) {
               System.out.println("Encontrado dato clínico a eliminar:");
               System.out.println("- ID: " + data.getId());
               System.out.println("- Título: " + data.getTitle());
           }
           return matches;
       });

       if (removed) {
           System.out.println("Dato clínico eliminado de la lista");
           patientRepository.save(patient);
           System.out.println("Cambios guardados en la base de datos");
           
           // Actualizar el currentUser
           currentUser = patientRepository.findByPhoneNumberWithClinicalData(currentUser.getPhoneNumber())
               .orElse(currentUser);
           System.out.println("Usuario actual actualizado");
           System.out.println("Cantidad de datos clínicos después de eliminar: " + 
                             currentUser.getClinicalDataList().size());
       } else {
           System.out.println("ADVERTENCIA: No se encontró el dato clínico para eliminar");
       }
       
       System.out.println("=== Proceso de eliminación finalizado ===");
   }

   /**
    * Verifica si existe un paciente con el número de teléfono dado
    * @param phoneNumber Número de teléfono a verificar
    * @return true si existe, false si no
    */
   public boolean hasPatient(String phoneNumber) {
       return patientRepository.existsByPhoneNumber(phoneNumber);
   }
   
}
