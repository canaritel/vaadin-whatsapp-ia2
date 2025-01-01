package es.televoip.views.clinica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import es.televoip.model.entities.Category;
import es.televoip.model.entities.SubCategory;
import es.televoip.service.CategoryService;
import es.televoip.util.I18nUtil;
import es.televoip.util.MyNotification;
import es.televoip.views.MainLayout;

//@org.springframework.stereotype.Component
@Route(value = "clinic-admin", layout = MainLayout.class) // Usa MainLayout como diseño principal
public class ClinicalAdminView extends VerticalLayout { // No implementar Translatable

    private static final long serialVersionUID = 1L;

    private final CategoryService categoryManager;
    private final I18nUtil i18nUtil; // Inyectar I18nUtil
    private final Grid<Category> categoriesGrid;

    public ClinicalAdminView(CategoryService categoryManager, I18nUtil i18nUtil) {
        this.categoryManager = categoryManager;
        this.i18nUtil = i18nUtil;
        this.categoriesGrid = new Grid<>(Category.class);

        // Hacer que el layout principal ocupe toda la pantalla
        setSizeFull();

        setupLayout();
    }

    private void setupLayout() {
        // Header con botón de nueva categoría
        H2 title = new H2(i18nUtil.get("dialog.newCategory.title"));
        title.getStyle().set("margin", "0"); // Eliminar márgenes predeterminados

        Button newCategoryButton = new Button(i18nUtil.get("button.newCategory.label"), VaadinIcon.PLUS.create());
        newCategoryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Estilizar el botón como primario
        newCategoryButton.getStyle().set("margin-left", "auto"); // Empujar el botón al final
        newCategoryButton.addClickListener(e -> openNewCategoryDialog());

        // Layout horizontal para título y botón
        HorizontalLayout header = new HorizontalLayout(title, newCategoryButton);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true); // Añadir padding
        header.setSpacing(true); // Añadir espaciado entre componentes

        add(header);

