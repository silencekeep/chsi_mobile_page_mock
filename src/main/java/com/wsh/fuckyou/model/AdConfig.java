package com.wsh.fuckyou.model;

/**
 * 广告配置模型
 */
public class AdConfig {
    private boolean enabled;      // 是否显示广告
    private String url;           // 广告链接
    private String cssClass;      // CSS类名
    
    public AdConfig() {
        this.enabled = false;
        this.url = "";
        this.cssClass = "vote-jy-part vote_bg_kzxz";
    }
    
    public AdConfig(boolean enabled, String url, String cssClass) {
        this.enabled = enabled;
        this.url = url;
        this.cssClass = cssClass;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
