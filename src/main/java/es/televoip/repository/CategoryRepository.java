package es.televoip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.televoip.model.CategoryConfig;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryConfig, String> {
	
	// Puedes definir métodos de consulta personalizados si son necesarios
	
}