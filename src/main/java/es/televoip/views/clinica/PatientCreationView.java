package es.televoip.views.clinica;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import es.televoip.model.entities.PatientData;
import es.televoip.service.PatientService;
import es.televoip.util.I18nUtil;
import es.televoip.util.MyNotification;
import es.televoip.util.Translatable;
import es.televoip.views.MainLayout;

@Route(value = "patients", layout = MainLayout.class)
@PageTitle("Gestión de Pacientes")
public class PatientCreationView extends VerticalLayout implements Translatable  {
    private static final long serialVersionUID = 1L;
    
    private final PatientService patientService;
    private final I18nUtil i18nUtil;
    
    private Grid<PatientData> grid;
    private TextField filterField;
    private HorizontalLayout toolbar;

    public PatientCreationView(PatientService patientService, I18nUtil i18nUtil) {
        this.patientService = patientService;
        this.i18nUtil = i18nUtil;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createToolbar();
        createGrid();
        
        add(toolbar, grid);
    }

    private void createToolbar() {
       toolbar = new HorizontalLayout();
       toolbar.setWidthFull();
       toolbar.setSpacing(true);
       toolbar.setPadding(false);
       toolbar.addClassName("toolbar");

       // Campo de búsqueda
       filterField = new TextField();
       filterField.setPlaceholder(i18nUtil.get("filter.search.placeholder"));
       filterField.setPrefixComponent(VaadinIcon.SEARCH.create());
       filterField.setClearButtonVisible(true);
       filterField.setValueChangeMode(ValueChangeMode.LAZY);
       filterField.addClassName("custom-status-filter");
       filterField.setWidth("240px");

       // Botón de añadir
       Button addButton = new Button(i18nUtil.get("button.newPatient"), VaadinIcon.PLUS.create());
       addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
       addButton.addClickListener(e -> showPatientForm(new PatientData()));

       // Botones de filtro con estados
       Button viewActiveButton = new Button(i18nUtil.get("button.viewActive"));
       Button viewSuspendedButton = new Button(i18nUtil.get("button.viewSuspended"));
       
       viewActiveButton.addClassName("filter-button");
       viewSuspendedButton.addClassName("filter-button");
       
       // Establecer estado inicial
       viewActiveButton.addClassName("active");
       
       viewActiveButton.addClickListener(e -> {
           updateList("");
           viewActiveButton.addClassName("active");
           viewSuspendedButton.removeClassName("active");
       });
       
       viewSuspendedButton.addClickListener(e -> {
           showSuspendedPatients();
           viewSuspendedButton.addClassName("active");
           viewActiveButton.removeClassName("active");
       });

       // Añadir todos los componentes
       toolbar.add(filterField, addButton, viewActiveButton, viewSuspendedButton);
       toolbar.setAlignItems(Alignment.CENTER);
   }

    private void createGrid() {
        grid = new Grid<>(PatientData.class, false);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeightFull();
        grid.addClassName("v-grid"); // Clase CSS definida en clinica-chat.css

        // Definir columnas
        grid.addColumn(PatientData::getName)
            .setHeader("Nombre")
            .setAutoWidth(true)
            .setSortable(true);
            
        grid.addColumn(PatientData::getLastName)
            .setHeader("Apellidos")
            .setAutoWidth(true)
            .setSortable(true);
            
        grid.addColumn(PatientData::getPhoneNumber)
            .setHeader("Teléfono")
            .setAutoWidth(true);
            
        grid.addColumn(PatientData::getEmail)
            .setHeader("Email")
            .setAutoWidth(true);

        // Columna de acciones
        grid.addComponentColumn(patient -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            actions.addClassName("actions-container"); // Clase para estilizar botones de acción

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> showPatientForm(patient));
            editButton.setTooltipText("Editar paciente");

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> deletePatient(patient));
            deleteButton.setTooltipText("Eliminar paciente");

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Acciones").setAutoWidth(true);

