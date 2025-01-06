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
    
    private boolean viewingSuspended = false;
    
    private Grid.Column<PatientData> activeActionsColumn;
    private Grid.Column<PatientData> suspendedActionsColumn;


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
           .setHeader(i18nUtil.get("column.name"))
           .setAutoWidth(true)
           .setSortable(true);
           
       grid.addColumn(PatientData::getLastName)
           .setHeader(i18nUtil.get("column.lastName"))
           .setAutoWidth(true)
           .setSortable(true);
           
       grid.addColumn(PatientData::getPhoneNumber)
           .setHeader(i18nUtil.get("column.phone"))
           .setAutoWidth(true);
           
       grid.addColumn(PatientData::getEmail)
           .setHeader(i18nUtil.get("column.email"))
           .setAutoWidth(true);

       // Columna de acciones para pacientes activos (Editar y Eliminar)
       activeActionsColumn = grid.addComponentColumn(patient -> {
           HorizontalLayout actions = new HorizontalLayout();
           actions.setSpacing(true);
           actions.addClassName("actions-container"); // Clase para estilizar botones de acción

           // Botón Editar
           Button editButton = new Button(VaadinIcon.EDIT.create());
           editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
           editButton.addClickListener(e -> showPatientForm(patient));
           editButton.setTooltipText(i18nUtil.get("tooltip.editPatient"));

           // Botón Eliminar
           Button deleteButton = new Button(VaadinIcon.TRASH.create());
           deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
           deleteButton.addClickListener(e -> deletePatient(patient));
           deleteButton.setTooltipText(i18nUtil.get("tooltip.deletePatient"));

           actions.add(editButton, deleteButton);
           return actions;
       }).setHeader(i18nUtil.get("column.actions")).setAutoWidth(true);

       // Columna de acciones para pacientes suspendidos (Editar y Recuperar)
       suspendedActionsColumn = grid.addComponentColumn(patient -> {
           HorizontalLayout actions = new HorizontalLayout();
           actions.setSpacing(true);
           actions.addClassName("actions-container"); // Clase para estilizar botones de acción

           // Botón Editar
           Button editButton = new Button(VaadinIcon.EDIT.create());
           editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
           editButton.addClickListener(e -> showPatientForm(patient));
           editButton.setTooltipText(i18nUtil.get("tooltip.editPatient"));

           // Botón Recuperar
           Button recoverButton = new Button(VaadinIcon.ARROW_RIGHT.create());
           recoverButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
           recoverButton.addClickListener(e -> activatePatient(patient));
           recoverButton.setTooltipText(i18nUtil.get("tooltip.activatePatient"));

           actions.add(editButton, recoverButton);
           return actions;
       }).setHeader(i18nUtil.get("column.actions")).setAutoWidth(true);

       // Al inicio, mostrar solo la columna de acciones para activos
       activeActionsColumn.setVisible(true);
       suspendedActionsColumn.setVisible(false);

       // Establecer el mensaje de "No Data" para pacientes activos
       //grid.setEmptyMessage(i18nUtil.get("message.noActivePatients"));

       updateList("");
   }

    
    private void activatePatient(PatientData patient) {
       Dialog confirmDialog = new Dialog();
       confirmDialog.setHeaderTitle(i18nUtil.get("dialog.confirmActivate.title"));

       String confirmationMessage = String.format("%s %s?", 
           i18nUtil.get("dialog.confirmActivate.message"), 
           patient.getName());
       Text message = new Text(confirmationMessage);

       Button confirmButton = new Button(i18nUtil.get("button.activate"), e -> {
          try {
              patientService.reactivatePatient(patient.getId());
              refreshGrid(); // Refresca la vista actual (activos o suspendidos)
              confirmDialog.close();
              Notification.show(i18nUtil.get("notification.patientActivated"), 
                              3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          } catch (Exception ex) {
              Notification.show(i18nUtil.get("notification.errorActivating") + ": " + ex.getMessage(), 
                              3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
          }
      });

       confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

       Button cancelButton = new Button(i18nUtil.get("button.cancel"), e -> confirmDialog.close());
       cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

       // Añadir botones al diálogo
       HorizontalLayout buttons = new HorizontalLayout(cancelButton, confirmButton);
       buttons.setJustifyContentMode(JustifyContentMode.END);

       VerticalLayout dialogLayout = new VerticalLayout(message, buttons);
       dialogLayout.setPadding(true);
       dialogLayout.setSpacing(true);

       confirmDialog.add(dialogLayout);
       confirmDialog.open();
   }

    private void showPatientForm(PatientData patient) {
       Dialog dialog = new Dialog();
       dialog.setHeaderTitle(patient.getId() == null ? i18nUtil.get("dialog.newPatient.title") : i18nUtil.get("dialog.editPatient.title"));
       dialog.setWidth("600px");

       FormLayout form = new FormLayout();
       form.addClassName("dialog-layout"); // Clase CSS para estilizar el diálogo

       TextField nameField = new TextField(i18nUtil.get("field.name"));
       nameField.setValue(patient.getName() != null ? patient.getName() : "");
       nameField.setRequired(true);

       TextField lastNameField = new TextField(i18nUtil.get("field.lastName"));
       lastNameField.setValue(patient.getLastName() != null ? patient.getLastName() : "");
       
       EmailField emailField = new EmailField(i18nUtil.get("field.email"));
       emailField.setValue(patient.getEmail() != null ? patient.getEmail() : "");
       emailField.setErrorMessage(i18nUtil.get("validation.invalidEmail"));
       
       TextField phoneField = new TextField(i18nUtil.get("field.phone"));
       phoneField.setValue(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "");
       phoneField.setRequired(true);
       phoneField.setPattern("^\\+?[0-9]{9,15}$");
       phoneField.setHelperText(i18nUtil.get("helper.phoneFormat"));

       // Campos adicionales
       Select<String> genderField = new Select<>();
       genderField.setLabel(i18nUtil.get("field.gender"));
       genderField.setItems(i18nUtil.get("gender.male"), i18nUtil.get("gender.female"), i18nUtil.get("gender.other"));
       genderField.setValue(patient.getGender() != null ? patient.getGender() : "");

       TextArea addressField = new TextArea(i18nUtil.get("field.address"));
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
       Button saveButton = new Button(i18nUtil.get("button.save"), e -> {
          if (savePatient(patient, nameField, lastNameField, emailField, 
                        phoneField, genderField, addressField)) {
              dialog.close();
              refreshGrid(); // Refresca la vista actual (activos o suspendidos)
          }
       });

       saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

       Button cancelButton = new Button(i18nUtil.get("button.cancel"), e -> dialog.close());
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
        suspendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        
        // Añadir botones y mensaje al diálogo
        dialog.add(new VerticalLayout(message, new HorizontalLayout(cancelButton, suspendButton)));
        dialog.open();
    }

    private void showSuspendedPatients() {
       viewingSuspended = true; // Establecer el estado a suspendido
       
       // Cargar pacientes suspendidos
       grid.setItems(patientService.getAllSuspendedPatients());

       // Establecer el mensaje de "No Data" para suspendidos
       //grid.setEmptyMessage(i18nUtil.get("message.noSuspendedPatients"));

       // Mostrar la columna de acciones para suspendidos y ocultar la de activos
       activeActionsColumn.setVisible(false);
       suspendedActionsColumn.setVisible(true);

       // Actualizar clases de los botones en el toolbar
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
       viewingSuspended = false; // Establecer el estado a activo
       
       if (filterText != null && !filterText.isEmpty()) {
           grid.setItems(patientService.findActiveByNameOrPhoneOrEmail(filterText));
       } else {
           grid.setItems(patientService.getAllActivePatients());
       }
       
       // Establecer el mensaje de "No Data" para activos
       //grid.setEmptyMessage(i18nUtil.get("message.noActivePatients"));

       // Mostrar la columna de acciones para activos y ocultar la de suspendidos
       activeActionsColumn.setVisible(true);
       suspendedActionsColumn.setVisible(false);

       // Actualizar clases de los botones en el toolbar
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
      dialog.setHeaderTitle(i18nUtil.get("dialog.confirmDeletion.title"));

      // Contenido del diálogo
      String confirmationMessage = String.format("%s %s?", 
          i18nUtil.get("dialog.confirmDeletion.message"), 
          patient.getName());
      Text message = new Text(confirmationMessage);

      // Botones
      Button deleteButton = new Button(i18nUtil.get("button.delete"), event -> {
          try {
              patientService.deletePatient(patient.getId());
              dialog.close();
              refreshGrid();
              MyNotification.show(i18nUtil.get("notification.patientDeleted"));
          } catch (Exception e) {
              MyNotification.showError(i18nUtil.get("notification.errorDeleting") + ": " + e.getMessage(), Position.TOP_CENTER, 3000);
          }
      });
      deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

      Button cancelButton = new Button(i18nUtil.get("button.cancel"), e -> dialog.close());
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
      if (viewingSuspended) {
          // Si estamos viendo pacientes suspendidos, cargarlos
          grid.setItems(patientService.getAllSuspendedPatients());
          //grid.setEmptyMessage(i18nUtil.get("message.noSuspendedPatients"));
          
          // Mostrar/ocultar las columnas de acciones adecuadamente
          activeActionsColumn.setVisible(false);
          suspendedActionsColumn.setVisible(true);
      } else {
          // Si estamos viendo pacientes activos, aplicamos el filtro si existe
          if (currentFilter != null && !currentFilter.isEmpty()) {
              grid.setItems(patientService.findActiveByNameOrPhoneOrEmail(currentFilter));
          } else {
              grid.setItems(patientService.getAllActivePatients());
          }
          
          //grid.setEmptyMessage(i18nUtil.get("message.noActivePatients"));
          
          // Mostrar/ocultar las columnas de acciones adecuadamente
          activeActionsColumn.setVisible(true);
          suspendedActionsColumn.setVisible(false);
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
