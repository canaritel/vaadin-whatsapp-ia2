package es.televoip.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import es.televoip.model.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    // Método existente para cargar subCategories
    @Query("SELECT DISTINCT c FROM Category c " +
          "LEFT JOIN FETCH c.subCategories " +
          "ORDER BY c.displayOrder")
    List<Category> findAllWithSubCategories();

    @Query("SELECT DISTINCT c FROM Category c " +
          "LEFT JOIN FETCH c.subCategories " +
          "WHERE c.id = :id")
    Optional<Category> findByIdWithSubCategories(@Param("id") String id);
        
}
