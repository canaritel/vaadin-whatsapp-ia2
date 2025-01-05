package es.televoip.model;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String role; // Ejemplo: "ADMIN", "OPERATOR"

	public User(String username, String role) {
		this.username = username;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User{" + "username='" + username + '\'' + ", role='" + role + '\'' + '}';
	}
	
}
