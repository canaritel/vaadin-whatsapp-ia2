package es.televoip.model.enums;

public enum ClinicalStatus {
   URGENTE("urgent", "Urgente"),
   PENDIENTE("pending", "Pendiente"),
   EN_CURSO("in_progress", "En curso"),
   COMPLETADO("completed", "Completado");

   private final String code;
   private final String displayName;

   ClinicalStatus(String code, String displayName) {
       this.code = code;
       this.displayName = displayName;
   }

   public String getCode() {
       return code;
   }

   public String getDisplayName() {
       return displayName;
   }

   public static ClinicalStatus fromString(String status) {
       if (status == null) {
           throw new IllegalArgumentException("El estado no puede ser nulo");
       }

       String normalizedStatus = status.trim().toLowerCase();
       for (ClinicalStatus clinicalStatus : values()) {
           if (clinicalStatus.code.equals(normalizedStatus) || 
               clinicalStatus.displayName.toLowerCase().equals(normalizedStatus)) {
               return clinicalStatus;
           }
       }
       throw new IllegalArgumentException("Estado clínico desconocido: " + status);
   }

   public String getI18nKey() {
       return "clinical.status." + code;
   }
   
}