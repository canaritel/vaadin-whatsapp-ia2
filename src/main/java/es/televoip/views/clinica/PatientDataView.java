package es.televoip.views.clinica;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import es.televoip.model.entities.PatientData;
import es.televoip.service.CategoryService;
import es.televoip.service.PatientService;
import es.televoip.service.PatientlUIService;
import es.televoip.util.I18nUtil;
import es.televoip.util.LocaleChangeNotifier;
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

		/*
		// Verificar el estado de currentUser
		if (dataManager.getCurrentUser() != null) {
			System.out.println("currentUser está establecido por defecto: " + dataManager.getCurrentUser().getName());
		} else {
			System.out.println("currentUser es null al cargar la vista.");
		}
		*/
		
		 // Verificar currentUser
	    PatientData currentUser = dataManager.getCurrentUser();
	    if (currentUser != null) {
	        System.out.println("Usuario actual: " + currentUser.getName());
	    } else {
	        System.out.println("No hay un paciente seleccionado actualmente.");
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
	/*
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
	*/
	private void onPatientSelected(PatientData patient) {
	    if (patient == null) {
	        System.out.println("Ningún paciente seleccionado.");
	        messageList.removeAll(); // Limpia mensajes si no hay paciente
	        return;
	    }

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
		//uiManager.displayClinicalData(messageList, new ArrayList<>(), "all");
	}

	private void createUserPanel() {
		userListLayout = new VerticalLayout();
		userListLayout.addClassName("user-list-panel");
		userListLayout.setWidth("35%");
		userListLayout.setHeight("100%");
		userListLayout.setPadding(true);
		userListLayout.setSpacing(true);

		// Header con búsqueda
		HorizontalLayout header = new HorizontalLayout();
		//header.addClassName("user-panel-header");
		header.setWidthFull();

		TextField searchField = new TextField();
		searchField.setPlaceholder(i18nUtil.get("filter.search.placeholder"));
		searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.LAZY);
		searchField.addClassName("custom-status-filter"); 
		
		 // Listener para agregar o quitar clase activa
		searchField.addValueChangeListener(event -> {
          if (!event.getValue().isEmpty()) {
         	 searchField.addClassName("filter-active");
          } else {
         	 searchField.removeClassName("filter-active");
          }

          filterUsers(event.getValue());
      });

		header.add(searchField); // Añadir botón al header
		header.setFlexGrow(1, searchField); // El campo de búsqueda ocupa el espacio restante

		userListLayout.add(header);
		userListLayout.add(patientList); // Añadir la instancia existente de patientList

		// Mostrar todos los usuarios iniciales
		refreshUserList();
	}
	
	private void refreshUserList() {
	    patientList.removeAll(); // Limpia la lista

	    dataManager.getAllPatients().forEach(patient -> {
	        // Usa el método para cargar datos relacionados
	        PatientData patientWithDetails = dataManager.getPatientWithClinicalData(patient.getPhoneNumber());

	        HorizontalLayout patientItem = uiManager.createPatientListItem(patientWithDetails);
	        patientList.add(patientItem);
	    });
	}

	private void filterUsers(String searchTerm) {
	    patientList.removeAll(); // Limpia la lista

	    // Utilizar el método de servicio para buscar pacientes
	    List<PatientData> filteredPatients = dataManager.findByNameOrPhoneOrEmail(searchTerm);

	    filteredPatients.forEach(patient -> {
	        HorizontalLayout patientItem = uiManager.createPatientListItem(patient);
	        patientList.add(patientItem);
	    });

	    // Si no hay coincidencias, mostrar mensaje
	    if (filteredPatients.isEmpty()) {
	        Div noResults = new Div();
	        noResults.setText(i18nUtil.get("message.noMatches"));
	        noResults.addClassName("no-results"); // Clase CSS para estilos
	        patientList.add(noResults);
	    }
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
		//createUserPanel();
		//createChatPanel();
		//createClinicalPanel();
		
		// Recargar la página para aplicar las traducciones
	    UI.getCurrent().getPage().reload();
	}
	
}
