package es.televoip.util;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public abstract class MyNotification {

   public static void show(String message, Position position, NotificationVariant color, int duration) {
      showNotification(message, position, color, duration, VaadinIcon.CHECK_CIRCLE);
   }

   public static void showWarning(String message, Position position, NotificationVariant color, int duration) {
      showNotification(message, position, color, duration, VaadinIcon.EXCLAMATION_CIRCLE);
   }

   private static void showNotification(String message, Position position, NotificationVariant color, int duration, VaadinIcon icon) {
      Notification notification = new Notification(message, duration);
      notification.setPosition(position);
      notification.addThemeVariants(color);

      Icon notificationIcon = icon.create();
      Div info = new Div(new Text(message));

      HorizontalLayout layout = new HorizontalLayout(notificationIcon, info);
      layout.setAlignItems(FlexComponent.Alignment.CENTER);

      notification.add(layout);
      notification.open();
   }

   public static void showError(String message, Position position, int duration) {
      Notification notification = new Notification();
      notification.setPosition(position);
      notification.setDuration(duration);
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

      Icon icon = VaadinIcon.WARNING.create();
      Div info = new Div(new Text(message));

      Button retryBtn = new Button("Close", clickEvent -> notification.close());
      retryBtn.getStyle().set("margin", "0 0 0 var(--lumo-space-l)");

      HorizontalLayout layout = new HorizontalLayout(icon, info, retryBtn, createCloseBtn(notification));
      layout.setAlignItems(FlexComponent.Alignment.CENTER);

      notification.add(layout);
      notification.open();
   }

   private static Button createCloseBtn(Notification notification) {
      Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), clickEvent -> notification.close());
      closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

      return closeBtn;
   }

}