        updateList("");
    }

    private void showPatientForm(PatientData patient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(patient.getId() == null ? "Nuevo Paciente" : "Editar Paciente");
        dialog.setWidth("600px");

        FormLayout form = new FormLayout();
        form.addClassName("dialog-layout"); // Clase CSS para estilizar el diálogo

        TextField nameField = new TextField("Nombre");
        nameField.setValue(patient.getName() != null ? patient.getName() : "");
        nameField.setRequired(true);

        TextField lastNameField = new TextField("Apellidos");
        lastNameField.setValue(patient.getLastName() != null ? patient.getLastName() : "");
        
        EmailField emailField = new EmailField("Email");
        emailField.setValue(patient.getEmail() != null ? patient.getEmail() : "");
        emailField.setErrorMessage("Por favor introduce un email válido");
        
        TextField phoneField = new TextField("Teléfono");
        phoneField.setValue(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "");
        phoneField.setRequired(true);
        phoneField.setPattern("^\\+?[0-9]{9,15}$");
        phoneField.setHelperText("Formato: +34XXXXXXXXX");

        // Campos adicionales
        Select<String> genderField = new Select<>();
        genderField.setLabel("Género");
        genderField.setItems("Masculino", "Femenino", "Otro");
        genderField.setValue(patient.getGender() != null ? patient.getGender() : "");

        TextArea addressField = new TextArea("Dirección");
        addressField.setValue(patient.getAddress() != null ? patient.getAddress() : "");

        form.add(
            nameField, lastNameField,
            emailField, phoneField,
            genderField, addressField
        );

        form.setColspan(addressField, 2);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Botones
        Button saveButton = new Button("Guardar", e -> {
            if (savePatient(patient, nameField, lastNameField, emailField, 
                          phoneField, genderField, addressField)) {
                dialog.close();
                updateList("");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancelButton, saveButton);
        //dialog.getFooter().addClassName("dialog-footer"); // Clase CSS para estilizar el footer

        dialog.open();
    }

    private boolean savePatient(PatientData patient, 
                              TextField nameField,
                              TextField lastNameField,
                              EmailField emailField,
                              TextField phoneField,
                              Select<String> genderField,
                              TextArea addressField) {
        
        try {
            // Validaciones
            if (nameField.getValue().trim().isEmpty() || phoneField.getValue().trim().isEmpty()) {
               MyNotification.showError("Nombre y teléfono son obligatorios", Position.TOP_CENTER, 3000); 
            	return false;
            }

            patient.setName(nameField.getValue());
            patient.setLastName(lastNameField.getValue());
            patient.setEmail(emailField.getValue());
            patient.setPhoneNumber(phoneField.getValue());
            patient.setGender(genderField.getValue());
            patient.setAddress(addressField.getValue());

            patientService.savePatient(patient);
            
            Notification.show("Paciente guardado correctamente", 
                            3000, Position.TOP_CENTER)
                      .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;

        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 
                            3000, Position.TOP_CENTER)
                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
    }
    
    private void deletePatient(PatientData patient) {
       try {
           // Obtener el paciente con datos clínicos cargados
           PatientData fullPatient = patientService.getPatientWithClinicalData(patient.getPhoneNumber());
           
           if (fullPatient.getClinicalDataList() != null && !fullPatient.getClinicalDataList().isEmpty()) {
               showSuspendDialog(fullPatient);
           } else {
               showDeleteDialog(fullPatient);
           }
       } catch (Exception e) {
           MyNotification.showError("Error al procesar la solicitud: " + e.getMessage());
       }
   }

    private void showSuspendDialog(PatientData patient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Suspender Paciente");

        Text message = new Text(i18nUtil.get("message.suspendPacient"));

        Button suspendButton = new Button("Suspender", e -> {
            try {
                patientService.suspendPatient(patient.getId());
                dialog.close();
                refreshGrid();
                MyNotification.show("Paciente suspendido correctamente");
            } catch (Exception ex) {
                MyNotification.showError("Error al suspender el paciente");
            }
        });

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        
        // Añadir botones y mensaje al diálogo
        dialog.add(new VerticalLayout(message, 
            new HorizontalLayout(cancelButton, suspendButton)));
        dialog.open();
    }

    private void showSuspendedPatients() {
       grid.setItems(patientService.getAllSuspendedPatients());
       toolbar.getChildren()
           .filter(component -> component instanceof Button)
           .map(component -> (Button) component)
           .forEach(button -> {
               if (button.getText().equals(i18nUtil.get("button.viewSuspended"))) {
                   button.addClassName("active");
               } else if (button.getText().equals(i18nUtil.get("button.viewActive"))) {
                   button.removeClassName("active");
               }
           });
   }

   private void updateList(String filterText) {
       if (filterText != null && !filterText.isEmpty()) {
           grid.setItems(patientService.findActiveByNameOrPhoneOrEmail(filterText));
       } else {
           grid.setItems(patientService.getAllActivePatients());
       }
       
       toolbar.getChildren()
           .filter(component -> component instanceof Button)
           .map(component -> (Button) component)
           .forEach(button -> {
               if (button.getText().equals(i18nUtil.get("button.viewActive"))) {
                   button.addClassName("active");
               } else if (button.getText().equals(i18nUtil.get("button.viewSuspended"))) {
                   button.removeClassName("active");
               }
           });
   }
    
    /**
     * Muestra el diálogo de confirmación para eliminar un paciente
     */
    private void showDeleteDialog(PatientData patient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmar eliminación");

        // Contenido del diálogo
        Text message = new Text("¿Estás seguro de que deseas eliminar al paciente " + 
                              patient.getName() + "?");

        // Botones
        Button deleteButton = new Button("Eliminar", event -> {
            try {
                patientService.deletePatient(patient.getId());
                dialog.close();
                refreshGrid();
                MyNotification.show("Paciente eliminado correctamente");
            } catch (Exception e) {
                MyNotification.showError("Error al eliminar el paciente: " + e.getMessage());
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Layout para los botones
        HorizontalLayout buttons = new HorizontalLayout(cancelButton, deleteButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        // Añadir componentes al diálogo
        VerticalLayout dialogLayout = new VerticalLayout(message, buttons);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Actualiza la grid con los datos más recientes según el filtro actual
     */
    private void refreshGrid() {
        String currentFilter = filterField.getValue();
        if (currentFilter != null && !currentFilter.isEmpty()) {
            grid.setItems(patientService.findActiveByNameOrPhoneOrEmail(currentFilter));
        } else {
            grid.setItems(patientService.getAllActivePatients());
        }
    }

    /**
 	 * Implementación del método de la interfaz Translatable. Este método se llama cuando cambia el idioma para actualizar los textos de la UI.
 	 */
 	@Override
 	public void updateTexts() {
 		// Actualizar el título de la página
 		getUI().ifPresent(ui -> ui.getPage().setTitle(i18nUtil.get("page.title.patients")));

 		// Actualizar textos de los componentes existentes
 		// Recargar la página para aplicar las traducciones
	   UI.getCurrent().getPage().reload();
 	}
 	
}
