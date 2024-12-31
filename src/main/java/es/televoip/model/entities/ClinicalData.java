package es.televoip.model.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ClinicalData {
	private String category;
	private String subCategory; // Nuevo campo para subcategoría
	private String title;
	private String description;
	private String status;
	
	@Builder.Default
   private LocalDateTime date = LocalDateTime.now();

// Constructor para 4 parámetros
   public ClinicalData(String category, String subCategory, String title, String description) {
       this(category, subCategory, title, description, "Pendiente", LocalDateTime.now());
   }

   // Constructor para 5 parámetros
   public ClinicalData(String category, String subCategory, String title, String description, String status) {
       this(category, subCategory, title, description, status, LocalDateTime.now());
   }
	
}