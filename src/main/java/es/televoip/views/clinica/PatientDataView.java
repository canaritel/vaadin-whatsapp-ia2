package es.televoip.views.clinica;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import es.televoip.model.entities.ClinicalData;
import es.televoip.model.entities.PatientData;
import es.televoip.service.CategoryService;
import es.televoip.service.PatientService;
import es.televoip.service.PatientlUIService;
import es.televoip.util.I18nUtil;
import es.televoip.util.LocaleChangeNotifier;
import es.televoip.util.MyNotification;
import es.televoip.util.Translatable;
import es.televoip.views.MainLayout;

@Route(value = "clinica-datos", layout = MainLayout.class) // Usa MainLayout como diseño principal
@PageTitle("Datos Pacientes") // Este título se actualizará dinámicamente
public class PatientDataView extends HorizontalLayout implements Translatable {
	private static final long serialVersionUID = 1L;

	private final PatientService dataManager;
	
	@SuppressWarnings("unused")
	private final CategoryService categoryManager;
	private PatientlUIService uiManager; // Ya no es final porque se inicializa después del patientList

	// Componentes principales
	private VerticalLayout userListLayout;
	private VerticalLayout chatLayout;
	private VerticalLayout messageList;
	//private String selectedCategory = null;
	private VerticalLayout patientList;
	//private String selectedCategoryId = null;

	// Inyecta I18nUtil
	private final I18nUtil i18nUtil;

	public PatientDataView(PatientService dataManager, CategoryService categoryManager, I18nUtil i18nUtil) {
		this.dataManager = dataManager;
		this.categoryManager = categoryManager;
		this.i18nUtil = i18nUtil;

		// Configurar el layout básico primero
		setSizeFull();
		setPadding(false);
		setSpacing(false);

		// Inicializar componentes en orden correcto
		initializeComponents();

		// Crear el ChatUIManager después de inicializar los componentes
		this.uiManager = new PatientlUIService(dataManager, categoryManager, i18nUtil);

		// Entonces configurar el uiManager
		this.uiManager.setPatientListLayout(patientList);
		this.uiManager.setPatientSelectionListener(this::onPatientSelected);

		// Añadir datos de demo
		//addDemoData();

		// Verificar el estado de currentUser
		if (dataManager.getCurrentUser() != null) {
			System.out.println("currentUser está establecido por defecto: " + dataManager.getCurrentUser().getName());
		} else {
			System.out.println("currentUser es null al cargar la vista.");
		}

		// Configurar layout
		setupLayout();

		// Registrar como listener para cambios de idioma
		LocaleChangeNotifier.addListener(this);
	}

	private void initializeComponents() {
		// Inicializar los layouts principales
		userListLayout = new VerticalLayout();
		chatLayout = new VerticalLayout();
		messageList = new VerticalLayout();

		// Inicializar patientList con configuración básica
		patientList = new VerticalLayout();
		patientList.setPadding(false);
		patientList.setSpacing(false);
		patientList.addClassName("patient-list");
	}

	/**
	 * Método que se ejecuta cuando se selecciona un paciente. Si hay una categoría seleccionada, se cargan los datos correspondientes.
	 */
	private void onPatientSelected(PatientData patient) {
		// Solo limpiar los datos, no los filtros
		uiManager.clearPreviousData();

		// Recargar datos con la categoría actual
		if (uiManager.getCurrentCategoryId() != null) {
			uiManager.loadCategoryData(uiManager.getCurrentCategoryId());
		} else {
			// Si no hay categoría seleccionada, cargar todos
			uiManager.loadCategoryData("all");
		}
	}

	private void setupLayout() {
		// Panel izquierdo (lista de usuarios)
		createUserPanel();

		// Panel central
		createChatPanel();

		// Configurar messageList en el uiManager
		uiManager.setMessageList(messageList);

		// Ahora sí inicializamos los filtros
		uiManager.displayClinicalData(messageList, new ArrayList<>(), "all");

		// Asegurarse de que no haya padding o margen en el layout principal
		setPadding(false);
		setSpacing(false);
		setSizeFull();

		add(userListLayout, chatLayout);
	}

