package com.wsh.fuckyou.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wsh.fuckyou.model.StudentRecord;
import com.wsh.fuckyou.model.AdConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理服务（单例模式）
 */
public class ConfigService {
    
    private static final String CONFIG_FILE = "config.json";
    private static final ObjectMapper MAPPER;
    
    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    private static final ConfigService INSTANCE = new ConfigService();
    
    private final Map<String, StudentRecord> recordsMap = new ConcurrentHashMap<>();
    private AdConfig adConfig = new AdConfig(); // 广告配置
    private String serverHost = "0.0.0.0"; // 服务器主机
    private int serverPort = 8080; // 服务器端口
    
    private ConfigService() {
        loadConfig();
    }
    
    public static ConfigService getInstance() {
        return INSTANCE;
    }
    
    /**
     * 从 config.json 加载配置
     */
    @SuppressWarnings("unchecked")
    public synchronized void loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) {
                System.err.println("⚠️  配置文件不存在: " + CONFIG_FILE);
                System.err.println("   将创建默认配置文件");
                createDefaultConfig();
                return;
            }
            
            Map<String, Object> config = MAPPER.readValue(file, Map.class);
            
            if (config == null) {
                System.err.println("⚠️  配置文件为空，创建默认配置");
                createDefaultConfig();
                return;
            }
            
            Object recordsObj = config.get("records");
            if (recordsObj == null) {
                System.err.println("⚠️  配置文件中没有 records 字段，创建空记录集");
                recordsMap.clear();
                return;
            }
            
            List<Map<String, String>> records = (List<Map<String, String>>) recordsObj;
            
            recordsMap.clear();
            if (records != null) {
                for (Map<String, String> rec : records) {
                    StudentRecord record = new StudentRecord();
                    record.setId(rec.get("id"));
                    record.setType(rec.get("type")); // 读取类型：xj/xl/xw/ky
                    record.setSchool(rec.get("school"));
                    record.setLevel(rec.get("level"));
                    record.setMajor(rec.get("major"));
                    record.setDescription(rec.get("description"));
                    
                    if (record.getId() != null) {
                        recordsMap.put(record.getId(), record);
                    }
                }
            }
            
            System.out.println("✓ 配置加载成功，共 " + recordsMap.size() + " 条记录");
            
            // 读取服务器配置
            Object serverObj = config.get("server");
            if (serverObj != null && serverObj instanceof Map) {
                Map<String, Object> serverMap = (Map<String, Object>) serverObj;
                if (serverMap.get("host") != null) {
                    serverHost = (String) serverMap.get("host");
                }
                if (serverMap.get("port") != null) {
                    serverPort = ((Number) serverMap.get("port")).intValue();
                }
            }
            
            // 读取广告配置
            Object adObj = config.get("ad");
            if (adObj != null && adObj instanceof Map) {
                Map<String, Object> adMap = (Map<String, Object>) adObj;
                adConfig.setEnabled(Boolean.TRUE.equals(adMap.get("enabled")));
                adConfig.setUrl((String) adMap.get("url"));
                adConfig.setCssClass((String) adMap.get("cssClass"));
            }
        } catch (IOException e) {
            System.err.println("✗ 加载配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("outputDir", "dist");
            config.put("sourceHtml", "archive-template.html");
            
            // 默认服务器配置
            Map<String, Object> serverMap = new HashMap<>();
            serverMap.put("host", "0.0.0.0");
            serverMap.put("port", 8080);
            config.put("server", serverMap);
            
            // 默认广告配置
            Map<String, Object> adMap = new HashMap<>();
            adMap.put("enabled", false);
            adMap.put("url", "");
            adMap.put("cssClass", "vote-jy-part vote_bg_kzxz");
            config.put("ad", adMap);
            
            config.put("records", new ArrayList<>());
            
            File file = new File(CONFIG_FILE);
            MAPPER.writeValue(file, config);
            System.out.println("✓ 默认配置文件已创建: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("✗ 创建默认配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 保存配置到 config.json
     */
    public synchronized void saveConfig() throws IOException {
        Map<String, Object> config = new HashMap<>();
        config.put("outputDir", "dist");
        config.put("sourceHtml", "archive-template.html");
        
        // 保存服务器配置
        Map<String, Object> serverMap = new HashMap<>();
        serverMap.put("host", serverHost);
        serverMap.put("port", serverPort);
        config.put("server", serverMap);
        
        // 保存广告配置
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("enabled", adConfig.isEnabled());
        adMap.put("url", adConfig.getUrl());
        adMap.put("cssClass", adConfig.getCssClass());
        config.put("ad", adMap);
        
        config.put("records", new ArrayList<>(recordsMap.values()));
        
        File file = new File(CONFIG_FILE);
        MAPPER.writeValue(file, config);
        System.out.println("✓ 配置已保存");
    }
    
    /**
     * 获取所有记录
     */
    public List<StudentRecord> getAllRecords() {
        return new ArrayList<>(recordsMap.values());
    }
    
    /**
     * 根据ID获取记录
     */
    public StudentRecord getRecord(String id) {
        return recordsMap.get(id);
    }
    
    /**
     * 添加或更新记录
     */
    public void saveRecord(StudentRecord record) throws IOException {
        recordsMap.put(record.getId(), record);
        saveConfig();
    }
    
    /**
     * 删除记录
     */
    public void deleteRecord(String id) throws IOException {
        recordsMap.remove(id);
        saveConfig();
    }
    
    /**
     * 获取广告配置
     */
    public AdConfig getAdConfig() {
        return adConfig;
    }
    
    /**
     * 保存广告配置
     */
    public void saveAdConfig(AdConfig config) throws IOException {
        this.adConfig = config;
        saveConfig();
    }
    
    /**
     * 获取服务器主机地址
     */
    public String getServerHost() {
        return serverHost;
    }
    
    /**
     * 获取服务器端口
     */
    public int getServerPort() {
        return serverPort;
    }
}
