package es.televoip.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.televoip.model.entities.CategoryConfig;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryConfig, String> {

	@Query("SELECT c FROM CategoryConfig c LEFT JOIN FETCH c.subCategories")
	List<CategoryConfig> findAllWithSubCategories();

	@Query("SELECT c FROM CategoryConfig c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
	Optional<CategoryConfig> findByIdWithSubCategories(@Param("id") String id);

}