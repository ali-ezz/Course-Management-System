package com.coursemanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthenticationManager {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationManager.class.getName());
    protected Map<String, User> userDatabase;
    private static final String USER_DB_FILE = "users.ser";
    private FileStorageManager<User> storageManager;
    private User currentUser;
    
    public AuthenticationManager() {
        storageManager = new FileStorageManager<>();
        userDatabase = new HashMap<>();
        initializeUserDatabase();
    }
    
    private void initializeUserDatabase() {
        try {
            List<User> users = storageManager.loadFromFile(USER_DB_FILE);
            
            if (users == null || users.isEmpty()) {
                users = createPredefinedUsers();
                storageManager.saveToFile(users, USER_DB_FILE);
                LOGGER.info("Predefined users created and saved.");
            }
            
            users.forEach(user -> userDatabase.put(user.getEmail(), user));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing user database", e);
            userDatabase = new HashMap<>();
            List<User> predefinedUsers = createPredefinedUsers();
            predefinedUsers.forEach(user -> userDatabase.put(user.getEmail(), user));
        }
    }
    
    private List<User> createPredefinedUsers() {
        List<User> predefinedUsers = new ArrayList<>();
        
        predefinedUsers.add(new AdminUser(
            generateUserId(), 
            "System Administrator", 
            "admin@coursemanagement.com", 
            "admin123"
        ));
        
        predefinedUsers.add(new InstructorUser(
            generateUserId(), 
            "John Doe", 
            "instructor@coursemanagement.com", 
            "instructor123"
        ));
        
        predefinedUsers.add(new Student(
            generateUserId(), 
            "Alice Smith", 
            "student@coursemanagement.com", 
            "student123"
        ));
        
        return predefinedUsers;
    }
    
    private String generateUserId() {
        return UUID.randomUUID().toString();
    }
    
    public boolean authenticate(String email, String password) {
        try {
            User user = userDatabase.get(email);
            if (user != null && user.getPassword().equals(password)) {
                currentUser = user;
                LOGGER.info("User authenticated: " + email);
                return true;
            }
            LOGGER.warning("Authentication failed for: " + email);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Authentication error", e);
            return false;
        }
    }
    
    public void registerUser(User user) {
        try {
            if (userExists(user.getEmail())) {
                LOGGER.warning("User already exists: " + user.getEmail());
                throw new IllegalArgumentException("User with this email already exists");
            }
            
            userDatabase.put(user.getEmail(), user);
            saveUsers();
            LOGGER.info("User registered: " + user.getEmail());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering user", e);
        }
    }
    
    private void saveUsers() {
        try {
            storageManager.saveToFile(new ArrayList<>(userDatabase.values()), USER_DB_FILE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving users", e);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getUserByEmail(String email) {
        return userDatabase.get(email);
    }

    public boolean userExists(String email) {
        return userDatabase.containsKey(email);
    }

    public void logout() {
        LOGGER.info("User logged out: " + (currentUser != null ? currentUser.getEmail() : "No user"));
        currentUser = null;
    }
}