	private void createChatPanel() {
		chatLayout = new VerticalLayout();
		chatLayout.setWidthFull();
		chatLayout.setHeightFull();
		chatLayout.setPadding(false);
		chatLayout.setSpacing(false);

		// Área de mensajes
		messageList = new VerticalLayout();
		messageList.addClassName("message-list");
		messageList.setHeightFull();

		chatLayout.add(messageList);

		// Inicializar los filtros aunque no haya datos
		uiManager.displayClinicalData(messageList, new ArrayList<>(), "all");
	}

	private void createUserPanel() {
		userListLayout = new VerticalLayout();
		userListLayout.addClassName("user-list-panel");
		userListLayout.setWidth("35%");
		userListLayout.setHeight("100%");
		userListLayout.setPadding(false);
		userListLayout.setSpacing(true);

		// Header con búsqueda
		HorizontalLayout header = new HorizontalLayout();
		header.addClassName("user-panel-header");
		header.setWidthFull();

		TextField searchField = new TextField();
		searchField.setPlaceholder(i18nUtil.get("filter.search.placeholder"));
		searchField.getStyle().set("margin-left", "14px"); // Añade margen izquierdo
		searchField.addValueChangeListener(e -> filterUsers(e.getValue())); // Usa el filtro correctamente

		// Botón con icono para añadir paciente
		Button addUserButton = new Button(new Icon(VaadinIcon.USER_CARD), e -> openAddUserDialog());
		addUserButton.setClassName("icon-button"); // Clase CSS para estilos opcionales
		addUserButton.getElement().setProperty("title", i18nUtil.get("tooltip.addClient")); // Tooltip al pasar el ratón

		header.add(searchField, addUserButton); // Añadir botón al header
		header.setFlexGrow(1, searchField); // El campo de búsqueda ocupa el espacio restante

		userListLayout.add(header);
		userListLayout.add(patientList); // Añadir la instancia existente de patientList

		// Mostrar todos los usuarios iniciales
		refreshUserList();
	}

	private void openAddUserDialog() {
		// Crear el diálogo
		Dialog dialog = new Dialog();
		dialog.setHeaderTitle(i18nUtil.get("dialog.addPatient.title"));
		dialog.setWidth("500px"); // Ancho ajustado
		dialog.setCloseOnOutsideClick(false); // Deshabilitar cierre al hacer clic fuera
		dialog.setCloseOnEsc(false); // Deshabilitar cierre con la tecla Escape

		// Campos del formulario (solo Nombre y Teléfono)
		TextField nameField = new TextField(i18nUtil.get("field.name"));
		nameField.setWidthFull(); // Ocupa todo el ancho

		TextField phoneField = new TextField(i18nUtil.get("field.phone"));
		phoneField.setWidthFull(); // Ocupa todo el ancho

		// Botón para guardar
		Button saveButton = new Button(i18nUtil.get("button.save"), e -> {
			String name = nameField.getValue();
			String phone = phoneField.getValue();

			// Validaciones básicas
			if (name.trim().isEmpty() || phone.trim().isEmpty()) {
				showMessageWarning(i18nUtil.get("notification.allFieldsRequired"));
				return;
			}

			// Validar unicidad del teléfono
			if (dataManager.hasPatient(phone)) {
				showMessageWarning(i18nUtil.get("notification.phoneAlreadyExists"));
				return;
			}

			// Crear una lista vacía de datos clínicos
			List<ClinicalData> clinicalData = new ArrayList<>();

			// Añadir el paciente al dataManager con datos clínicos vacíos
			dataManager.addDemoPatient(phone, name, clinicalData);

			// Refrescar la lista de usuarios
			refreshUserList();

			// Cerrar el diálogo
			dialog.close();

			// Opcional: Mostrar una notificación de éxito
			showMessage(i18nUtil.get("notification.patientAdded"));
		});

		saveButton.addClassName("save-button");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Estilizar como primario

		// Botón para cerrar
		Button closeButton = new Button(i18nUtil.get("button.close"), e -> dialog.close());
		closeButton.addClassName("close-button");

		// Contenedor del formulario
		VerticalLayout formLayout = new VerticalLayout(nameField, phoneField,
				new HorizontalLayout(saveButton, closeButton) // Botones en un layout horizontal
		);
		formLayout.setPadding(true);
		formLayout.setSpacing(true);

		dialog.add(formLayout);
		dialog.open();
	}

