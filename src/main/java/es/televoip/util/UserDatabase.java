package es.televoip.util;

import es.televoip.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
	private static final Map<String, User> users = new HashMap<>();

	static {
		// Definir usuarios con roles
		users.put("Toni", new User("Toni", "ADMIN"));
		users.put("Pepe", new User("Pepe", "OPERATOR"));
	}

	public static User getUser(String username) {
		return users.get(username);
	}

	public static boolean isValidUser(String username) {
		return users.containsKey(username);
	}
	
}
