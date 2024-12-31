package es.televoip.views.login;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import es.televoip.views.MainLayout;

@Route(value = "login", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Vaadin AI Chat")
public class LoginView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

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
			//UI.getCurrent().navigate(ChatView.class);
		}
	}

}
