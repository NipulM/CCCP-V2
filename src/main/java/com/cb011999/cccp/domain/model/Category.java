package com.cb011999.cccp.domain.model;


public class Category {
    private final int id;
    private String name;
    private Category parentCategory;
    
    public Category(int id, String name, Category parentCategory) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        
        this.id = id;
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public Category(int id, String name) {
        this(id, name, null);
    }
    
    public String getFullPath() {
        if (parentCategory == null) {
            return name;
        }
        return parentCategory.getFullPath() + " > " + name;
    }
    
    public boolean isSubcategoryOf(Category category) {
        if (parentCategory == null) {
            return false;
        }
        if (parentCategory.equals(category)) {
            return true;
        }
        return parentCategory.isSubcategoryOf(category);
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Category getParentCategory() {
        return parentCategory;
    }
    
    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name;
    }
    
    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }
    
    @Override
    public String toString() {
        return getFullPath();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return id == category.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}