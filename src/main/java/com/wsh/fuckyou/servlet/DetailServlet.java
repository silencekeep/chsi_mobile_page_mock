package com.wsh.fuckyou.servlet;

import com.wsh.fuckyou.model.StudentRecord;
import com.wsh.fuckyou.service.ConfigService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 学籍详情页 Servlet
 */
public class DetailServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String id = req.getParameter("id");
        if (id == null || id.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 id 参数");
            return;
        }
        
        StudentRecord record = ConfigService.getInstance().getRecord(id);
        if (record == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "记录不存在");
            return;
        }
        
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        
        // 生成详情页 HTML
        String html = generateDetailPage(record);
        resp.getWriter().write(html);
    }
    
    private String generateDetailPage(StudentRecord record) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>学籍详情 - escapeHtml(record.getStudentName())</title>\n" +
            "    <link rel=\"stylesheet\" href=\"/resources/app.css\">\n" +
            "    <style>\n" +
            "        body { font-family: \"Microsoft YaHei\", sans-serif; padding: 20px; background: #f5f5f5; }\n" +
            "        .detail-card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
            "        .detail-item { margin: 15px 0; }\n" +
            "        .detail-label { color: #666; font-size: 14px; margin-bottom: 5px; }\n" +
            "        .detail-value { color: #333; font-size: 16px; font-weight: 500; }\n" +
            "        .back-btn { display: inline-block; padding: 10px 20px; background: #25b887; color: white; text-decoration: none; border-radius: 4px; }\n" +
            "        .back-btn:hover { background: #1fa378; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"detail-card\">\n" +
            "        <h2 style=\"margin-top: 0; color: #25b887;\">学籍详情</h2>\n" +
            "        \n" +
            "        <div class=\"detail-item\">\n" +
            "            <div class=\"detail-label\">姓名</div>\n" +
            "            <div class=\"detail-value\"> + escapeHtml(record.getStudentName()) + </div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"detail-item\">\n" +
            "            <div class=\"detail-label\">证件号码</div>\n" +
            "            <div class=\"detail-value\"> + escapeHtml(record.getIdNumber()) + </div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"detail-item\">\n" +
            "            <div class=\"detail-label\">院校名称</div>\n" +
            "            <div class=\"detail-value\">" + escapeHtml(record.getSchool()) + "</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"detail-item\">\n" +
            "            <div class=\"detail-label\">层次</div>\n" +
            "            <div class=\"detail-value\">" + escapeHtml(record.getLevel()) + "</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"detail-item\">\n" +
            "            <div class=\"detail-label\">专业</div>\n" +
            "            <div class=\"detail-value\">" + escapeHtml(record.getMajor()) + "</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"detail-item\">\n" +
            "            <div class=\"detail-label\">描述</div>\n" +
            "            <div class=\"detail-value\">" + escapeHtml(record.getDescription()) + "</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div style=\"margin-top: 30px;\">\n" +
            "            <a href=\"/\" class=\"back-btn\">返回主页</a>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
