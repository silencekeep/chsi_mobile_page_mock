package com.wsh.fuckyou.model;

/**
 * 学籍记录模型
 */
public class StudentRecord {
    private String id;
    private String type; // "xj"学籍 "xl"学历 "xw"学位 "ky"考研
    private String school;
    private String level;
    private String major;
    private String description;
    
    public StudentRecord() {
    }
    
    public StudentRecord(String id, String type, String school, String level, String major, 
                        String description) {
        this.id = id;
        this.type = type;
        this.school = school;
        this.level = level;
        this.major = major;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSchool() {
        return school;
    }
    
    public void setSchool(String school) {
        this.school = school;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
