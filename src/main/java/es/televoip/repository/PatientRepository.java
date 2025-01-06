package es.televoip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import es.televoip.model.entities.PatientData;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<PatientData, String> {

	boolean existsByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);

	Optional<PatientData> findByEmail(String email);

	//Optional<PatientData> findByPhoneNumber(String phoneNumber);
	@Query("SELECT p FROM PatientData p LEFT JOIN FETCH p.clinicalDataList WHERE p.phoneNumber = :phoneNumber")
	Optional<PatientData> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
	
	
	// Uso de JOIN FETCH //////////////////////////
   @Query("SELECT DISTINCT p FROM PatientData p " +
         "LEFT JOIN FETCH p.clinicalDataList cd " +
         "LEFT JOIN FETCH cd.category c")
   List<PatientData> findAllWithClinicalData();
   
   @Query("SELECT DISTINCT p FROM PatientData p " +
         "LEFT JOIN FETCH p.clinicalDataList cd " +
         "LEFT JOIN FETCH cd.category cat " + // Importante: JOIN FETCH con category
         "WHERE p.phoneNumber = :phoneNumber")
   Optional<PatientData> findByPhoneNumberWithClinicalData(@Param("phoneNumber") String phoneNumber);

}
