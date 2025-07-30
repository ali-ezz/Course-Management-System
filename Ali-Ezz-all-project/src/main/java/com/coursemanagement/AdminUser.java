package com.coursemanagement;


import java.util.ArrayList;
import java.util.List;

public class AdminUser extends User {
    private List<String> permissions;
    private String adminLevel;

    public AdminUser(String userId, String name, String email, String password) {
        super(userId, name, email, password);
        this.permissions = new ArrayList<>();
        this.adminLevel = "Standard";
        initializeDefaultPermissions();
    }

    public AdminUser(String userId, String name, String email, String password, 
                     String adminLevel) {
        super(userId, name, email, password);
        this.permissions = new ArrayList<>();
        this.adminLevel = adminLevel;
        initializeDefaultPermissions();
    }

    private void initializeDefaultPermissions() {
        permissions.add("VIEW_USERS");
        permissions.add("MANAGE_SYSTEM");

        if ("SuperAdmin".equals(adminLevel)) {
            permissions.add("MODIFY_USERS");
            permissions.add("SYSTEM_CONFIG");
        }
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }

    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public List<String> getPermissions() {
        return new ArrayList<>(permissions);
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
        permissions.clear();
        initializeDefaultPermissions();
    }

    @Override
    public String toString() {
        return "AdminUser{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", adminLevel='" + adminLevel + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}