package es.televoip.model.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder(toBuilder = true) // Habilitar toBuilder()
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor
public class Category {

	@Id
	private String id;

	private String name;

	private String icon;

	private boolean isActive;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "category_id", nullable = false)
	private List<SubCategory> subCategories;

	private int displayOrder;

}
