package es.televoip.model;

import java.util.List;

import es.televoip.model.entities.SubCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_configs")
@Data
@Builder(toBuilder = true) // Habilitar toBuilder()
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor
public class CategoryConfig {

	@Id
	private String id;

	private String name;

	private String icon;

	private boolean isActive;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
   @JoinColumn(name = "category_id")
	private List<SubCategory> subCategories;

	private int displayOrder;

}
