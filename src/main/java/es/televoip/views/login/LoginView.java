package es.televoip.views.login;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import es.televoip.model.User;
import es.televoip.util.MyNotification;
import es.televoip.util.UserDatabase;
import es.televoip.views.MainLayout;
import es.televoip.views.clinica.ClinicalCategoryView;

@Route(value = "login", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Vaadin AI Chat")
public class LoginView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/*
	public LoginView() {
		addClassName(getClass().getSimpleName());

		H1 title = new H1("AI Chat");
		title.addClassName(getClass().getSimpleName() + "-title");

		TextField nickname = new TextField();
		nickname.addClassName(getClass().getSimpleName() + "-nickname");
		nickname.setPlaceholder("Introduzca su NickName...");

		Button enter = new Button("Enter", event -> enter(nickname.getValue()));
		enter.addClassName(getClass().getSimpleName() + "-enter");
		enter.addClickShortcut(Key.ENTER);

		VerticalLayout form = new VerticalLayout(title, nickname, enter);
		form.setSizeUndefined();
		form.addClassName(getClass().getSimpleName() + "-form");
		add(form);
	}

	private void enter(String nickname) {
		if (nickname.trim().isEmpty()) {
			Notification.show("Introduzca un NickName");
		} else {
			VaadinSession.getCurrent().setAttribute("nickname", nickname);
			UI.getCurrent().navigate(ClinicalCategoryView.class);
		}
	}
	*/
	
	public LoginView() {
		addClassName(getClass().getSimpleName());

      H1 title = new H1("AI Chat");
      title.addClassName(getClass().getSimpleName() + "-title");
      
      TextField usernameField = new TextField("Username");
      usernameField.addClassName(getClass().getSimpleName() + "-nickname");
     
      Button loginButton = new Button("Login", event -> login(usernameField.getValue()));
      loginButton.addClassName(getClass().getSimpleName() + "-enter");
      loginButton.addClickShortcut(Key.ENTER);

      //add(title, usernameField, loginButton);
      VerticalLayout form = new VerticalLayout(title, usernameField, loginButton);
		form.setSizeUndefined();
		form.addClassName(getClass().getSimpleName() + "-form");
		add(form);
  }

  private void login(String username) {
      if (username.trim().isEmpty()) {
          Notification.show("Introduzca un nombre de usuario.");
          return;
      }

      if (!UserDatabase.isValidUser(username)) {
          //Notification.show("Usuario no válido.");
          MyNotification.showError("Usuario no válido.", Position.MIDDLE, 3000);
          return;
      }

      // Obtener usuario desde la base de datos
      User user = UserDatabase.getUser(username);

      // Guardar usuario en la sesión
      //VaadinSession.getCurrent().setAttribute(User.class, user);
      VaadinSession.getCurrent().setAttribute("currentUser", user);


      Notification.show("Bienvenido, " + user.getUsername() + "! Rol: " + user.getRole());

      // Redirigir al panel principal
      UI.getCurrent().navigate(ClinicalCategoryView.class);
  }
	
}
