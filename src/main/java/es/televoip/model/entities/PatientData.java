package es.televoip.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PreUpdate;
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
//@ToString(exclude = "data")
public class PatientData {

    /**
     * Identificador único del paciente.
     */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * Nombre del paciente.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Número de teléfono del paciente.
     */
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Teléfono inválido")
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    /**
     * Lista de datos clínicos asociados al paciente.
     */
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "patient_clinical_data",
        joinColumns = @JoinColumn(name = "patient_id"),
        inverseJoinColumns = @JoinColumn(name = "clinical_data_id")
    )
    private List<ClinicalData> clinicalDataList = new ArrayList<>();

    /**
     * Fecha y hora de creación del registro.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización del registro.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Método que se ejecuta antes de insertar la entidad en la base de datos.
     * Establece el campo 'id' y 'createdAt' si son nulos.
     */
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Método que se ejecuta antes de actualizar la entidad en la base de datos.
     * Actualiza el campo 'updatedAt'.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
