package es.televoip.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa datos clínicos asociados a un paciente.
 */
@Entity
@Table(name = "clinical_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalData {

	/**
	 * Identificador único del registro clínico.
	 */
	@Id
	@Column(name = "id", length = 36, updatable = false, nullable = false)
	private String id;

	/**
	 * Categoría del registro clínico.
	 */
	@ManyToOne(fetch = FetchType.LAZY)  // Cambiar de LAZY a EAGER
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	/**
	 * Título del registro clínico.
	 */
	@NotBlank(message = "El título es obligatorio")
	@Column(name = "title", nullable = false)
	private String title;

	/**
	 * Descripción del registro clínico.
	 */
	@NotBlank(message = "La descripción es obligatorio")
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	/**
	 * Estado del registro clínico (e.g., Pendiente, Completo).
	 */
	@NotBlank(message = "El estado es obligatorio")
	@Column(name = "status", nullable = false)
	private String status;

	/**
	 * Fecha y hora de creación del registro clínico.
	 */
	@Column(name = "date", nullable = false)
	private LocalDateTime date;
	
	@Override
   public String toString() {
        return "ClinicalData{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            // No incluir category para evitar lazy loading
            '}';
   }

	/**
	 * Método que se ejecuta antes de insertar la entidad en la base de datos. Establece el campo 'date' si es nulo.
	 */
	@PrePersist
	protected void onCreate() {
		if (this.id == null) {
			this.id = UUID.randomUUID().toString();
		}
		if (this.date == null) {
			this.date = LocalDateTime.now();
		}
	}

}
