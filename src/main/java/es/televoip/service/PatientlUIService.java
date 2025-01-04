package es.televoip.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;

import es.televoip.listener.PatientSelectionListener;
import es.televoip.model.entities.Category;
import es.televoip.model.entities.ClinicalData;
import es.televoip.model.entities.PatientData;
import es.televoip.model.enums.ClinicalStatus;
import es.televoip.util.I18nUtil;
import es.televoip.util.MyNotification;
import es.televoip.util.StringUtils;

@Service
public class PatientlUIService {
	private final PatientService dataManager;
	private final CategoryService categoryManager;

	private PatientSelectionListener patientSelectionListener; // Listener
	private VerticalLayout patientListLayout; // Nuevo atributo

	// Variables para mantener el estado de los filtros
	private String currentStatusFilter = "Todos";
	private String currentSearchTerm = "";

	// Componentes de filtro
	private TextField searchField;
	private ComboBox<String> statusFilter;

	// Datos originales para aplicar filtros
	private List<ClinicalData> allData;

	private VerticalLayout messageList;

	private String currentCategoryId; // Nuevo campo para rastrear la categoría actual

	// Referencia al layout de filtros
	private HorizontalLayout filterLayout;

	private ComboBox<Category> categoryFilter;

	// Estado de los filtros
	private boolean areFiltersCreated = false; // Nuevo flag para rastrear si los filtros ya fueron creados

	private final I18nUtil i18nUtil;

	private static final String CATEGORY_ALL_ID = "all";
	private static String CATEGORY_ALL_NAME;
	
	private static final String STATUS_ALL_ID = "all";
	private static String STATUS_ALL_NAME;

	public void setMessageList(VerticalLayout messageList) {
		this.messageList = messageList;
	}

	public PatientlUIService(PatientService dataManager, CategoryService categoryManager, I18nUtil i18nUtil) {
		this.dataManager = dataManager;
		this.categoryManager = categoryManager;
		this.i18nUtil = i18nUtil;

		CATEGORY_ALL_NAME = i18nUtil.get("clinical.category.all");
		STATUS_ALL_NAME = i18nUtil.get("clinical.status.all");
	}

	// Método setter para el listener
	public void setPatientSelectionListener(PatientSelectionListener listener) {
		this.patientSelectionListener = listener;
	}

	// Setter para el layout de la lista de pacientes
	public void setPatientListLayout(VerticalLayout layout) {
		this.patientListLayout = layout;
	}

	// Método para notificar la selección de un paciente
	protected void notifyPatientSelection(PatientData patient) {
		if (patientSelectionListener != null) {
			patientSelectionListener.onPatientSelected(patient);
		}
	}

	/**
	 * Método para mostrar datos clínicos en la interfaz con filtros de búsqueda y estado.
	 *
	 * @param container El contenedor donde se mostrarán los datos.
	 * @param data      La lista de datos clínicos a mostrar.
	 */
	public void displayClinicalData(VerticalLayout container, List<ClinicalData> newData, String categoryId) {
		if (container == null)
			return;

		this.currentCategoryId = categoryId;
		this.allData = new ArrayList<>(newData);

		if (!areFiltersCreated) {
			createLayout(container);
		}

		if (messageList != null) {
			applyFilters(container);
		}
	}

	/**
	 * Crea y añade el layout de filtros al contenedor.
	 *
	 * @param container El contenedor donde se añadirán los filtros.
	 */
	private void createLayout(VerticalLayout container) {
		filterLayout = new HorizontalLayout();
		filterLayout.addClassName("filter-layout");
		filterLayout.setWidth("97%");
		filterLayout.setSpacing(false);
		filterLayout.setPadding(false);
		filterLayout.setAlignItems(Alignment.CENTER);

		// Campo de búsqueda
		searchField = createSearchField();

		// ComboBox de categorías
		categoryFilter = createCategoryFilter();

		// Filtro de estado
		statusFilter = createStatusFilter();

		filterLayout.add(categoryFilter, statusFilter, searchField);

		if (container.getComponentCount() > 0) {
			container.addComponentAsFirst(filterLayout);
		} else {
			container.add(filterLayout);
		}

		// No aplicar filtros aquí
		areFiltersCreated = true;
	}
	

	// En ClinicalUIService
	public void loadCategoryData(String category) {
		if (dataManager.getCurrentUser() == null) {
			showMessageWarning(i18nUtil.get("message.selectPatientFirst"));
			return;
		}

		List<ClinicalData> data;
		if ("all".equals(category)) {
			data = dataManager.getAllClinicalData(dataManager.getCurrentUser().getPhoneNumber());
		} else {
			data = dataManager.getClinicalData(dataManager.getCurrentUser().getPhoneNumber(), category);
		}

		// No limpiar todo el messageList, solo los datos
		clearPreviousData();
		displayClinicalData(messageList, data, category);
	}