	private void refreshUserList() {
		patientList.removeAll(); // Limpia la lista

		// Agrega todos los pacientes actuales de dataManager
		dataManager.getAllPatients().forEach(patient -> {
			HorizontalLayout patientItem = uiManager.createPatientListItem(patient);
			patientList.add(patientItem);
		});
	}

	private void filterUsers(String searchTerm) {
		patientList.removeAll(); // Limpia la lista

		// Filtra los usuarios cuyo nombre o teléfono coincida con el término de búsqueda
		dataManager.getAllPatients().stream()
				.filter(patient -> patient.getName().toLowerCase().contains(searchTerm.toLowerCase())
						|| patient.getPhoneNumber().contains(searchTerm))
				.forEach(patient -> {
					HorizontalLayout patientItem = uiManager.createPatientListItem(patient);
					patientList.add(patientItem);
				});

		// Si no hay coincidencias, mostrar mensaje
		if (patientList.getComponentCount() == 0) {
			Div noResults = new Div();
			noResults.setText(i18nUtil.get("message.noMatches"));
			noResults.addClassName("no-results"); // Clase CSS para estilos
			patientList.add(noResults);
		}
	}

	private void createClinicalPanel() {
		VerticalLayout clinicalPanel = new VerticalLayout();
		clinicalPanel.addClassName("clinical-panel");
		clinicalPanel.setWidth("30%");

		// Resumen clínico
		H3 title = new H3(i18nUtil.get("clinical.info.title"));

		// Secciones de información
		createInfoSection(clinicalPanel, i18nUtil.get("clinical.info.lastVisit"), "10/12/2024");
		createInfoSection(clinicalPanel, i18nUtil.get("clinical.info.nextAppointment"), "15/12/2024");
		createInfoSection(clinicalPanel, i18nUtil.get("clinical.info.currentTreatment"),
				i18nUtil.get("clinical.info.currentTreatmentDetail"));

		clinicalPanel.add(title);
	}

	private void createInfoSection(VerticalLayout container, String label, String value) {
		Div section = new Div();
		section.addClassName("info-section");

		Span labelSpan = new Span(label);
		labelSpan.addClassName("info-label");

		Span valueSpan = new Span(value);
		valueSpan.addClassName("info-value");

		section.add(labelSpan, valueSpan);
		container.add(section);
	}

	private void showMessage(String message) {
		MyNotification.show(message, Notification.Position.MIDDLE, NotificationVariant.LUMO_SUCCESS, 3000);
	}

	private void showMessageWarning(String message) {
		MyNotification.showWarning(message, Notification.Position.MIDDLE, NotificationVariant.LUMO_WARNING, 3000);
	}

	/**
	 * Implementación del método de la interfaz Translatable. Este método se llama cuando cambia el idioma para actualizar los textos de la UI.
	 */
	@Override
	public void updateTexts() {
		// Actualizar el título de la página
		getUI().ifPresent(ui -> ui.getPage().setTitle(i18nUtil.get("page.title.clinicData")));

		// Actualizar textos de los componentes existentes
		// Por ejemplo, recrear los paneles para actualizar los textos dinámicamente
		createUserPanel();
		createChatPanel();
		createClinicalPanel();
	}
}