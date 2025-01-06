package es.televoip.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import es.televoip.model.entities.ClinicalData;

@Repository
public interface ClinicalDataRepository extends JpaRepository<ClinicalData, String> {

	@Query("SELECT cd FROM ClinicalData cd " + "LEFT JOIN FETCH cd.category " + "WHERE cd.category.id = :categoryId")
	List<ClinicalData> findByCategoryIdWithCategory(@Param("categoryId") String categoryId);


	@Query("SELECT cd FROM ClinicalData cd LEFT JOIN FETCH cd.category WHERE cd.id = :id")
   Optional<ClinicalData> findByIdWithCategory(@Param("id") String id);
   
   @Query("SELECT DISTINCT cd FROM ClinicalData cd LEFT JOIN FETCH cd.category")
   List<ClinicalData> findAllWithCategory(); 

}