	private ComboBox<Category> createCategoryFilter() {
		ComboBox<Category> filterCategory = new ComboBox<>();

		// Usar i18nUtil para el placeholder
		filterCategory.setPlaceholder(i18nUtil.get("filter.category.placeholder"));
		filterCategory.addClassName("custom-status-filter");
		filterCategory.setWidth("240px");

		// Crear lista de categorías activas
		List<Category> activeCategories = new ArrayList<>(categoryManager.getActiveCategories());

		// Crear la categoría ficticia "Todos"
		Category allCategory = Category.builder().id(CATEGORY_ALL_ID).name(CATEGORY_ALL_NAME).isActive(true) // Puede ser activo o no, según prefieras
				.build();

		// Prepend "Todos" al inicio de la lista de categorías
		List<Category> categoriesWithAll = new ArrayList<>();
		categoriesWithAll.add(allCategory);
		categoriesWithAll.addAll(activeCategories);

		filterCategory.setItems(categoriesWithAll);
		filterCategory.setItemLabelGenerator(Category::getName);

		// Permitir la selección nula si deseas manejar "Todos" como selección nula
		// filterCategory.setAllowCustomValue(false);
		// filterCategory.setEmptySelectionAllowed(true);

		// Listener para manejar la selección
		filterCategory.addValueChangeListener(event -> {
			Category selectedCategory = event.getValue();
			if (selectedCategory == null || CATEGORY_ALL_ID.equals(selectedCategory.getId())) {
				currentCategoryId = "all"; // valor por defecto interno
				filterCategory.removeClassName("filter-active");
			} else {
				currentCategoryId = selectedCategory.getId();
				filterCategory.addClassName("filter-active");
			}

			if (messageList != null && dataManager.getCurrentUser() != null) {
				loadCategoryData(currentCategoryId);
			}
		});

		return filterCategory;
	}

	private ComboBox<String> createStatusFilter() {
	    ComboBox<String> filterStatus = new ComboBox<>();

	    // Crear la lista de estados, añadiendo "Todos" como primera opción
	    List<String> estados = new ArrayList<>();
	    estados.add(STATUS_ALL_NAME); // "Todos" localizado
	    estados.addAll(Arrays.stream(ClinicalStatus.values())
	            .map(ClinicalStatus::getDisplayName)
	            .collect(Collectors.toList()));

	    filterStatus.setItems(estados);
	    filterStatus.setPlaceholder(i18nUtil.get("filter.status.placeholder"));
	    filterStatus.addClassName("custom-status-filter");
	    filterStatus.setWidth("240px");

	    // Listener para manejar la selección
	    filterStatus.addValueChangeListener(event -> {
	        String newValue = event.getValue();
	        if (newValue == null || STATUS_ALL_NAME.equals(newValue)) {
	            currentStatusFilter = STATUS_ALL_ID; // "all"
	            filterStatus.removeClassName("filter-active");
	        } else {
	            currentStatusFilter = newValue;
	            filterStatus.addClassName("filter-active");
	        }
	        applyFilters(messageList);
	    });

	    return filterStatus;
	}

	private TextField createSearchField() {
		TextField field = new TextField();
		field.setPlaceholder(i18nUtil.get("clinical.searchfield"));
		field.setWidth("260px");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		field.addClassName("custom-status-filter"); // Misma clase que el combo

		field.addValueChangeListener(event -> {
			currentSearchTerm = event.getValue();

			// Aplicar o remover clase activa basado en si hay texto
			if (!event.getValue().isEmpty()) {
				field.addClassName("filter-active");
			} else {
				field.removeClassName("filter-active");
			}

			applyFilters(messageList);
		});

		return field;
	}

	public String getCurrentCategoryId() {
		return currentCategoryId;
	}

