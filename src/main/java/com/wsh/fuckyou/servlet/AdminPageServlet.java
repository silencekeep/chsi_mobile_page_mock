package com.wsh.fuckyou.servlet;

import com.wsh.fuckyou.model.StudentRecord;
import com.wsh.fuckyou.model.AdConfig;
import com.wsh.fuckyou.service.ConfigService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Admin 管理页面 Servlet - 简化版，无需 API
 */
public class AdminPageServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        
        resp.getWriter().write(getAdminHtml());
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        String action = req.getParameter("action");
        
        if ("add".equals(action)) {
            handleAdd(req, resp);
        } else if ("delete".equals(action)) {
            handleDelete(req, resp);
        } else if ("updateAd".equals(action)) {
            handleUpdateAd(req, resp);
        } else {
            resp.sendRedirect("/admin");
        }
    }
    
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 自动生成ID
            String id = generateRandomId();
            
            StudentRecord record = new StudentRecord();
            record.setId(id);
            record.setType(req.getParameter("type"));
            record.setSchool(req.getParameter("school"));
            record.setLevel(req.getParameter("level"));
            record.setMajor(req.getParameter("major"));
            record.setDescription(req.getParameter("description"));
            
            ConfigService.getInstance().saveRecord(record);
            resp.sendRedirect("/admin?success=add");
        } catch (Exception e) {
            resp.sendRedirect("/admin?error=" + e.getMessage());
        }
    }
    
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ConfigService.getInstance().deleteRecord(req.getParameter("id"));
            resp.sendRedirect("/admin?success=delete");
        } catch (Exception e) {
            resp.sendRedirect("/admin?error=" + e.getMessage());
        }
    }
    
    private void handleUpdateAd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            AdConfig adConfig = new AdConfig();
            adConfig.setEnabled("on".equals(req.getParameter("adEnabled")));
            adConfig.setUrl(req.getParameter("adUrl") != null ? req.getParameter("adUrl") : "");
            adConfig.setCssClass(req.getParameter("adCssClass") != null ? req.getParameter("adCssClass") : "vote-jy-part vote_bg_kzxz");
            
            ConfigService.getInstance().saveAdConfig(adConfig);
            resp.sendRedirect("/admin?success=updateAd");
        } catch (Exception e) {
            resp.sendRedirect("/admin?error=" + e.getMessage());
        }
    }
    
    private String getAdminHtml() {
        List<StudentRecord> records = ConfigService.getInstance().getAllRecords();
        AdConfig adConfig = ConfigService.getInstance().getAdConfig();
        
        StringBuilder recordsHtml = new StringBuilder();
        if (records.isEmpty()) {
            recordsHtml.append("<tr><td colspan='6' style='text-align: center; color: #999; padding:40px;'>暂无记录,点击右上角\"+ 添加记录\"按钮开始</td></tr>");
        } else {
            for (StudentRecord record : records) {
                String typeLabel;
                String typeClass;
                switch (record.getType()) {
                    case "xj": typeLabel = "学籍"; typeClass = "type-xj"; break;
                    case "xl": typeLabel = "学历"; typeClass = "type-xl"; break;
                    case "xw": typeLabel = "学位"; typeClass = "type-xw"; break;
                    case "ky": typeLabel = "考研"; typeClass = "type-ky"; break;
                    default: typeLabel = "未知"; typeClass = "type-unknown"; break;
                }
                
                recordsHtml.append("<tr>")
                    .append("<td>").append(escapeHtml(record.getId())).append("</td>")
                    .append("<td><span class='type-badge ").append(typeClass).append("'>").append(typeLabel).append("</span></td>")
                    .append("<td>").append(escapeHtml(record.getSchool())).append("</td>")
                    .append("<td>").append(escapeHtml(record.getLevel())).append("</td>")
                    .append("<td>").append(escapeHtml(record.getMajor())).append("</td>")
                    .append("<td class='actions'>")
                    .append("<form method='post' style='display:inline;' onsubmit='return confirm(\"确定要删除吗？\");'>")
                    .append("<input type='hidden' name='action' value='delete'>")
                    .append("<input type='hidden' name='id' value='").append(escapeHtml(record.getId())).append("'>")
                    .append("<button type='submit' class='btn btn-danger'>删除</button>")
                    .append("</form>")
                    .append("</td>")
                    .append("</tr>");
            }
        }
        
        return buildHtml(recordsHtml.toString(), adConfig);
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\"", "&quot;").replace("'", "&#x27;");
    }
    
    private String generateRandomId() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
    }
    
    private String buildHtml(String rows, AdConfig adConfig) {
        return "<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>学信档案管理后台</title>\n" +
            "    <style>\n" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f0f2f5; padding: 20px; }\n" +
            "        .container { max-width: 1400px; margin: 0 auto; }\n" +
            "        h1 { color: #333; margin-bottom: 30px; text-align: center; }\n" +
            "        .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }\n" +
            "        .btn-primary { background: #25b887; color: white; }\n" +
            "        .btn-primary:hover { background: #1fa378; }\n" +
            "        .btn-danger { background: #e85759; color: white; }\n" +
            "        .btn-danger:hover { background: #d43f41; }\n" +
            "        .btn-secondary { background: #6c757d; color: white; }\n" +
            "        .btn-secondary:hover { background: #5a6268; }\n" +
            "        .card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n" +
            "        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n" +
            "        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n" +
            "        th { background: #f5f5f5; font-weight: 600; color: #333; }\n" +
            "        tr:hover { background: #f9f9f9; }\n" +
            "        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); overflow-y: auto; }\n" +
            "        .modal-content { background: white; margin: 5% auto; padding: 30px; width: 90%; max-width: 600px; border-radius: 8px; max-height: 80vh; overflow-y: auto; }\n" +
            "        .form-group { margin-bottom: 16px; }\n" +
            "        .form-group label { display: block; margin-bottom: 6px; color: #333; font-weight: 500; }\n" +
            "        .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }\n" +
            "        .form-group small { color: #6c757d; font-size: 12px; margin-left: 4px; }\n" +
            "        .checkbox-wrapper { display: flex; align-items: center; padding: 12px; background: #f8f9fa; border-radius: 6px; border: 1px solid #e9ecef; }\n" +
            "        .checkbox-wrapper input[type=\"checkbox\"] { width: 20px; height: 20px; margin: 0 10px 0 0; cursor: pointer; flex-shrink: 0; }\n" +
            "        .checkbox-wrapper label { margin: 0; font-weight: 500; color: #495057; cursor: pointer; user-select: none; }\n" +
            "        .form-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 24px; }\n" +
            "        .link-btn { color: #25b887; text-decoration: none; padding: 8px 16px; border: 1px solid #25b887; border-radius: 4px; display: inline-block; }\n" +
            "        .link-btn:hover { background: #25b887; color: white; }\n" +
            "        .type-badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 500; white-space: nowrap; }\n" +
            "        .type-xj { background: #d4edda; color: #155724; }\n" +
            "        .type-xl { background: #cce5ff; color: #004085; }\n" +
            "        .type-xw { background: #e7d4f5; color: #6a1b9a; }\n" +
            "        .type-ky { background: #ffe0b2; color: #e65100; }\n" +
            "        \n" +
            "        /* 移动端响应式设计 */\n" +
            "        @media screen and (max-width: 768px) {\n" +
            "            body { padding: 10px; }\n" +
            "            h1 { font-size: 24px; margin-bottom: 20px; }\n" +
            "            .card { padding: 15px; }\n" +
            "            .container { padding: 0; }\n" +
            "            \n" +
            "            /* 表格横向滚动 */\n" +
            "            .table-wrapper { overflow-x: auto; -webkit-overflow-scrolling: touch; }\n" +
            "            table { min-width: 800px; }\n" +
            "            th, td { padding: 8px; font-size: 13px; }\n" +
            "            \n" +
            "            /* 按钮组响应式 */\n" +
            "            .form-actions { flex-direction: column; }\n" +
            "            .btn { width: 100%; padding: 12px; }\n" +
            "            \n" +
            "            /* 模态框适配 */\n" +
            "            .modal-content { margin: 10px; padding: 20px; width: calc(100% - 20px); max-height: 90vh; }\n" +
            "            \n" +
            "            /* 链接按钮响应式 */\n" +
            "            .link-btn { display: block; text-align: center; margin-bottom: 10px; }\n" +
            "        }\n" +
            "        \n" +
            "        @media screen and (max-width: 480px) {\n" +
            "            h1 { font-size: 20px; }\n" +
            "            .form-group input, .form-group select { font-size: 16px; } /* 防止iOS自动缩放 */\n" +
            "            .checkbox-wrapper { padding: 10px; }\n" +
            "            .checkbox-wrapper input[type=\"checkbox\"] { width: 18px; height: 18px; }\n" +
            "        }\n" +
            "    </style>\n</head>\n<body>\n" +
            "    <div class=\"container\">\n" +
            "        <h1>🎓 学信档案管理后台</h1>\n" +
            "        <div class=\"card\">\n" +
            "            <h2 style=\"color: #333; margin-bottom: 15px;\">📢 广告管理</h2>\n" +
            "            <form method=\"post\" style=\"border: 1px solid #ddd; padding: 15px; border-radius: 6px; background: #f9f9f9;\">\n" +
            "                <input type=\"hidden\" name=\"action\" value=\"updateAd\">\n" +
            "                <div class=\"form-group\">\n" +
            "                    <div class=\"checkbox-wrapper\">\n" +
            "                        <input type=\"checkbox\" id=\"adEnabled\" name=\"adEnabled\" " + (adConfig.isEnabled() ? "checked" : "") + ">\n" +
            "                        <label for=\"adEnabled\">启用广告显示</label>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                <div class=\"form-group\">\n" +
            "                    <label>广告链接</label>\n" +
            "                    <input name=\"adUrl\" value=\"" + escapeHtml(adConfig.getUrl()) + "\" placeholder=\"https://example.com\">\n" +
            "                </div>\n" +
            "                <div class=\"form-group\">\n" +
            "                    <label>CSS类名 <small>(用于控制广告样式)</small></label>\n" +
            "                    <input name=\"adCssClass\" value=\"" + escapeHtml(adConfig.getCssClass()) + "\" placeholder=\"vote-jy-part vote_bg_kzxz\">\n" +
            "                </div>\n" +
            "                <button type=\"submit\" class=\"btn btn-primary\">💾 保存广告配置</button>\n" +
            "            </form>\n" +
            "        </div>\n" +
            "        <div class=\"card\">\n" +
            "            <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; flex-wrap: wrap; gap: 10px;\">\n" +
            "                <h2 style=\"color: #333; margin: 0;\">学籍/学历/学位/考研记录</h2>\n" +
            "                <div style=\"display: flex; gap: 10px; flex-wrap: wrap;\">\n" +
            "                    <a href=\"/\" class=\"link-btn\" target=\"_blank\">📋 查看档案</a>\n" +
            "                    <button class=\"btn btn-primary\" onclick=\"openModal()\">+ 添加记录</button>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            <div class=\"table-wrapper\">\n" +
            "                <table>\n" +
            "                    <thead><tr><th>ID</th><th>类型</th><th>院校</th><th>层次</th><th>专业</th><th>操作</th></tr></thead>\n" +
            "                    <tbody>" + rows + "</tbody>\n" +
            "                </table>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <div id=\"modal\" class=\"modal\">\n" +
            "        <div class=\"modal-content\">\n" +
            "            <h2>添加学籍/学历/学位/考研记录</h2>\n" +
            "            <form method=\"post\">\n" +
            "                <input type=\"hidden\" name=\"action\" value=\"add\">\n" +
            "                <div class=\"form-group\">\n" +
            "                    <label>类型 <small>(xj=学籍(绿色) xl=学历(蓝色) xw=学位(紫色) ky=考研(橙色))</small></label>\n" +
            "                    <select name=\"type\">\n" +
            "                        <option value=\"\">请选择</option>\n" +
            "                        <option value=\"xj\">学籍 - 在读信息</option>\n" +
            "                        <option value=\"xl\">学历 - 毕业证书</option>\n" +
            "                        <option value=\"xw\">学位 - 学位证书</option>\n" +
            "                        <option value=\"ky\">考研 - 研究生考试</option>\n" +
                "                    </select>\n" +
            "                </div>\n" +
            "                <div class=\"form-group\"><label>院校</label><input name=\"school\"></div>\n" +
            "                <div class=\"form-group\"><label>层次</label><input name=\"level\" placeholder=\"本科/专科/研究生\"></div>\n" +
            "                <div class=\"form-group\"><label>专业</label><input name=\"major\"></div>\n" +
            "                <div class=\"form-group\"><label>描述</label><input name=\"description\" placeholder=\"如: 2019级 | 软件工程\"></div>\n" +
            "                <div class=\"form-actions\">\n" +
            "                    <button type=\"button\" class=\"btn btn-secondary\" onclick=\"closeModal()\">取消</button>\n" +
            "                    <button type=\"submit\" class=\"btn btn-primary\">保存</button>\n" +
            "                </div>\n" +
            "            </form>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <script>\n" +
            "        function openModal() { document.getElementById('modal').style.display = 'block'; }\n" +
            "        function closeModal() { document.getElementById('modal').style.display = 'none'; }\n" +
            "        window.onclick = e => { if (e.target == document.getElementById('modal')) closeModal(); }\n" +
            "        const p = new URLSearchParams(location.search);\n" +
            "        if (p.has('success')) { alert(p.get('success')=='add'?'添加成功！':'删除成功！'); history.replaceState({},document.title,'/admin'); }\n" +
            "        if (p.has('error')) { alert('失败：'+p.get('error')); history.replaceState({},document.title,'/admin'); }\n" +
            "    </script>\n" +
            "</body>\n</html>";
    }
}
