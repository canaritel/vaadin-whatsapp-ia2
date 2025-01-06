package es.televoip.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import es.televoip.model.User;
import es.televoip.views.clinica.ClinicalCategoryView;
import es.televoip.views.clinica.PatientCreationView;
import es.televoip.views.clinica.PatientDataView;
import es.televoip.views.login.LoginView;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
//@Push
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private static final long serialVersionUID = 1L;
    private H1 viewTitle;
    private DrawerToggle toggle;
    private SideNav nav;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
        setDrawerOpened(false);
        addThemeVariants();
    }

    private void addHeaderContent() {
        toggle = new DrawerToggle();
        toggle.setAriaLabel("Abrir/Cerrar menú");

        viewTitle = new H1("Panel Clínico");
        viewTitle.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.NONE
        );
        viewTitle.getStyle().set("margin-left", "1em");

        // Crear barra de herramientas en el header
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setAlignItems(Alignment.CENTER);

        // Añadir elementos a la barra de herramientas
        Button notificationsBtn = new Button(LineAwesomeIcon.BELL_SOLID.create());
        notificationsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Avatar userAvatar = new Avatar();
        String userNickname = (String) VaadinSession.getCurrent().getAttribute("nickname");
        userAvatar.setName(userNickname != null ? userNickname : "Usuario");

        toolbar.add(notificationsBtn, userAvatar);

        // Añadir todos los elementos al navbar
        addToNavbar(true, toggle, viewTitle, toolbar);
    }

    private void addDrawerContent() {
       Header header = createDrawerHeader();
       Scroller scroller = new Scroller(createNavigation());
       scroller.addClassName("app-nav-scroller");
       
       // Ajustar el ancho del contenedor de navegación
       //scroller.setWidth("300px");
       nav.setWidth("100%");  // El nav tomará el ancho completo del scroller
       
       Footer footer = createDrawerFooter();
       //footer.setWidth("300px");

       addToDrawer(header, scroller, footer);
   }

    private Header createDrawerHeader() {
        Header header = new Header();
        header.addClassName("app-nav-header");
        
        // Logo o título de la aplicación
        H2 appName = new H2("Clínica Manager");
        appName.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.NONE
        );

        Span version = new Span("v1.0.0");
        version.addClassNames(
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.XSMALL
        );

        VerticalLayout headerContent = new VerticalLayout(appName, version);
        headerContent.setPadding(true);
        headerContent.setSpacing(false);
        
        header.add(headerContent);
        return header;
    }

    private SideNav createNavigation() {
        nav = new SideNav();
        nav.addClassName("app-nav");

        // SECCIÓN: PANEL PRINCIPAL
        SideNavItem dashboardSection = createSection(
            "Panel Principal",
            LineAwesomeIcon.COLUMNS_SOLID,
            null
        );

        // SECCIÓN: GESTIÓN CLÍNICA
        SideNavItem clinicSection = createSection(
            "Gestión Clínica",
            LineAwesomeIcon.HOSPITAL_SOLID,
            null
        );
        clinicSection.addItem(createNavItem("Pacientes", LineAwesomeIcon.USERS_SOLID, PatientCreationView.class));
        clinicSection.addItem(createNavItem("Calendario", LineAwesomeIcon.CALENDAR_ALT_SOLID, null));
        clinicSection.addItem(createNavItem("Datos Clínicos", LineAwesomeIcon.DATABASE_SOLID, PatientDataView.class));
        clinicSection.addItem(createNavItem("Categorías", LineAwesomeIcon.NOTES_MEDICAL_SOLID, ClinicalCategoryView.class));

        // SECCIÓN: COMUNICACIONES
        SideNavItem communicationsSection = createSection(
            "Comunicaciones",
            LineAwesomeIcon.COMMENTS,
            null
        );
        //communicationsSection.addItem(createNavItem("Chat", LineAwesomeIcon.COMMENT_DOTS_SOLID, ChatView.class));
        //communicationsSection.addItem(createNavItem("WhatsApp", LineAwesomeIcon.WHATSAPP, WhatsAppChatView.class));
        //communicationsSection.addItem(createNavItem("Mensajes Test", LineAwesomeIcon.GLOBE_SOLID, TestWhatsappView.class));
        //communicationsSection.addItem(createNavItem("Envíos Test", LineAwesomeIcon.PAPER_PLANE, EnviosTestView.class));

        // SECCIÓN: HERRAMIENTAS
        SideNavItem toolsSection = createSection(
            "Herramientas",
            LineAwesomeIcon.TOOLS_SOLID,
            null
        );
        toolsSection.addItem(createNavItem("Plantillas", LineAwesomeIcon.FILE_ALT_SOLID, null));
        toolsSection.addItem(createNavItem("Reportes", LineAwesomeIcon.CHART_BAR_SOLID, null));

        // SECCIÓN: CONFIGURACIÓN
        SideNavItem configSection = createSection(
            "Configuración",
            LineAwesomeIcon.COG_SOLID,
            null
        );
        configSection.addItem(createNavItem("Preferencias", LineAwesomeIcon.SLIDERS_H_SOLID, null));
        configSection.addItem(createNavItem("Usuarios", LineAwesomeIcon.USER_COG_SOLID, null));
        configSection.addItem(createNavItem("Sistema", LineAwesomeIcon.SERVER_SOLID, null));

        // Agregar todas las secciones al nav
        nav.addItem(dashboardSection);
        nav.addItem(clinicSection);
        nav.addItem(communicationsSection);
        nav.addItem(toolsSection);
        nav.addItem(configSection);

        return nav;
    }

    private SideNavItem createSection(String text, LineAwesomeIcon icon, Class<? extends Component> navigationTarget) {
        SideNavItem item = new SideNavItem(text, navigationTarget, icon.create());
        item.addClassName("menu-section");
        return item;
    }

    private SideNavItem createNavItem(String text, LineAwesomeIcon icon, Class<? extends Component> navigationTarget) {
        return new SideNavItem(text, navigationTarget, icon.create());
    }

    private Footer createDrawerFooter() {
        Footer footer = new Footer();
        footer.addClassName("app-nav-footer");

        String userNickname = (String) VaadinSession.getCurrent().getAttribute("nickname");
        Div userInfo = new Div();
        //userInfo.add(LineAwesomeIcon.USER_CIRCLE_SOLID));
        userInfo.add(LineAwesomeIcon.USER.create());
        userInfo.add(new Span(" " + (userNickname != null ? userNickname : "Invitado")));
        userInfo.addClassName("user-info");

        Button logoutButton = new Button("Cerrar Sesión", LineAwesomeIcon.SIGN_OUT_ALT_SOLID.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClassName("logout-button");
        logoutButton.addClickListener(e -> logout());

        VerticalLayout footerContent = new VerticalLayout(userInfo, logoutButton);
        footerContent.setPadding(true);
        footerContent.setSpacing(true);

        footer.add(footerContent);
        return footer;
    }

    private void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().navigate(LoginView.class);
    }

    private void addThemeVariants() {
        addClassName("clinica-layout");
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
        
        User currentUser = (User) VaadinSession.getCurrent().getAttribute("currentUser");
        if (currentUser == null) {
            System.out.println("No hay sesión activa");
            UI.getCurrent().navigate(LoginView.class);
            toggle.setVisible(false);
        } else {
            toggle.setVisible(true);
        }
    }


    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
    
}