	/**
	 * Aplica los filtros actuales (estado y búsqueda) a los datos clínicos y actualiza la UI.
	 *
	 * @param container El contenedor donde se mostrarán los datos.
	 */
	private void applyFilters(VerticalLayout container) {
		// Normalizar el término de búsqueda una sola vez
		String normalizedSearchTerm = StringUtils.removeAccents(currentSearchTerm.toLowerCase());

		// Filtrar los datos clínicos
		List<ClinicalData> filteredData = allData.stream().filter(cd -> {
			boolean matchesStatus = currentStatusFilter.equals("Todos")
					|| (cd.getStatus() != null && cd.getStatus().equalsIgnoreCase(currentStatusFilter));
			boolean matchesSearch = currentSearchTerm.isEmpty()
					|| (cd.getTitle() != null
							&& StringUtils.removeAccents(cd.getTitle().toLowerCase()).contains(normalizedSearchTerm))
					|| (cd.getDescription() != null
							&& StringUtils.removeAccents(cd.getDescription().toLowerCase()).contains(normalizedSearchTerm));
			return matchesStatus && matchesSearch;
		}).sorted(Comparator
				// Primero, ordenar por prioridad de estado (ascendente)
				.comparingInt((ClinicalData cd) -> getStatusPriority(cd.getStatus()))
				// Luego, por fecha (descendente)
				.thenComparing(ClinicalData::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
				// Finalmente, por displayOrder de la categoría (ascendente)
				.thenComparing(cd -> {
					return categoryManager.getCategoryById(cd.getCategory()).map(Category::getDisplayOrder)
							.orElse(Integer.MAX_VALUE);
				})).collect(Collectors.toList());

		// Crear una línea de tiempo
		VerticalLayout timeline = new VerticalLayout();
		timeline.addClassName("clinical-timeline");
		timeline.setPadding(false);
		timeline.setSpacing(false);
		timeline.setWidth("97%");

		if (filteredData.isEmpty()) {
			Div emptyMessage = new Div();
			emptyMessage.setText("No hay registros clínicos para mostrar.");
			emptyMessage.addClassName("empty-message");
			timeline.add(emptyMessage);
		} else {
			filteredData.forEach(item -> {
				// Crear un contenedor para cada evento
				HorizontalLayout eventLayout = new HorizontalLayout();
				eventLayout.setWidthFull(); // Asegura que ocupa todo el ancho disponible
				eventLayout.setSpacing(false);
				eventLayout.setPadding(false);
				eventLayout.setAlignItems(Alignment.START);
				eventLayout.addClassName("timeline-event");

				// Asignar clase CSS según el estado
				switch (item.getStatus()) {
				case "Urgente":
					eventLayout.addClassName("status-urgente");
					break;
				case "Pendiente":
					eventLayout.addClassName("status-pendiente");
					break;
				case "En curso":
					eventLayout.addClassName("status-en-curso");
					break;
				case "Completado":
					eventLayout.addClassName("status-completado");
					break;
				default:
					eventLayout.addClassName("status-unknown");
					break;
				}

				// Punto de la línea de tiempo (icono de estado + texto)
				Span statusIcon = createStatusIconWithText(item.getStatus());
				statusIcon.addClassName("timeline-status-icon");

				// Detalles del evento
				VerticalLayout eventDetails = new VerticalLayout();
				eventDetails.setPadding(true);
				eventDetails.setSpacing(false);
				eventDetails.addClassName("timeline-details");

				// Manejar 'null' en la fecha
				String dateText = (item.getDate() != null) ? item.getDate().toLocalDate().toString()
						: "Fecha no disponible";
				Span date = new Span(dateText);
				date.addClassName("timeline-date");

				Span title = new Span(item.getTitle());
				title.addClassName("timeline-title");

				Span description = new Span(item.getDescription());
				description.addClassName("timeline-description");

				Button detailsButton = new Button("Ver detalles", new Icon(VaadinIcon.INFO_CIRCLE));
				detailsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				detailsButton.addClickListener(e -> openDetailsDialog(item));

				eventDetails.add(date, title, description, detailsButton);

				// Contenedor para la información de categoría (derecha)
				Div categoryInfo = new Div();
				categoryInfo.addClassName("timeline-category-info");

				categoryManager.getCategoryById(item.getCategory()).ifPresent(category -> {
					try {
						VaadinIcon vaadinIcon = VaadinIcon.valueOf(category.getIcon());
						Icon icon = vaadinIcon.create();
						icon.addClassName("timeline-category-icon");

						Span categoryName = new Span(category.getName());
						categoryName.addClassName("timeline-category-name");

						HorizontalLayout categoryContent = new HorizontalLayout(icon, categoryName);
						categoryContent.addClassName("timeline-category-content");

						categoryInfo.add(categoryContent);

						// Agregar clase específica de la categoría al layout del evento para el color de fondo
						String categoryClass = "category-" + category.getId().toLowerCase();
						eventLayout.addClassName(categoryClass);
					} catch (IllegalArgumentException e) {
						System.err.println("Icono inválido para categoría: " + category.getIcon());
					}
				});

				eventLayout.add(statusIcon, eventDetails, categoryInfo);
				timeline.add(eventLayout);
			});
		}

		// Eliminar la línea de tiempo anterior si existe y añadir la nueva
		container.getChildren().filter(component -> component.hasClassName("clinical-timeline"))
				.forEach(container::remove);
		container.add(timeline);
	}

	/**
	 * Crea un icono representativo del estado clínico.
	 *
	 * @param status El estado clínico.
	 * @return Un Span que contiene el icono correspondiente.
	 */
	private Span createStatusIcon(String status) {
		ClinicalStatus clinicalStatus;
		try {
			clinicalStatus = ClinicalStatus.fromString(status);
		} catch (IllegalArgumentException e) {
			clinicalStatus = null;
			System.err.println("Error al mapear el estado: " + status);
		}

		Icon icon;
		String tooltip;

		if (clinicalStatus != null) {
			switch (clinicalStatus) {
			case URGENTE:
				icon = VaadinIcon.MEGAPHONE.create();
				tooltip = "Urgente";
				break;
			case PENDIENTE:
				icon = VaadinIcon.CLOCK.create();
				tooltip = "Pendiente";
				break;
			case EN_CURSO:
				icon = VaadinIcon.HOURGLASS.create();
				tooltip = "En curso";
				break;
			case COMPLETADO:
				icon = VaadinIcon.CHECK_CIRCLE.create();
				tooltip = "Completado";
				break;
			default:
				icon = VaadinIcon.QUESTION_CIRCLE.create();
				tooltip = "Desconocido";
				break;
			}
		} else {
			icon = VaadinIcon.QUESTION_CIRCLE.create();
			tooltip = "Desconocido";
		}

		Span iconContainer = new Span(icon);
		iconContainer.addClassName("status-icon");
		if (clinicalStatus != null) {
			iconContainer.addClassName("status-" + clinicalStatus.name().toLowerCase());
		} else {
			iconContainer.addClassName("status-unknown");
		}
		iconContainer.getElement().setAttribute("title", tooltip);
		return iconContainer;
	}

	private Span createStatusIconWithText(String status) {
		ClinicalStatus clinicalStatus;
		try {
			clinicalStatus = ClinicalStatus.fromString(status);
		} catch (IllegalArgumentException e) {
			clinicalStatus = null;
			System.err.println("Error al mapear el estado: " + status);
		}

		Icon icon;
		String tooltip;
		String displayName;

		if (clinicalStatus != null) {
			switch (clinicalStatus) {
			case URGENTE:
				icon = VaadinIcon.MEGAPHONE.create();
				tooltip = "Urgente";
				displayName = "Urgente";
				break;
			case PENDIENTE:
				icon = VaadinIcon.CLOCK.create();
				tooltip = "Pendiente";
				displayName = "Pendiente";
				break;
			case EN_CURSO:
				icon = VaadinIcon.HOURGLASS.create();
				tooltip = "En curso";
				displayName = "En curso";
				break;
			case COMPLETADO:
				icon = VaadinIcon.CHECK_CIRCLE.create();
				tooltip = "Completado";
				displayName = "Completado";
				break;
			default:
				icon = VaadinIcon.QUESTION_CIRCLE.create();
				tooltip = "Desconocido";
				displayName = "Desconocido";
				break;
			}
		} else {
			icon = VaadinIcon.QUESTION_CIRCLE.create();
			tooltip = "Desconocido";
			displayName = "Desconocido";
		}

		// Crear el contenedor del icono y el texto
		Span iconContainer = new Span(icon, new Span(displayName));
		iconContainer.addClassName("status-icon-container");

		// Añadir clases según el estado
		if (clinicalStatus != null) {
			iconContainer.addClassName("status-" + clinicalStatus.name().toLowerCase());
		} else {
			iconContainer.addClassName("status-unknown");
		}

		// Añadir tooltip al contenedor completo
		iconContainer.getElement().setAttribute("title", tooltip);

		return iconContainer;
	}

	/**
	 * Asigna una prioridad numérica a cada estado clínico. Menor número indica mayor prioridad.
	 *
	 * @param status El estado clínico.
	 * @return La prioridad numérica del estado.
	 */
	private int getStatusPriority(String status) {
		switch (status) {
		case "Urgente":
			return 0; // Prioridad más alta
		case "Pendiente":
			return 1; // Mayor prioridad
		case "En curso":
			return 2;
		case "Completado":
			return 3; // Menor prioridad
		default:
			return 4; // Para estados no definidos
		}
	}

	/**
	 * Método para abrir el diálogo de detalles de un dato clínico. Permite editar y eliminar el dato clínico seleccionado.
	 *
	 * @param data El dato clínico para mostrar los detalles.
	 */
	public void openDetailsDialog(ClinicalData data) {
		Dialog dialog = new Dialog();
		dialog.setWidth("600px");
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(true);

		// Cabecera del diálogo
		H3 dialogTitle = new H3("Detalles de " + (data.getTitle() != null ? data.getTitle() : "Sin título"));

		// Formulario de detalles
		TextField titleField = new TextField("Título");
		titleField.setValue(data.getTitle() != null ? data.getTitle() : "");
		titleField.setWidthFull();

		TextArea descriptionArea = new TextArea("Descripción");
		descriptionArea.setValue(data.getDescription() != null ? data.getDescription() : "");
		descriptionArea.setWidthFull();
		descriptionArea.setHeight("150px");

		// Campo de fecha
		DatePicker datePicker = new DatePicker("Fecha");
		if (data.getDate() != null) {
			datePicker.setValue(data.getDate().toLocalDate());
		} else {
			datePicker.setValue(LocalDate.now());
		}

		// Campo de estado
		ComboBox<ClinicalStatus> statusCombo = new ComboBox<>("Estado");
		statusCombo.setItems(ClinicalStatus.values());
		statusCombo.setItemLabelGenerator(ClinicalStatus::getDisplayName);
		statusCombo.setValue(
				data.getStatus() != null ? ClinicalStatus.fromString(data.getStatus()) : ClinicalStatus.PENDIENTE);

		// **Nuevo: Campo de selección de categoría**
		ComboBox<Category> categoryCombo = new ComboBox<>("Categoría");
		List<Category> activeCategories = categoryManager.getActiveCategories(); // Obtener categorías activas
		categoryCombo.setItems(activeCategories);
		categoryCombo.setItemLabelGenerator(Category::getName);
		categoryCombo.setPlaceholder("Seleccione una categoría");

		// Preseleccionar la categoría actual
		categoryManager.getCategoryById(data.getCategory()).ifPresent(categoryCombo::setValue);

		// Opcional: Deshabilitar la selección de categoría si no deseas que sea editable
		// categoryCombo.setEnabled(false); // Descomenta esta línea si quieres que la categoría no sea editable

		// Botones de acción
		Button saveButton = new Button("Guardar", event -> {
			// Validaciones básicas
			if (titleField.isEmpty() || descriptionArea.isEmpty() || datePicker.isEmpty() || statusCombo.isEmpty()
					|| categoryCombo.isEmpty()) {
				Notification.show("Todos los campos son obligatorios.", 3000, Notification.Position.MIDDLE)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}

			// Actualizar los datos
			ClinicalStatus selectedStatus = statusCombo.getValue();
			Category selectedCategory = categoryCombo.getValue(); // Obtener la categoría seleccionada

			data.setTitle(titleField.getValue());
			data.setDescription(descriptionArea.getValue());
			data.setStatus(selectedStatus.getDisplayName());
			data.setDate(datePicker.getValue().atStartOfDay());

			if (selectedCategory != null) {
				data.setCategory(selectedCategory.getId()); // Actualizar la categoría
			}

			// Guardar los cambios en el backend
			dataManager.updateClinicalData(data);

			// Refrescar el ítem del paciente
			PatientData currentPatient = dataManager.getCurrentUser();
			if (currentPatient != null) {
				refreshPatientItem(currentPatient);

				// Obtener los datos actualizados según la categoría actual
				List<ClinicalData> updatedData;
				if ("all".equals(currentCategoryId)) {
					updatedData = dataManager.getAllClinicalData(currentPatient.getPhoneNumber());
				} else {
					updatedData = dataManager.getClinicalData(currentPatient.getPhoneNumber(), currentCategoryId);
				}

				// Re-renderizar la línea de tiempo con los datos actualizados
				displayClinicalData(messageList, updatedData, currentCategoryId);
			}

			showMessage("Datos actualizados exitosamente.");

			dialog.close();
		});

		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button deleteButton = new Button("Eliminar", event -> {
			// Confirmación antes de eliminar
			Dialog confirmDialog = new Dialog();
			confirmDialog.setWidth("400px");
			confirmDialog.setCloseOnEsc(false);
			confirmDialog.setCloseOnOutsideClick(false);

			VerticalLayout confirmLayout = new VerticalLayout();
			confirmLayout.setPadding(true);
			confirmLayout.setSpacing(true);
			confirmLayout.add(new Span("¿Estás seguro de que deseas eliminar este dato clínico?"));

			HorizontalLayout confirmButtons = new HorizontalLayout();

			Button confirmDelete = new Button("Eliminar", e -> {
				dataManager.deleteClinicalData(data.getCategory(), data.getTitle());
				showMessage("Dato clínico eliminado.");
				confirmDialog.close();
				dialog.close();

				// Refrescar el ítem del paciente
				PatientData currentPatient = dataManager.getCurrentUser();
				if (currentPatient != null) {
					refreshPatientItem(currentPatient);

					// Recargar los datos clínicos en la línea de tiempo usando la categoría actual
					if (messageList != null && currentCategoryId != null) { // Verificar que currentCategoryId esté definido
						List<ClinicalData> updatedData;
						if ("all".equals(currentCategoryId)) {
							updatedData = dataManager.getAllClinicalData(currentPatient.getPhoneNumber());
						} else {
							updatedData = dataManager.getClinicalData(currentPatient.getPhoneNumber(), currentCategoryId);
						}
						displayClinicalData(messageList, updatedData, currentCategoryId); // Pasar categoryId
					}
				}
			});
			confirmDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
			Button cancelDelete = new Button("Cancelar", e -> confirmDialog.close());

			confirmButtons.add(confirmDelete, cancelDelete);
			confirmLayout.add(confirmButtons);
			confirmDialog.add(confirmLayout);
			confirmDialog.open();
		});

		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

		// Layout de los botones
		HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, deleteButton);
		buttonLayout.setJustifyContentMode(JustifyContentMode.END);

		// Layout principal del diálogo
		VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, titleField, descriptionArea, datePicker,
				categoryCombo, // Añadir el ComboBox de categoría al layout
				statusCombo, buttonLayout);
		dialogLayout.setPadding(true);
		dialogLayout.setSpacing(true);

