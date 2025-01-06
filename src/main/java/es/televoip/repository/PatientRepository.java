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

	// Uso de JOIN FETCH para cargar clinicalDataList
	@Query("SELECT p FROM PatientData p LEFT JOIN FETCH p.clinicalDataList WHERE p.phoneNumber = :phoneNumber")
	Optional<PatientData> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

	// Cargar todas las asociaciones necesarias
	@Query("SELECT DISTINCT p FROM PatientData p " 
			+ "LEFT JOIN FETCH p.clinicalDataList cd "
			+ "LEFT JOIN FETCH cd.category c")
	List<PatientData> findAllWithClinicalData();

	// Cargar un paciente con sus datos clínicos y categorías
	@Query("SELECT DISTINCT p FROM PatientData p " 
			+ "LEFT JOIN FETCH p.clinicalDataList cd "
			+ "LEFT JOIN FETCH cd.category cat " + "WHERE p.phoneNumber = :phoneNumber")
	Optional<PatientData> findByPhoneNumberWithClinicalData(@Param("phoneNumber") String phoneNumber);

	/**
	 * Busca pacientes cuyo nombre, número de teléfono o correo electrónico contengan el texto de filtro.
	 *
	 * @param filterText Texto de búsqueda para filtrar pacientes.
	 * @return Lista de pacientes que coinciden con el criterio de búsqueda.
	 */
	@Query("SELECT DISTINCT p FROM PatientData p " + "LEFT JOIN FETCH p.clinicalDataList cd "
			+ "LEFT JOIN FETCH cd.category c " + "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :filterText, '%')) "
			+ "OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :filterText, '%')) "
			+ "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :filterText, '%'))")
	List<PatientData> findByNameOrPhoneOrEmail(@Param("filterText") String filterText);

	// Nueva consulta para obtener pacientes suspendidos
	@Query("SELECT DISTINCT p FROM PatientData p " + "LEFT JOIN FETCH p.clinicalDataList cd "
			+ "LEFT JOIN FETCH cd.category c " + "WHERE p.status = 'suspended'")
	List<PatientData> findAllSuspendedPatients();

	// Nueva consulta para obtener pacientes activos con filtro
	@Query("SELECT DISTINCT p FROM PatientData p " + "LEFT JOIN FETCH p.clinicalDataList cd "
			+ "LEFT JOIN FETCH cd.category c " + "WHERE p.status = 'active' AND ("
			+ "LOWER(p.name) LIKE LOWER(CONCAT('%', :filterText, '%')) "
			+ "OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :filterText, '%')) "
			+ "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :filterText, '%'))" + ")")
	List<PatientData> findActiveByNameOrPhoneOrEmail(@Param("filterText") String filterText);

	// Nueva consulta para obtener todos los pacientes activos
	@Query("SELECT DISTINCT p FROM PatientData p " + "LEFT JOIN FETCH p.clinicalDataList cd "
			+ "LEFT JOIN FETCH cd.category c " + "WHERE p.status = 'active'")
	List<PatientData> findAllActivePatients();

	@Query("SELECT DISTINCT p FROM PatientData p " 
			+ "LEFT JOIN FETCH p.clinicalDataList " 
			+ "WHERE p.id = :id")
	Optional<PatientData> findByIdWithClinicalData(@Param("id") String id);

	@Query("SELECT p FROM PatientData p WHERE p.status = :status")
	List<PatientData> findByStatus(@Param("status") String status);

}
