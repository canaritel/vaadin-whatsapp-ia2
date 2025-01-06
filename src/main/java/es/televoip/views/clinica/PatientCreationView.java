package es.televoip.views.clinica;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
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
        toolbar.addClassName("toolbar"); // Clase CSS definida en clinica-chat.css

        // Campo de búsqueda
        filterField = new TextField();
        filterField.setPlaceholder("Buscar paciente...");
        filterField.setPrefixComponent(VaadinIcon.SEARCH.create());
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addClassName("custom-status-filter"); // Clase CSS definida en clinica-chat.css
        filterField.setWidth("240px"); // Puedes ajustar o eliminar esta línea según los estilos CSS
        
        // Listener para agregar o quitar clase activa
        filterField.addValueChangeListener(event -> {
            if (!event.getValue().isEmpty()) {
                filterField.addClassName("filter-active");
            } else {
                filterField.removeClassName("filter-active");
            }

            updateList(event.getValue());
        });

        // Botón de añadir
        Button addButton = new Button("Nuevo Paciente", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> showPatientForm(new PatientData()));

        toolbar.add(filterField, addButton);
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
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(new Text("¿Estás seguro de que deseas eliminar al paciente " 
                                + patient.getName() + "?"));
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.addClassName("dialog-layout"); // Clase CSS para estilizar el diálogo

        Button deleteButton = new Button("Eliminar", e -> {
            patientService.deletePatient(patient.getId());
            updateList("");
            confirmDialog.close();
            Notification.show("Paciente eliminado correctamente", 
                            3000, Position.TOP_CENTER)
                      .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> confirmDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialogLayout.add(new HorizontalLayout(cancelButton, deleteButton));
        dialogLayout.addClassName("dialog-footer"); // Clase CSS para estilizar el footer

        confirmDialog.add(dialogLayout);
        confirmDialog.open();
    }

    private void updateList(String filterText) {
        if (filterText != null && !filterText.isEmpty()) {
            grid.setItems(patientService.findByNameOrPhoneOrEmail(filterText));
        } else {
            grid.setItems(patientService.getAllPatients());
        }
    }

	@Override
	public void updateTexts() {
		// Implementación para la internacionalización si es necesario
	}
}
