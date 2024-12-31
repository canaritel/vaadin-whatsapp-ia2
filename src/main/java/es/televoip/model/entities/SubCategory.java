package es.televoip.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "sub_categories")
@Data
@Builder(toBuilder = true) // Permitir toBuilder
@AllArgsConstructor
public class SubCategory {

	@Id
	private String id; // Identificador único

	private String name; // Nombre de la subcategoría

	private String icon; // Icono de la subcategoría

	private boolean isActive; // Si está activa

	private boolean isRequired; // Si es obligatoria

	private int displayOrder; // Orden de visualización

	// Constructor simplificado para uso rápido
	public SubCategory(String id, String name, boolean isActive) {
		this.id = id;
		this.name = name;
		this.isActive = isActive;
		this.isRequired = false;
		this.displayOrder = 0;
		this.icon = null;
	}

}