package es.televoip.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaadin.flow.component.icon.VaadinIcon;

import es.televoip.model.entities.CategoryConfig;
import es.televoip.model.entities.SubCategory;
import es.televoip.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // @Autowired - No es necesario Autowired pero si se aplica
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<CategoryConfig> defaultCategories = initializeDefaultCategoriesList();
            categoryRepository.saveAll(defaultCategories);
            log.info("Categorías predeterminadas inicializadas.");
        }
    }

    private List<CategoryConfig> initializeDefaultCategoriesList() {
        // Aquí debes crear y devolver la lista de categorías predeterminadas
        /// 1. Citas y Agenda
   	 CategoryConfig appointments = CategoryConfig.builder()
             .id("appointments")
             .name("Citas")
             .icon(VaadinIcon.CALENDAR.name())
             .isActive(true)
             .subCategories(Arrays.asList(
                 SubCategory.builder()
                     .id("view")
                     .name("Ver/Solicitar")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(1)
                     .build(),
                 SubCategory.builder()
                     .id("modify")
                     .name("Modificar/Cancelar")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(2)
                     .build(),
                 SubCategory.builder()
                     .id("reminders")
                     .name("Recordatorios")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(3)
                     .build()
             ))
             .displayOrder(1)
             .build();

         // 2. Tratamientos
         CategoryConfig treatments = CategoryConfig.builder()
             .id("treatments")
             .name("Tratamientos")
             .icon(VaadinIcon.DOCTOR.name())
             .isActive(true)
             .subCategories(Arrays.asList(
                 SubCategory.builder()
                     .id("active")
                     .name("Activos")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(1)
                     .build(),
                 SubCategory.builder()
                     .id("follow")
                     .name("Seguimiento")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(2)
                     .build(),
                 SubCategory.builder()
                     .id("history")
                     .name("Historial")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(3)
                     .build()
             ))
             .displayOrder(2)
             .build();

         // 3. Pagos
         CategoryConfig payments = CategoryConfig.builder()
             .id("payments")
             .name("Pagos")
             .icon(VaadinIcon.EURO.name())
             .isActive(true)
             .subCategories(Arrays.asList(
                 SubCategory.builder()
                     .id("pending")
                     .name("Facturas Pendientes")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(1)
                     .build(),
                 SubCategory.builder()
                     .id("quotes")
                     .name("Presupuestos")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(2)
                     .build(),
                 SubCategory.builder()
                     .id("history")
                     .name("Historial")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(3)
                     .build()
             ))
             .displayOrder(3)
             .build();
         
         // 4. Documentos
         CategoryConfig documents = CategoryConfig.builder()
             .id("documents")
             .name("Documentos")
             .icon(VaadinIcon.FILE_TEXT.name())
             .isActive(true)
             .subCategories(Arrays.asList(
                 SubCategory.builder()
                     .id("upload")
                     .name("Subir")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(1)
                     .build(),
                 SubCategory.builder()
                     .id("view")
                     .name("Ver")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(2)
                     .build(),
                 SubCategory.builder()
                     .id("share")
                     .name("Compartir")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(3)
                     .build()
             ))
             .displayOrder(4)
             .build();
         
         // 5. Comunicaciones
         CategoryConfig communications = CategoryConfig.builder()
             .id("communications")
             .name("Comunicaciones")
             .icon(VaadinIcon.COMMENT.name())
             .isActive(true)
             .subCategories(Arrays.asList(
                 SubCategory.builder()
                     .id("messages")
                     .name("Mensajes")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(1)
                     .build(),
                 SubCategory.builder()
                     .id("notifications")
                     .name("Notificaciones")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(2)
                     .build(),
                 SubCategory.builder()
                     .id("feedback")
                     .name("Retroalimentación")
                     .isActive(true)
                     .isRequired(false)
                     .displayOrder(3)
                     .build()
             ))
             .displayOrder(5)
             .build();

        return List.of(appointments, treatments, payments, documents,  communications);
    }

    /**
     * Obtiene todas las categorías, ordenadas según el displayOrder.
     * 
     * @return Lista de todas las categorías.
     */
    @Transactional(readOnly = true)
    public List<CategoryConfig> getAllCategories() {
        return categoryRepository.findAllWithSubCategories().stream()
            .sorted(Comparator.comparingInt(CategoryConfig::getDisplayOrder))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las categorías que están activas.
     *
     * @return Lista de categorías activas.
     */
    public List<CategoryConfig> getActiveCategories() {
        return getAllCategories().stream()
            .filter(CategoryConfig::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Añade una nueva categoría.
     * 
     * @param category La categoría a añadir.
     */
    public void addCategory(CategoryConfig category) {
        if (categoryRepository.existsById(category.getId())) {
            log.warn("La categoría con ID '{}' ya existe. Use 'updateCategory' para actualizarla.", category.getId());
            return;
        }
        categoryRepository.save(category);
        log.info("Categoría añadida: {}", category.getName());
    }

    /**
     * Elimina una categoría existente.
     * 
     * @param categoryId ID de la categoría a eliminar.
     */
    public void deleteCategory(String categoryId) {
        if (categoryRepository.count() <= 1) {
            log.warn("No se puede eliminar la única categoría existente.");
            return;
        }

        if (categoryRepository.existsById(categoryId)) {
            categoryRepository.deleteById(categoryId);
            reorderCategories();
            log.info("Categoría eliminada: {}", categoryId);
        } else {
            log.warn("Intento de eliminar una categoría que no existe: {}", categoryId);
        }
    }

    /**
     * Actualiza una categoría existente.
     * 
     * @param updatedCategory La categoría con los nuevos datos.
     */
    @Transactional(readOnly = true)
    public void updateCategory(CategoryConfig updatedCategory) {
        if (categoryRepository.existsById(updatedCategory.getId())) {
            categoryRepository.save(updatedCategory);
            log.info("Categoría actualizada: {}", updatedCategory.getName());
        } else {
            log.warn("Intento de actualizar una categoría que no existe: {}", updatedCategory.getId());
        }
    }

    /**
     * Obtiene una categoría por su ID.
     * 
     * @param categoryId ID de la categoría.
     * @return La categoría correspondiente o null si no se encuentra.
     */
    public Optional<CategoryConfig> getCategoryById(String categoryId) {
       return categoryRepository.findByIdWithSubCategories(categoryId);
    }

    /**
     * Mueve una categoría hacia arriba o hacia abajo en el orden de visualización.
     * 
     * @param categoryId ID de la categoría a mover.
     * @param moveUp     Si es true, mueve la categoría hacia arriba; si es false, hacia abajo.
     */
    public void moveCategory(String categoryId, boolean moveUp) {
        List<CategoryConfig> categories = getAllCategories();
        Optional<CategoryConfig> optionalCategory = categoryRepository.findById(categoryId);

        if (optionalCategory.isEmpty()) {
            log.warn("Categoría no encontrada: {}", categoryId);
            return;
        }

        CategoryConfig categoryToMove = optionalCategory.get();
        int currentOrder = categoryToMove.getDisplayOrder();
        int newOrder = moveUp ? currentOrder - 1 : currentOrder + 1;

        // Validar límites
        if (newOrder < 1 || newOrder > categories.size()) {
            log.warn("Movimiento fuera de límites para la categoría: {}", categoryId);
            return;
        }

        // Encontrar la categoría con la que intercambiar
        Optional<CategoryConfig> optionalCategoryToSwap = categories.stream()
            .filter(c -> c.getDisplayOrder() == newOrder)
            .findFirst();

        if (optionalCategoryToSwap.isPresent()) {
            CategoryConfig categoryToSwap = optionalCategoryToSwap.get();

            // Intercambiar órdenes
            categoryToMove.setDisplayOrder(newOrder);
            categoryToSwap.setDisplayOrder(currentOrder);

            // Guardar los cambios
            categoryRepository.save(categoryToMove);
            categoryRepository.save(categoryToSwap);

            log.info("Categoría '{}' movida a posición {}", categoryToMove.getName(), newOrder);
            log.info("Categoría '{}' movida a posición {}", categoryToSwap.getName(), currentOrder);
        } else {
            log.warn("No se encontró una categoría para intercambiar en la posición {}", newOrder);
        }
    }

    /**
     * Reordena todas las categorías según el displayOrder.
     */
    public void reorderCategories() {
        List<CategoryConfig> orderedCategories = getAllCategories();
        int order = 1;
        for (CategoryConfig category : orderedCategories) {
            category.setDisplayOrder(order++);
            categoryRepository.save(category);
        }
        log.info("Categorías reordenadas.");
    }
    
}
