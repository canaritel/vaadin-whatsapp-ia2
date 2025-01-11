package es.televoip.views.clinica;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import es.televoip.model.entities.ClinicalData;
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
    private VerticalLayout patientList;

    // Inyecta I18nUtil
    private final I18nUtil i18nUtil;

    // ComboBoxes
    private ComboBox<String> sortComboBox;
    private ComboBox<String> statusFilterComboBox;
    private ComboBox<String> categoryFilterComboBox;

    private String currentSortOption = "Nombre"; // Valor por defecto
    private String currentStatusFilter = "";
    private String currentCategoryFilter = "";

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

        // Inicializar los filtros con la lista de pacientes ordenados por defecto
        loadPatientsOrderedByName();

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
    }

    private void createUserPanel() {
        userListLayout = new VerticalLayout();
        userListLayout.addClassName("user-list-panel");
        userListLayout.setWidth("35%");
        userListLayout.setHeight("100%");
        userListLayout.setPadding(true);
        userListLayout.setSpacing(true);

        // Header con búsqueda y ordenación
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();

        // Campo de búsqueda
        TextField searchField = new TextField();
        searchField.setPlaceholder(i18nUtil.get("filter.search.placeholder"));
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addClassName("custom-status-filter"); 
        searchField.setClearButtonVisible(true); // mostrar botón para limpiar la selección
        
        // Listener para agregar o quitar clase activa
        searchField.addValueChangeListener(event -> {
            if (!event.getValue().isEmpty()) {
                searchField.addClassName("filter-active");
            } else {
                searchField.removeClassName("filter-active");
            }

            filterUsers(event.getValue());
        });

        // ComboBox para opciones de ordenación
        sortComboBox = new ComboBox<>();
        sortComboBox.setPlaceholder("Ordenar por"); // Establecer el placeholder
        sortComboBox.setItems("Nombre", "Última Actualización", "Estado", "Categoría"); // Reemplazar "Severidad" por "Categoría"
        sortComboBox.setValue("Nombre"); // Valor por defecto
        sortComboBox.setClearButtonVisible(true); // mostrar botón para limpiar la selección
        // Listener para cambios en la ordenación
        sortComboBox.addValueChangeListener(event -> {
            String selectedSort = event.getValue();
            currentSortOption = selectedSort; // Actualizar la opción actual

            // Limpiar filtros secundarios
            currentStatusFilter = "";
            currentCategoryFilter = "";
            statusFilterComboBox.clear();
            categoryFilterComboBox.clear();
            statusFilterComboBox.setVisible(false);
            categoryFilterComboBox.setVisible(false);

            // Refrescar la lista de pacientes según la nueva opción de ordenación
            refreshUserList();
        });

        // ComboBox secundario para filtrar por Estado
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.setPlaceholder("Seleccionar Estado");
        statusFilterComboBox.setItems("Pendiente", "Urgente", "Completo"); // Ajusta según tus estados reales
        statusFilterComboBox.setVisible(false); // Oculto por defecto
        statusFilterComboBox.setClearButtonVisible(true); // mostrar botón para limpiar la selección

        // Listener para filtrar por Estado
        statusFilterComboBox.addValueChangeListener(event -> {
            currentStatusFilter = event.getValue();
            refreshUserList();
        });

        // ComboBox secundario para filtrar por Categoría
        categoryFilterComboBox = new ComboBox<>();
        categoryFilterComboBox.setPlaceholder("Seleccionar Categoría");
        // Asumiendo que CategoryService tiene un método para obtener todas las categorías
        List<String> categories = categoryManager.getAllCategoryNames();
        categoryFilterComboBox.setItems(categories);
        categoryFilterComboBox.setVisible(false); // Oculto por defecto

        // Listener para filtrar por Categoría
        categoryFilterComboBox.addValueChangeListener(event -> {
            currentCategoryFilter = event.getValue();
            refreshUserList();
        });

        // Listener para mostrar el ComboBox secundario según la selección en sortComboBox
        sortComboBox.addValueChangeListener(event -> {
            String selectedSort = event.getValue();
            if ("Estado".equals(selectedSort)) {
                statusFilterComboBox.setVisible(true);
                categoryFilterComboBox.setVisible(false);
            } else if ("Categoría".equals(selectedSort)) {
                categoryFilterComboBox.setVisible(true);
                statusFilterComboBox.setVisible(false);
            } else {
                statusFilterComboBox.setVisible(false);
                categoryFilterComboBox.setVisible(false);
            }
        });

        // Añadir los componentes al header
        header.add(searchField, sortComboBox, statusFilterComboBox, categoryFilterComboBox);
        header.setFlexGrow(1, searchField); // El campo de búsqueda ocupa el espacio restante

        userListLayout.add(header);
        userListLayout.add(patientList); // Añadir la instancia existente de patientList

        // Mostrar todos los usuarios iniciales ordenados por defecto (Nombre)
        refreshUserList();
    }
    
    /**
     * Carga y muestra la lista de pacientes ordenados alfabéticamente por nombre.
     */
    private void loadPatientsOrderedByName() {
        // Establece la opción de ordenación actual a "Nombre"
        currentSortOption = "Nombre";

        // Refresca la lista de pacientes con la ordenación por nombre
        refreshUserList();

        // Establece el valor del ComboBox para reflejar la opción seleccionada
        sortComboBox.setValue("Nombre");
    }

    /**
     * Método para refrescar la lista de pacientes según la opción de ordenación seleccionada y los filtros aplicados.
     */
    private void refreshUserList() {
        patientList.removeAll(); // Limpia la lista existente

        List<PatientData> patients = new ArrayList<>();

        // Obtener la lista de pacientes ordenados según la opción actual
        switch (currentSortOption) {
            case "Nombre":
                patients = dataManager.getAllPatientsOrderedByNameAsc();
                break;
            case "Última Actualización":
                patients = dataManager.getAllPatientsOrderedByLastUpdatedDesc();
                break;
            case "Estado":
                patients = dataManager.getAllPatientsOrderedByStatus();
                break;
            case "Categoría":
                patients = dataManager.getAllPatientsOrderedByCategoryAsc();
                break;
            default:
                patients = dataManager.getAllPatientsOrderedByNameAsc();
                break;
        }

        // Aplicar filtros adicionales
        if ("Estado".equals(currentSortOption) && !currentStatusFilter.isEmpty()) {
            patients.removeIf(patient -> !patient.getStatus().equalsIgnoreCase(currentStatusFilter));
        }

        if ("Categoría".equals(currentSortOption) && !currentCategoryFilter.isEmpty()) {
            patients.removeIf(patient -> !patientHasCategory(patient, currentCategoryFilter));
        }

        // Iterar sobre cada paciente y añadirlo a la UI
        for (PatientData patient : patients) {
            // Usa el método para cargar datos relacionados
            PatientData patientWithDetails = dataManager.getPatientWithClinicalData(patient.getPhoneNumber());

            HorizontalLayout patientItem = uiManager.createPatientListItem(patientWithDetails);
            patientList.add(patientItem);
        }

        // Si la lista está vacía, mostrar un mensaje
        if (patients.isEmpty()) {
            Div noResults = new Div();
            noResults.setText(i18nUtil.get("message.noPatients"));
            noResults.addClassName("no-results"); // Clase CSS para estilos
            patientList.add(noResults);
        }
    }

    /**
     * Verifica si un paciente tiene al menos un ClinicalData con la categoría especificada.
     *
     * @param patient           El paciente a verificar.
     * @param categoryToFilter  La categoría a buscar.
     * @return true si el paciente tiene al menos una ClinicalData con la categoría especificada, false en caso contrario.
     */
    private boolean patientHasCategory(PatientData patient, String categoryToFilter) {
        if (patient.getClinicalDataList() == null) {
            return false;
        }

        for (ClinicalData cd : patient.getClinicalDataList()) {
            if (cd.getCategory() != null && cd.getCategory().getName().equalsIgnoreCase(categoryToFilter)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Filtra la lista de pacientes según el término de búsqueda y la opción de ordenación seleccionada.
     *
     * @param searchTerm Término de búsqueda ingresado por el usuario.
     */
    private void filterUsers(String searchTerm) {
        // El filtrado se realiza dentro de refreshUserList según los filtros aplicados
        refreshUserList();
    }

    /**
     * Implementación del método de la interfaz Translatable. Este método se llama cuando cambia el idioma para actualizar los textos de la UI.
     */
    @Override
    public void updateTexts() {
        // Actualizar el título de la página
        getUI().ifPresent(ui -> ui.getPage().setTitle(i18nUtil.get("page.title.clinicData")));

        // Actualizar textos de los componentes existentes
        // Por ejemplo, actualizar los placeholders y labels
        sortComboBox.setPlaceholder("Ordenar por"); // Actualizar placeholder si es necesario
        statusFilterComboBox.setPlaceholder("Seleccionar Estado");
        categoryFilterComboBox.setPlaceholder("Seleccionar Categoría");
        // Actualizar otros textos según sea necesario

        // Recargar la página para aplicar las traducciones
        UI.getCurrent().getPage().reload();
    }
}
