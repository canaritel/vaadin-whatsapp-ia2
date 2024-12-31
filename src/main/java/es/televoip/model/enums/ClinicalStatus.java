package es.televoip.model.enums;

public enum ClinicalStatus {
    URGENTE("Urgente"),
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    COMPLETADO("Completado");

    private final String displayName;

    ClinicalStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre para mostrar del estado clínico.
     *
     * @return El nombre para mostrar.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convierte un string a un valor de ClinicalStatus.
     *
     * @param status El string que representa el estado.
     * @return El valor correspondiente de ClinicalStatus.
     * @throws IllegalArgumentException Si el estado no es reconocido.
     */
    public static ClinicalStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        String normalizedStatus = status.trim().toLowerCase();
        switch (normalizedStatus) {
            case "urgente":
                return URGENTE;
            case "pendiente":
                return PENDIENTE;
            case "en curso":
                return EN_CURSO;
            case "completado":
                return COMPLETADO;
            default:
                throw new IllegalArgumentException("Estado clínico desconocido: " + status);
        }
    }
}
