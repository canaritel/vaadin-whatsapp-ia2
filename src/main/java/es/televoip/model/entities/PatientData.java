package es.televoip.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Entidad que representa a un paciente en el sistema.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientData {

	@Id
	@Column(name = "id", length = 36)
	private String id;

	@NotBlank(message = "El nombre es obligatorio")
	@Column(name = "name", nullable = false)
	private String name;

	//@NotBlank(message = "El apellido es obligatorio")
	@Column(name = "last_name", nullable = true)
	private String lastName;

	@Email(message = "Correo electrónico inválido")
	//@NotBlank(message = "El correo electrónico es obligatorio")
	@Column(name = "email", nullable = true, unique = true)
	private String email;

	//@Pattern(regexp = "\\+?[0-9]{7,15}", message = "Teléfono inválido")
	//@NotBlank(message = "El teléfono es obligatorio")
	@Column(name = "phone_number", nullable = false, unique = true)
	private String phoneNumber;

	//@NotBlank(message = "El género es obligatorio")
	@Column(name = "gender", nullable = true)
	private String gender;

	@Column(name = "address")
	private String address;

	@Builder.Default
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinTable(name = "patient_clinical_data", joinColumns = @JoinColumn(name = "patient_id"), inverseJoinColumns = @JoinColumn(name = "clinical_data_id"))
	private List<ClinicalData> clinicalDataList = new ArrayList<>();

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		if (this.id == null) {
			this.id = UUID.randomUUID().toString();
		}
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	
}