        // Grid de categorías
        setupCategoriesGrid();
        add(categoriesGrid);
    }

    /*
    private void setupCategoriesGrid() {
        categoriesGrid.removeAllColumns();

        // Columna de categoría
        categoriesGrid.addColumn(Category::getName)
            .setHeader(i18nUtil.get("field.name.label"))
            .setWidth("200px")
            .setFlexGrow(1);

        // Columna de estado
        categoriesGrid.addColumn(new ComponentRenderer<>(category -> {
            Checkbox activeBox = new Checkbox();
            activeBox.setValue(category.isActive());
            activeBox.setEnabled(false);
            return activeBox;
        }))
        .setHeader(i18nUtil.get("field.active.label"))
        .setWidth("110px")
        .setFlexGrow(0);

        // Columna de orden
        categoriesGrid.addColumn(Category::getDisplayOrder)
            .setHeader(i18nUtil.get("field.order.label"))
            .setWidth("90px")
            .setFlexGrow(0);

        // Nueva columna para flechas de ordenamiento
        categoriesGrid.addColumn(new ComponentRenderer<>(this::createOrderButtons))
            .setHeader(i18nUtil.get("dialog.moveCategory.header"))
            .setWidth("100px")
            .setFlexGrow(0);

        // Columna de acciones con botones de editar y eliminar
        categoriesGrid.addColumn(new ComponentRenderer<>(category -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editButton = new Button(i18nUtil.get("button.configure.label"), VaadinIcon.COG.create());
            editButton.addClickListener(e -> openCategoryEditor(category));
            editButton.getElement().setAttribute("title", i18nUtil.get("tooltip.configureCategory"));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> confirmDelete(category));
            deleteButton.getElement().setAttribute("title", i18nUtil.get("tooltip.deleteCategory"));

            actions.add(editButton, deleteButton);
            return actions;
        }))
        .setHeader(i18nUtil.get("dialog.actions.header"))
        .setWidth("200px")
        .setFlexGrow(0);

        categoriesGrid.setItems(categoryManager.getAllCategories());
    }
    */
    
    private void setupCategoriesGrid() {
       categoriesGrid.removeAllColumns();

       // Columna de categoría con ícono
       categoriesGrid.addColumn(new ComponentRenderer<>(category -> {
           HorizontalLayout layout = new HorizontalLayout();
           layout.setAlignItems(Alignment.CENTER);
           layout.setSpacing(true);
           
           // Crear y añadir el ícono
           try {
               VaadinIcon vaadinIcon = VaadinIcon.valueOf(category.getIcon());
               Icon icon = vaadinIcon.create();
               icon.setSize("16px");
               layout.add(icon);
           } catch (IllegalArgumentException e) {
               Icon defaultIcon = VaadinIcon.QUESTION_CIRCLE.create();
               defaultIcon.setSize("16px");
               layout.add(defaultIcon);
           }
           
           Span name = new Span(category.getName());
           layout.add(name);
           
           return layout;
       }))
       .setHeader(i18nUtil.get("field.name.label"))
       .setWidth("200px")
       .setFlexGrow(1);

       // Columna de estado
       categoriesGrid.addColumn(new ComponentRenderer<>(category -> {
           Checkbox activeBox = new Checkbox();
           activeBox.setValue(category.isActive());
           activeBox.setEnabled(false);
           return activeBox;
       }))
       .setHeader(i18nUtil.get("field.active.label"))
       .setWidth("110px")
       .setFlexGrow(0);

       // Columna de orden
       categoriesGrid.addColumn(Category::getDisplayOrder)
           .setHeader(i18nUtil.get("field.order.label"))
           .setWidth("90px")
           .setFlexGrow(0);

       // Nueva columna para flechas de ordenamiento
       categoriesGrid.addColumn(new ComponentRenderer<>(this::createOrderButtons))
           .setHeader(i18nUtil.get("dialog.moveCategory.header"))
           .setWidth("100px")
           .setFlexGrow(0);

       // Columna de acciones con botones de editar y eliminar
       categoriesGrid.addColumn(new ComponentRenderer<>(category -> {
           HorizontalLayout actions = new HorizontalLayout();
           actions.setSpacing(true);

           Button editButton = new Button(i18nUtil.get("button.configure.label"), VaadinIcon.COG.create());
           editButton.addClickListener(e -> openCategoryEditor(category));
           editButton.getElement().setAttribute("title", i18nUtil.get("tooltip.configureCategory"));

           Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
           deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
           deleteButton.addClickListener(e -> confirmDelete(category));
           deleteButton.getElement().setAttribute("title", i18nUtil.get("tooltip.deleteCategory"));

           actions.add(editButton, deleteButton);
           return actions;
       }))
       .setHeader(i18nUtil.get("dialog.actions.header"))
       .setWidth("200px")
       .setFlexGrow(0);

       categoriesGrid.setItems(categoryManager.getAllCategories());
    }

    private void openNewCategoryDialog() {
       Dialog dialog = new Dialog();
       dialog.setHeaderTitle(i18nUtil.get("dialog.newCategory.title"));

       // Campos del formulario
       TextField nameField = new TextField(i18nUtil.get("field.name.label"));
       nameField.setRequired(true);
       nameField.setWidthFull();

       ComboBox<VaadinIcon> iconCombo = new ComboBox<>(i18nUtil.get("field.icon.label"));
       iconCombo.setItems(VaadinIcon.values());
       iconCombo.setRenderer(new ComponentRenderer<>(icon -> {
           HorizontalLayout layout = new HorizontalLayout();
           layout.setAlignItems(Alignment.CENTER);
           layout.add(icon.create(), new Span(convertEnumNameToDisplayName(icon.name())));
           return layout;
       }));
       iconCombo.setPlaceholder(i18nUtil.get("field.icon.placeholder")); // Asegúrate de tener esta clave en tus archivos de propiedades
       iconCombo.setRequired(true);
       // No establecer un valor por defecto para forzar al usuario a seleccionar

       Checkbox activeField = new Checkbox(i18nUtil.get("field.active.label"));
       activeField.setValue(false); // Establecer como desactivado por defecto

       // Botones
       Button saveButton = new Button(i18nUtil.get("button.save.label"), e -> {
           if (nameField.getValue().trim().isEmpty() || iconCombo.getValue() == null) {
               nameField.setInvalid(true);
               iconCombo.setInvalid(true);
               // Opcional: Mostrar un mensaje de error específico
               MyNotification.showWarning(
                   i18nUtil.get("notification.iconRequired"),
                   Notification.Position.MIDDLE,
                   NotificationVariant.LUMO_ERROR,
                   3000
               );
               return;
           }

           Category newCategory = Category.builder()
               .id(UUID.randomUUID().toString()) // Usar UUID para IDs únicos
               .name(nameField.getValue().trim())
               .icon(iconCombo.getValue().name()) // Asignar el icono seleccionado
               .isActive(activeField.getValue())
               .displayOrder(categoryManager.getAllCategories().size() + 1)
               .subCategories(new ArrayList<>())
               .build();

           categoryManager.addCategory(newCategory);
           refreshGrid();
           dialog.close();
           MyNotification.show(
               i18nUtil.get("message.categoryAdded"),
               Notification.Position.MIDDLE,
               NotificationVariant.LUMO_SUCCESS,
               3000
           );
       });

       Button cancelButton = new Button(i18nUtil.get("button.cancel.label"), e -> dialog.close());

       // Layout
       VerticalLayout layout = new VerticalLayout(nameField, iconCombo, activeField);
       layout.setSpacing(true);
       layout.setPadding(true);

       HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
       buttons.setSpacing(true);

       dialog.add(layout, buttons);
       dialog.open();
    }
    
    private String convertEnumNameToDisplayName(String enumName) {
       return Arrays.stream(enumName.split("_"))
                    .map(word -> word.substring(0, 1) + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
    }

    private void confirmDelete(Category category) {
        // Validaciones previas
        if (category.getDisplayOrder() == 1) {
            MyNotification.showWarning(
                i18nUtil.get("message.cannotDeleteMainCategory"),
                Notification.Position.MIDDLE,
                NotificationVariant.LUMO_ERROR,
                3000
            );
            return;
        }

        // Nueva validación: verificar si la categoría está activa
        if (category.isActive()) {
            MyNotification.showWarning(
                i18nUtil.get("message.cannotDeleteActiveCategory"),
                Notification.Position.MIDDLE,
                NotificationVariant.LUMO_WARNING,
                3000
            );
            return;
        }

        // Diálogo de confirmación
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(i18nUtil.get("dialog.confirmDelete.title"));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Usar formato para insertar el nombre de la categoría
        Span message = new Span(i18nUtil.getFormatted("dialog.confirmDelete.message", category.getName()));
        content.add(new H3(message.getText()));

        HorizontalLayout buttonsLayout = new HorizontalLayout();

        Button cancelButton = new Button(i18nUtil.get("button.cancel.label"), e -> dialog.close());

        Button deleteButton = new Button(i18nUtil.get("button.delete.label"), e -> {
            categoryManager.deleteCategory(category.getId());
            refreshGrid();
            dialog.close();
            MyNotification.show(
                i18nUtil.get("message.categoryDeleted"),
                Notification.Position.MIDDLE,
                NotificationVariant.LUMO_SUCCESS,
                3000
            );
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        buttonsLayout.add(cancelButton, deleteButton);
        buttonsLayout.setSpacing(true);

        content.add(buttonsLayout);
        dialog.add(content);
        dialog.open();
    }

    private Component createOrderButtons(Category category) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);

        Button upButton = new Button(new Icon(VaadinIcon.ARROW_UP));
        Button downButton = new Button(new Icon(VaadinIcon.ARROW_DOWN));

        upButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        downButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        upButton.addClickListener(e -> moveCategory(category, true));
        downButton.addClickListener(e -> moveCategory(category, false));

        // Deshabilitar botones en los límites
        List<Category> allCategories = categoryManager.getAllCategories();
        upButton.setEnabled(category.getDisplayOrder() > 1);
        downButton.setEnabled(category.getDisplayOrder() < allCategories.size());

        buttonLayout.add(upButton, downButton);
        return buttonLayout;
    }

    private void moveCategory(Category category, boolean moveUp) {
        categoryManager.moveCategory(category.getId(), moveUp);
        refreshGrid();
    }

    private void openCategoryEditor(Category category) {
       // Crear una copia temporal de la categoría original
       Category tempCategory = category.toBuilder()
           .subCategories(new ArrayList<>(category.getSubCategories()))
           .build();

       Dialog dialog = new Dialog();
       String dialogTitleText = i18nUtil.getFormatted("dialog.editCategory.title", category.getName());
       dialog.setHeaderTitle(dialogTitleText);
       dialog.setWidth("600px");
       dialog.setModal(true);
       dialog.setCloseOnOutsideClick(false); // Deshabilitar cierre al hacer clic fuera

       // Campo para el nombre de la categoría
       TextField nameField = new TextField(i18nUtil.get("field.name.label"), tempCategory.getName());
       nameField.setRequired(true);
       nameField.setValue(category.getName()); // sobreescribimos el nombre categoria

       // Campo para seleccionar el icono
       ComboBox<VaadinIcon> iconCombo = new ComboBox<>(i18nUtil.get("field.icon.label"));
       iconCombo.setItems(VaadinIcon.values());
       iconCombo.setRenderer(new ComponentRenderer<>(icon -> {
           HorizontalLayout layout = new HorizontalLayout();
           layout.setAlignItems(Alignment.CENTER);
           layout.add(icon.create(), new Span(convertEnumNameToDisplayName(icon.name())));
           return layout;
       }));
       iconCombo.setPlaceholder(i18nUtil.get("field.icon.placeholder")); // Asegúrate de tener esta clave en tus archivos de propiedades
       iconCombo.setRequired(true);

       // Seleccionar el icono actual de la categoría
       try {
           iconCombo.setValue(VaadinIcon.valueOf(tempCategory.getIcon()));
       } catch (IllegalArgumentException e) {
           System.err.println("Icono '" + tempCategory.getIcon() + "' inválido para la categoría '" + category.getName() + "'. Usando icono por defecto.");
           iconCombo.setValue(VaadinIcon.QUESTION_CIRCLE); // Icono por defecto
       }

       // Estado general de la categoría
       Checkbox activeCheckbox = new Checkbox(i18nUtil.get("field.active.label"), tempCategory.isActive());
       activeCheckbox.addValueChangeListener(e -> tempCategory.setActive(e.getValue()));

       // Subcategorías
       H2 subcategoriesTitle = new H2(i18nUtil.get("dialog.subCategories.title"));

       // Grid para mostrar subcategorías
       Grid<SubCategory> subCategoriesGrid = new Grid<>(SubCategory.class, false);
       subCategoriesGrid.addColumn(SubCategory::getName)
           .setHeader(i18nUtil.get("field.name.label"))
           .setWidth("200px")
           .setFlexGrow(1);
       subCategoriesGrid.addColumn(new ComponentRenderer<>(sub -> {
           Checkbox activeSub = new Checkbox();
           activeSub.setValue(sub.isActive());
           activeSub.setEnabled(false); // Deshabilitar edición directa
           return activeSub;
       }))
       .setHeader(i18nUtil.get("field.active.label"))
       .setWidth("100px")
       .setFlexGrow(0);

       // Botones de acción para cada subcategoría
       subCategoriesGrid.addColumn(new ComponentRenderer<>(sub -> {
           HorizontalLayout actions = new HorizontalLayout();
           actions.setSpacing(true);

           Button editSubButton = new Button(new Icon(VaadinIcon.EDIT));
           editSubButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
           editSubButton.addClickListener(e -> openSubCategoryEditor(dialog, subCategoriesGrid, tempCategory, sub));
           editSubButton.getElement().setAttribute("title", i18nUtil.get("tooltip.editSubCategory"));

           Button deleteSubButton = new Button(new Icon(VaadinIcon.TRASH));
           deleteSubButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
           deleteSubButton.addClickListener(e -> {
               tempCategory.getSubCategories().remove(sub);
               subCategoriesGrid.setItems(tempCategory.getSubCategories());
               MyNotification.show(
                   i18nUtil.get("message.subCategoryDeleted"),
                   Notification.Position.MIDDLE,
                   NotificationVariant.LUMO_SUCCESS,
                   3000
               );
           });
           deleteSubButton.getElement().setAttribute("title", i18nUtil.get("tooltip.deleteSubCategory"));

           actions.add(editSubButton, deleteSubButton);
           return actions;
       }))
       .setHeader(i18nUtil.get("dialog.actions.header"))
       .setWidth("150px")
       .setFlexGrow(0);

       subCategoriesGrid.setItems(tempCategory.getSubCategories());

       // Botón para agregar una nueva subcategoría
       Button addSubCategoryButton = new Button(i18nUtil.get("button.addSubCategory.label"), VaadinIcon.PLUS.create());
       addSubCategoryButton.addClickListener(e -> openSubCategoryEditor(dialog, subCategoriesGrid, tempCategory, null));

       // Layout de subcategorías
       VerticalLayout subcategoriesLayout = new VerticalLayout(subcategoriesTitle, subCategoriesGrid, addSubCategoryButton);
       subcategoriesLayout.setSpacing(true);
       subcategoriesLayout.setPadding(false);

       // Botón para guardar cambios
       Button saveButton = new Button(i18nUtil.get("button.save.label"), event -> {
           if (nameField.getValue().trim().isEmpty() || iconCombo.getValue() == null) {
               nameField.setInvalid(true);
               iconCombo.setInvalid(true);
               // Opcional: Mostrar un mensaje de error específico
               MyNotification.showWarning(
                   i18nUtil.get("notification.iconRequired"),
                   Notification.Position.MIDDLE,
                   NotificationVariant.LUMO_ERROR,
                   3000
               );
               return;
           }
           // Actualizar el objeto temporal con el valor del campo de texto y el icono
           tempCategory.setName(nameField.getValue().trim());
           tempCategory.setIcon(iconCombo.getValue().name());

           // Aplicar los cambios del objeto temporal al objeto original
           category.setName(tempCategory.getName());
           category.setIcon(tempCategory.getIcon());
           category.setActive(tempCategory.isActive());
           category.setSubCategories(tempCategory.getSubCategories());

           // Actualizar en el CategoryManager
           categoryManager.updateCategory(category);

           // Refrescar el Grid para mostrar los cambios
           refreshGrid();

           // Cerrar el diálogo
           dialog.close();
           MyNotification.show(
               i18nUtil.get("message.categoryUpdated"),
               Notification.Position.MIDDLE,
               NotificationVariant.LUMO_SUCCESS,
               3000
           );
       });

       // Botón para cerrar sin guardar
       Button closeButton = new Button(i18nUtil.get("button.cancel.label"), event -> dialog.close());

       // Layout de los botones
       HorizontalLayout buttons = new HorizontalLayout(saveButton, closeButton);
       buttons.setSpacing(true);

       // Layout principal del diálogo
       VerticalLayout dialogLayout = new VerticalLayout(nameField, iconCombo, activeCheckbox, subcategoriesLayout, buttons);
       dialogLayout.setSpacing(true);
       dialogLayout.setPadding(true);

       dialog.add(dialogLayout);
       dialog.open();
    }

    // Método para abrir el editor de subcategorías
    private void openSubCategoryEditor(Dialog parentDialog, Grid<SubCategory> grid, Category category, SubCategory subCategory) {
        boolean isEdit = subCategory != null;

        // Crear una copia temporal de la subcategoría si es edición
        SubCategory tempSubCategory = isEdit ? subCategory.toBuilder().build() : null;

        Dialog subDialog = new Dialog();
        String subDialogTitle = isEdit ? i18nUtil.get("dialog.editSubCategory.title") : i18nUtil.get("dialog.newSubCategory.title");
        subDialog.setHeaderTitle(subDialogTitle);
        subDialog.setWidth("400px");

        TextField subNameField = new TextField(i18nUtil.get("field.name.label"), isEdit ? tempSubCategory.getName() : "");
        subNameField.setRequired(true);
        subNameField.setValue(isEdit ? subCategory.getName() : ""); // sobreescribimos el nombre subcategoria

        Checkbox subActiveCheckbox = new Checkbox(i18nUtil.get("field.active.label"), isEdit ? tempSubCategory.isActive() : true);

        Button saveSubButton = new Button(i18nUtil.get("button.save.label"), event -> {
            if (subNameField.getValue().trim().isEmpty()) {
                subNameField.setInvalid(true);
                return;
            }
            if (isEdit) {
                // Actualizar el objeto temporal
                tempSubCategory.setName(subNameField.getValue().trim());
                tempSubCategory.setActive(subActiveCheckbox.getValue());

                // Aplicar los cambios al objeto original
                subCategory.setName(tempSubCategory.getName());
                subCategory.setActive(tempSubCategory.isActive());
            } else {
                // Crear una nueva subcategoría
                SubCategory newSub = SubCategory.builder()
                    .id(UUID.randomUUID().toString()) // Usar UUID para IDs únicos
                    .name(subNameField.getValue().trim())
                    .isActive(subActiveCheckbox.getValue())
                    .build();
                category.getSubCategories().add(newSub);
            }
            // Refrescar el Grid de subcategorías
            grid.setItems(category.getSubCategories());
            MyNotification.show(
                isEdit ? i18nUtil.get("message.subCategoryUpdated") : i18nUtil.get("message.subCategoryAdded"),
                Notification.Position.MIDDLE,
                NotificationVariant.LUMO_SUCCESS,
                3000
            );
            subDialog.close();
        });

        Button cancelSubButton = new Button(i18nUtil.get("button.cancel.label"), event -> subDialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveSubButton, cancelSubButton);
        buttonsLayout.setSpacing(true);

        VerticalLayout subLayout = new VerticalLayout(subNameField, subActiveCheckbox, buttonsLayout);
        subLayout.setSpacing(true);
        subLayout.setPadding(true);

        subDialog.add(subLayout);
        subDialog.open();
    }

    private void refreshGrid() {
        // Volver a establecer los datos en el Grid
        categoriesGrid.setItems(categoryManager.getAllCategories());
    }
}