		dialog.add(dialogLayout);
		dialog.open();
	}

	/**
	 * Método para abrir el diálogo de añadir nuevos datos clínicos.
	 */
	public void openAddClinicalDataDialog() {
		// Verificar si hay un paciente seleccionado
		PatientData selectedPatient = dataManager.getCurrentUser();
		if (selectedPatient == null) {
			showMessageWarning("Seleccione un paciente primero.");
			return;
		}

		Dialog addDialog = new Dialog();
		addDialog.setWidth("600px");
		addDialog.setCloseOnEsc(true);
		addDialog.setCloseOnOutsideClick(true);

		// Cabecera del diálogo
		H3 addDialogTitle = new H3("Añadir Nuevo Dato Clínico para " + selectedPatient.getName());

		// Formulario de ingreso de datos
		ComboBox<Category> categoryCombo = new ComboBox<>("Categoría");

		// Obtener categorías activas desde CategoryManager
		List<Category> activeCategories = categoryManager.getAllCategories().stream().filter(Category::isActive)
				.collect(Collectors.toList());

		categoryCombo.setItems(activeCategories);

		if (!activeCategories.isEmpty()) {
			categoryCombo.setValue(activeCategories.get(0)); // Selecciona la primera categoría por defecto
		}

		// Configurar el generador de etiquetas para mostrar el nombre de la categoría
		categoryCombo.setItemLabelGenerator(Category::getName);

		// Permitir cambiar la categoría
		categoryCombo.setEnabled(true); // Puedes deshabilitarlo si deseas fijar la categoría

		TextField titleField = new TextField("Título");
		titleField.setWidthFull();

		TextArea descriptionArea = new TextArea("Descripción");
		descriptionArea.setWidthFull();
		descriptionArea.setHeight("150px");

		DatePicker datePicker = new DatePicker("Fecha");
		datePicker.setValue(LocalDate.now());

		// Reemplaza el ComboBox de String por ComboBox<ClinicalStatus>
		ComboBox<ClinicalStatus> statusCombo = new ComboBox<>("Estado");
		statusCombo.setItems(ClinicalStatus.values());
		statusCombo.setItemLabelGenerator(ClinicalStatus::getDisplayName);
		statusCombo.setValue(ClinicalStatus.PENDIENTE); // Valor por defecto

		// Componentes para adjuntar archivos
		MemoryBuffer buffer = new MemoryBuffer();
		Upload upload = new Upload(buffer);
		upload.setAcceptedFileTypes("image/*", "application/pdf", "text/*");
		upload.setMaxFiles(5);
		upload.setMaxFileSize(10485760); // 10 MB

		// Botones de acción
		Button addButton = new Button("Añadir", event -> {
			// Validaciones básicas
			if (titleField.isEmpty() || descriptionArea.isEmpty() || datePicker.isEmpty() || statusCombo.isEmpty()) {
				showMessageWarning("Todos los campos son obligatorios.");
				return;
			}

			// Obtener la categoría seleccionada
			Category selectedCategory = categoryCombo.getValue();
			if (selectedCategory == null) {
				showMessageWarning("Seleccione una categoría válida.");
				return;
			}

			// Obtener el estado seleccionado del enum
			ClinicalStatus selectedStatus = statusCombo.getValue();

			// Crear el nuevo dato clínico usando el ID interno de la categoría y el estado del enum
			ClinicalData newData = ClinicalData.builder().category(selectedCategory.getId()) // Usar ID interno
					.title(titleField.getValue()).description(descriptionArea.getValue())
					.status(selectedStatus.getDisplayName()).date(datePicker.getValue().atStartOfDay()).build();

			// Añadir el nuevo dato clínico al dataManager
			dataManager.addClinicalData(selectedPatient.getPhoneNumber(), newData);

			// Refrescar los datos clínicos en la línea de tiempo usando la categoría actual
			if (messageList != null && currentCategoryId != null) { // Verificar que currentCategoryId esté definido
				List<ClinicalData> updatedData;
				if ("all".equals(currentCategoryId)) {
					updatedData = dataManager.getAllClinicalData(selectedPatient.getPhoneNumber());
				} else {
					updatedData = dataManager.getClinicalData(selectedPatient.getPhoneNumber(), currentCategoryId);
				}
				displayClinicalData(messageList, updatedData, currentCategoryId); // Pasar categoryId
			}

			// Actualizar la lista de pacientes para reflejar los nuevos conteos de estados
			refreshPatientItem(selectedPatient);

			showMessage("Nuevo dato clínico añadido exitosamente.");
			addDialog.close();
		});

		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button cancelButton = new Button("Cancelar", e -> addDialog.close());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		// Layout de los botones
		HorizontalLayout addButtonLayout = new HorizontalLayout(addButton, cancelButton);
		addButtonLayout.setJustifyContentMode(JustifyContentMode.END);

		// Layout principal del diálogo
		VerticalLayout addDialogLayout = new VerticalLayout(addDialogTitle, categoryCombo, titleField, descriptionArea,
				datePicker, statusCombo, upload, addButtonLayout);
		addDialogLayout.setPadding(true);
		addDialogLayout.setSpacing(true);

		addDialog.add(addDialogLayout);
		addDialog.open();
	}

	/**
	 * Crea un ítem de lista de paciente.
	 *
	 * @param patient El paciente para el cual crear el ítem.
	 * @return Un HorizontalLayout que representa el ítem del paciente.
	 */
	public HorizontalLayout createPatientListItem(PatientData patient) {
		// Layout principal del item
		HorizontalLayout patientItem = new HorizontalLayout();
		patientItem.addClassName("patient-item");
		patientItem.setWidthFull();
		patientItem.setPadding(false); // Evitar padding redundante
		patientItem.setAlignItems(Alignment.CENTER);

		// Asignar el número de teléfono como ID sin el símbolo "+"
		String phoneNumber = patient.getPhoneNumber().startsWith("+") ? patient.getPhoneNumber().substring(1)
				: patient.getPhoneNumber();
		patientItem.setId(phoneNumber);

		// Avatar o imagen del paciente
		Div avatar = new Div();
		avatar.addClassName("patient-avatar");
		avatar.setText(patient.getName().substring(0, 1).toUpperCase());

		// Información del paciente
		VerticalLayout patientInfo = new VerticalLayout();
		patientInfo.setPadding(false);
		patientInfo.setSpacing(false);
		patientInfo.setFlexGrow(1); // Permite que ocupe el espacio disponible

		// Nombre del paciente
		Span name = new Span(patient.getName());
		name.addClassName("patient-name");

		// Teléfono del paciente
		Span phone = new Span(patient.getPhoneNumber());
		phone.addClassName("patient-phone");

		patientInfo.add(name, phone);

		// Contenedor para los iconos de estado
		HorizontalLayout statusIconsContainer = new HorizontalLayout();
		statusIconsContainer.setSpacing(false); // Manejar espaciado via CSS
		statusIconsContainer.getStyle().set("overflow-x", "auto");
		statusIconsContainer.getStyle().set("white-space", "nowrap");

		statusIconsContainer.setAlignItems(Alignment.CENTER);
		statusIconsContainer.addClassName("status-icons-container");

		// Analizar estados de los datos del paciente
		if (patient.getData() != null && !patient.getData().isEmpty()) {
			// Agrupar por ClinicalStatus en lugar de String
			Map<ClinicalStatus, Long> statusCounts = patient.getData().stream().filter(data -> data.getStatus() != null)
					.map(data -> {
						try {
							return ClinicalStatus.fromString(data.getStatus());
						} catch (IllegalArgumentException e) {
							return null; // Puedes manejar estados desconocidos de otra manera si lo deseas
						}
					}).filter(cs -> cs != null).collect(Collectors.groupingBy(cs -> cs, Collectors.counting()));

			// Crear icono y contador para cada estado existente
			if (statusCounts.containsKey(ClinicalStatus.URGENTE)) {
				Span urgentIcon = createStatusIcon(ClinicalStatus.URGENTE.getDisplayName());
				Span urgentCount = new Span(statusCounts.get(ClinicalStatus.URGENTE).toString());
				urgentCount.addClassName("status-count");
				statusIconsContainer.add(urgentIcon, urgentCount);
			}

			if (statusCounts.containsKey(ClinicalStatus.PENDIENTE)) {
				Span pendingIcon = createStatusIcon(ClinicalStatus.PENDIENTE.getDisplayName());
				Span pendingCount = new Span(statusCounts.get(ClinicalStatus.PENDIENTE).toString());
				pendingCount.addClassName("status-count");
				statusIconsContainer.add(pendingIcon, pendingCount);
			}

			if (statusCounts.containsKey(ClinicalStatus.EN_CURSO)) {
				Span inProgressIcon = createStatusIcon(ClinicalStatus.EN_CURSO.getDisplayName());
				Span inProgressCount = new Span(statusCounts.get(ClinicalStatus.EN_CURSO).toString());
				inProgressCount.addClassName("status-count");
				statusIconsContainer.add(inProgressIcon, inProgressCount);
			}

			if (statusCounts.containsKey(ClinicalStatus.COMPLETADO)) {
				Span completedIcon = createStatusIcon(ClinicalStatus.COMPLETADO.getDisplayName());
				Span completedCount = new Span(statusCounts.get(ClinicalStatus.COMPLETADO).toString());
				completedCount.addClassName("status-count");
				statusIconsContainer.add(completedIcon, completedCount);
			}

		}

		// Crear el botón de añadir datos
		Button addDataButton = new Button(new Icon(VaadinIcon.PLUS));
		addDataButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
		addDataButton.addClassName("add-data-button");
		addDataButton.getElement().setAttribute("title", "Añadir datos clínicos");

		// Click listener para el botón de añadir
		addDataButton.addClickListener(e -> {
			selectPatient(patient);
			highlightSelectedPatient(patientItem);
			openAddClinicalDataDialog();
		});

		// Contenedor para iconos de estado y botón de añadir
		HorizontalLayout actionsContainer = new HorizontalLayout();
		actionsContainer.setSpacing(false); // Manejar espaciado via CSS
		actionsContainer.addClassName("actions-container");
		actionsContainer.getStyle().set("flex-grow", "0"); // Evita que el contenedor se expanda
		actionsContainer.getStyle().set("flex-shrink", "0"); // Evita que el contenedor se encoja

		actionsContainer.add(statusIconsContainer, addDataButton);
		actionsContainer.setAlignItems(Alignment.CENTER);
		actionsContainer.addClassName("actions-container");

		// Añadir todos los componentes al item principal
		patientItem.add(avatar, patientInfo, actionsContainer);
		patientItem.expand(patientInfo); // Permite que patientInfo ocupe el espacio disponible

		// Verificar si este paciente está seleccionado y aplicar la clase
		if (dataManager.getCurrentUser() != null
				&& patient.getPhoneNumber().equals(dataManager.getCurrentUser().getPhoneNumber())) {
			patientItem.addClassName("patient-selected");
		}

		// Manejar selección del paciente
		patientItem.addClickListener(e -> {
			selectPatient(patient);
			highlightSelectedPatient(patientItem);
			if (patientSelectionListener != null) {
				patientSelectionListener.onPatientSelected(patient);
			}
		});

		return patientItem;
	}

	/**
	 * Selecciona un paciente y actualiza el usuario actual en el dataManager.
	 *
	 * @param patient El paciente seleccionado.
	 */
	private void selectPatient(PatientData patient) {
		dataManager.setCurrentUser(patient.getPhoneNumber());
		// Puedes añadir aquí más lógica cuando se selecciona un paciente
	}

	/**
	 * Resalta el ítem del paciente seleccionado y elimina el resaltado de los demás.
	 *
	 * @param selectedItem El ítem seleccionado para resaltar.
	 */
	private void highlightSelectedPatient(HorizontalLayout selectedItem) {
		// Remover highlight de todos los items
		selectedItem.getParent().get().getChildren().forEach(component -> component.removeClassName("patient-selected"));

		// Añadir highlight al item seleccionado
		selectedItem.addClassName("patient-selected");
	}

	/**
	 * Método para refrescar el ítem del paciente en la lista de pacientes.
	 *
	 * @param patient El paciente cuyos datos han sido actualizados.
	 */
	public void refreshPatientItem(PatientData patient) {
		if (patientListLayout != null && patient.getPhoneNumber() != null) {
			// Obtener el número de teléfono sin el prefijo "+"
			String phoneNumber = patient.getPhoneNumber().startsWith("+") ? patient.getPhoneNumber().substring(1)
					: patient.getPhoneNumber();
			System.out.println("Refrescando ítem del paciente con teléfono: " + phoneNumber);

			// Buscar el componente con el ID correspondiente
			patientListLayout.getChildren()
					.filter(component -> component.getId().isPresent() && component.getId().get().equals(phoneNumber))
					.findFirst().ifPresent(component -> {
						System.out.println("Encontrado componente a reemplazar: " + component);
						// Crear un nuevo ítem actualizado
						HorizontalLayout newPatientItem = createPatientListItem(patient);
						System.out.println("Creando nuevo ítem: " + newPatientItem);
						// Reemplazar el ítem antiguo con el nuevo
						patientListLayout.replace(component, newPatientItem);
					});
		} else {
			System.err.println("patientListLayout es null o el número de teléfono es inválido.");
		}
	}

	public void clearFilters() {
	    if (filterLayout != null) {
	        searchField.clear();
	        statusFilter.setValue(STATUS_ALL_NAME); // Establecer "Todos" localizado
	        currentSearchTerm = "";
	        currentStatusFilter = STATUS_ALL_ID; // "all"
	    }
	}

	public void clearPreviousData() {
		this.allData = new ArrayList<>();
		if (messageList != null) {
			// Mantener los filtros, solo limpiar el contenido
			messageList.getChildren().filter(component -> component.hasClassName("clinical-timeline"))
					.forEach(messageList::remove);
		}
	}

	private void showMessage(String message) {
		MyNotification.show(message, Notification.Position.MIDDLE, NotificationVariant.LUMO_SUCCESS, 3000);
	}

	private void showMessageWarning(String message) {
		MyNotification.showWarning(message, Notification.Position.MIDDLE, NotificationVariant.LUMO_WARNING, 3000);
	}

}
