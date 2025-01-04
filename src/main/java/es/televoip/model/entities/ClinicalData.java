package es.televoip.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

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
@ToString(exclude = {})
public class ClinicalData {

	/**
	 * Identificador único del registro clínico.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id; // Cambia a String y usa UUID si prefieres

	/**
	 * Categoría del registro clínico.
	 */
	@NotBlank(message = "La categoría es obligatoria")
	@Column(name = "category", nullable = false)
	private String category;

	/**
	 * Subcategoría del registro clínico.
	 */
	@NotBlank(message = "La subcategoría es obligatoria")
	@Column(name = "sub_category", nullable = false)
	private String subCategory;

	/**
	 * Título del registro clínico.
	 */
	@NotBlank(message = "El título es obligatorio")
	@Column(name = "title", nullable = false)
	private String title;

	/**
	 * Descripción del registro clínico.
	 */
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

	/**
	 * Método que se ejecuta antes de insertar la entidad en la base de datos. Establece el campo 'date' si es nulo.
	 */
	@PrePersist
	protected void onCreate() {
		if (this.date == null) {
			this.date = LocalDateTime.now();
		}
	}

}
