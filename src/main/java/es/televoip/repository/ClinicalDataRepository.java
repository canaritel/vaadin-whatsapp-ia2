package es.televoip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.televoip.model.entities.ClinicalData;

@Repository
public interface ClinicalDataRepository extends JpaRepository<ClinicalData, String> {
    // Métodos de consulta personalizados si es necesario
}
