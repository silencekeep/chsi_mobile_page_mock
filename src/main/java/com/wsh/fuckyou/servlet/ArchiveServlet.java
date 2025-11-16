package com.wsh.fuckyou.servlet;

import com.wsh.fuckyou.model.StudentRecord;
import com.wsh.fuckyou.model.AdConfig;
import com.wsh.fuckyou.service.ConfigService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 学信档案主页 Servlet
 */
public class ArchiveServlet extends HttpServlet {
    
    private static final String SOURCE_HTML = "archive-template.html";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // 设置请求和响应编码
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        
        // 读取源 HTML
        File sourceFile = new File(SOURCE_HTML);
        if (!sourceFile.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "源文件不存在");
            return;
        }
        
        String html = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
        
        // 获取所有记录并按类型分组
        List<StudentRecord> records = ConfigService.getInstance().getAllRecords();
        
        // 分别处理学籍、学历、学位、考研
        List<StudentRecord> xjRecords = records.stream()
            .filter(r -> "xj".equals(r.getType()))
            .toList();
        List<StudentRecord> xlRecords = records.stream()
            .filter(r -> "xl".equals(r.getType()))
            .toList();
        List<StudentRecord> xwRecords = records.stream()
            .filter(r -> "xw".equals(r.getType()))
            .toList();
        List<StudentRecord> kyRecords = records.stream()
            .filter(r -> "ky".equals(r.getType()))
            .toList();
        
        // 处理学籍信息区域
        html = handleXjSection(html, xjRecords);
        
        // 处理广告区域
        html = handleAdSection(html);
        
        // 处理学历信息区域
        html = handleXlSection(html, xlRecords);
        
        // 处理学位信息区域
        html = handleXwSection(html, xwRecords);
        
        // 处理考研信息区域
        html = handleKySection(html, kyRecords);
        
        resp.getWriter().write(html);
    }
    
    /**
     * 更新学籍信息标题计数
     */
    private String updateXjTitle(String html, int count) {
        // 更新学籍信息的计数
        html = html.replaceFirst(
            "(<div class=\"l van-col van-col--8\">学籍信息) \\(\\d+\\)(</div>)",
            "$1 (" + count + ")$2"
        );
        return html;
    }
    
    /**
     * 处理广告区域
     */
    private String handleAdSection(String html) {
        AdConfig adConfig = ConfigService.getInstance().getAdConfig();
        
        if (!adConfig.isEnabled()) {
            // 如果广告未启用,移除广告块
            html = html.replaceFirst(
                " <div[^>]*class=\"vote-part\"[^>]*>[\\s\\S]*?</div></div>",
                ""
            );
        } else {
            // 更新链接
            String adUrl = escapeHtml(adConfig.getUrl());
            String adCssClass = adConfig.getCssClass(); // 获取CSS类名配置
            
            html = html.replaceFirst(
                "(<div[^>]*class=\"vote-part\"[^>]*>.*?<a href=\")[^\"]*",
                "$1" + adUrl
            );
            
            // 更新CSS类名
            html = html.replaceFirst(
                "(class=\")[^\"]*vote-jy-part[^\"]*",
                "$1" + escapeHtml(adCssClass)
            );
            
            // 如果URL是图片链接,通过CSS注入背景图片
            if (adUrl != null && !adUrl.isEmpty() && 
                (adUrl.toLowerCase().endsWith(".png") || 
                 adUrl.toLowerCase().endsWith(".jpg") || 
                 adUrl.toLowerCase().endsWith(".jpeg") || 
                 adUrl.toLowerCase().endsWith(".gif"))) {
                
                // 从cssClass中提取 vote_bg_* 类名(原网站实现模式)
                String bgClass = null;
                if (adCssClass != null && adCssClass.contains("vote_bg_")) {
                    String[] classes = adCssClass.split("\\s+");
                    for (String cls : classes) {
                        if (cls.startsWith("vote_bg_")) {
                            bgClass = cls;
                            break;
                        }
                    }
                }
                
                // 动态注入CSS,覆盖app.css中对应的 vote_bg_* 类背景图
                String customCss = "<style>\n" +
                    "/* 广告背景图片 - 动态覆盖app.css中的vote_bg类 */\n" +
                    ".vote-part { margin: 0.267rem 0 0 0 !important; }\n";
                
                if (bgClass != null) {
                    // 精准覆盖指定的 vote_bg_* 类(与原网站实现完全一致)
                    customCss += ".vote-part ." + bgClass + " { \n" +
                        "  background: url('" + adUrl + "') no-repeat !important;\n" +
                        "  background-size: 100% !important;\n" +
                        "  padding: 0 !important;\n" +
                        "  height: 2.133rem !important;\n" +
                        "}\n";
                } else {
                    // 无vote_bg类时,兜底覆盖.vote-jy-part
                    customCss += ".vote-part .vote-jy-part { \n" +
                        "  background: url('" + adUrl + "') no-repeat !important;\n" +
                        "  background-size: 100% !important;\n" +
                        "  padding: 0 !important;\n" +
                        "  height: 2.133rem !important;\n" +
                        "  display: block !important;\n" +
                        "}\n";
                }
                
                customCss += "</style>\n";
                html = html.replaceFirst("<body", customCss + "<body");
            } else {
                // 不是图片链接时,使用原CSS类名方式(背景图片,固定高度)
                String cssClass = adConfig.getCssClass();
                String bgClass = cssClass;
                if (cssClass.contains(" ")) {
                    String[] parts = cssClass.split("\\s+");
                    bgClass = parts[parts.length - 1];
                }
                
                String customCss = "<style>\n" +
                    "/* 动态广告背景 - 固定高度 */\n" +
                    ".vote-part .vote-jy-part." + bgClass + " {\n" +
                    "  background: url(" + adUrl + ") no-repeat !important;\n" +
                    "  background-size: 100% !important;\n" +
                    "  padding: 0 !important;\n" +
                    "  height: 2.133rem !important;\n" +
                    "  display: block !important;\n" +
                    "}\n" +
                    "</style>\n";
                
                html = html.replaceFirst("<body", customCss + "<body");
            }
        }
        return html;
    }
    
    /**
     * 处理学籍信息区域
     */
    private String handleXjSection(String html, List<StudentRecord> xjRecords) {
        // 更新学籍信息标题计数 (学籍总是显示计数)
        html = html.replaceFirst(
            "(<div class=\"l van-col van-col--8\">学籍信息) \\(\\d+\\)(</div>)",
            "$1 (" + xjRecords.size() + ")$2"
        );
        
        if (!xjRecords.isEmpty()) {
            // 构建学籍卡片HTML并插入到学籍信息标题后
            StringBuilder xjCards = new StringBuilder();
            for (StudentRecord record : xjRecords) {
                xjCards.append(" <div class=\"xj list-card\"><a href=\"/archive/wap/gdjy/xj/detail.action?id=")
                    .append(escapeHtml(record.getId()))
                    .append("\"><div class=\"yxmc-wrap\"><div class=\"yxmc\">")
                    .append(escapeHtml(record.getSchool()))
                    .append("</div> <div class=\"cc\">")
                    .append(escapeHtml(record.getLevel()))
                    .append("</div></div> <div class=\"des\">")
                    .append(escapeHtml(record.getMajor()));
                
                // 只有当description不为空时，才添加竖线和description
                if (record.getDescription() != null && !record.getDescription().trim().isEmpty()) {
                    xjCards.append("　|　").append(escapeHtml(record.getDescription()));
                }
                
                xjCards.append("</div></a></div>");
            }
            
            // 在学籍信息标题后插入卡片
            html = html.replaceFirst(
                "(<div class=\"list-tit van-row\"><div class=\"l van-col van-col--8\">学籍信息[\\s\\S]*?</div></div>)",
                "$1" + xjCards.toString()
            );
        }
        return html;
    }
    
    /**
     * 处理学历信息区域
     */
    private String handleXlSection(String html, List<StudentRecord> xlRecords) {
        if (xlRecords.isEmpty()) {
            // 没有学历信息，不显示计数，保留占位符
            html = html.replaceFirst(
                "(<div class=\"l van-col van-col--8\">学历信息) (<!----></div>)",
                "$1 $2"
            );
        } else {
            // 有学历信息，显示计数
            html = html.replaceFirst(
                "(<div class=\"l van-col van-col--8\">学历信息) (<!----></div>)",
                "$1 (" + xlRecords.size() + ")$2"
            );
            
            // 移除"没有找到学历信息"的占位符 (包含完整的嵌套结构)
            html = html.replaceFirst(
                " <div class=\"no-data\"><h4>没有找到您的学历信息[\\s\\S]*?<div class=\"find-xjxl-bottom\">[\\s\\S]*?</div></div>",
                ""
            );
            
            // 构建学历卡片HTML并插入到学历信息标题后
            StringBuilder xlCards = new StringBuilder();
            for (StudentRecord record : xlRecords) {
                xlCards.append(" <div class=\"xl list-card\"><a href=\"/archive/wap/gdjy/xj/detail.action?id=")
                    .append(escapeHtml(record.getId()))
                    .append("\"><div class=\"yxmc-wrap\"><div class=\"yxmc\">")
                    .append(escapeHtml(record.getSchool()))
                    .append("</div> <div class=\"cc\">")
                    .append(escapeHtml(record.getLevel()))
                    .append("</div></div> <div class=\"des\">")
                    .append(escapeHtml(record.getMajor()));
                
                // 只有当description不为空时，才添加竖线和description
                if (record.getDescription() != null && !record.getDescription().trim().isEmpty()) {
                    xlCards.append("　|　").append(escapeHtml(record.getDescription()));
                }
                
                xlCards.append("</div></a></div>");
            }
            
            // 在学历信息标题后插入卡片
            html = html.replaceFirst(
                "(<div class=\"list-tit van-row\"><div class=\"l van-col van-col--8\">学历信息[\\s\\S]*?</div></div>)",
                "$1" + xlCards.toString()
            );
        }
        return html;
    }
    
    /**
     * 处理学位信息区域
     */
    private String handleXwSection(String html, List<StudentRecord> xwRecords) {
        if (xwRecords.isEmpty()) {
            // 没有学位信息，不显示计数，保留占位符
            html = html.replaceFirst(
                "(<div class=\"l van-col van-col--8\">学位信息) (<!----></div>)",
                "$1 $2"
            );
        } else {
            // 有学位信息，显示计数
            html = html.replaceFirst(
                "(<div class=\"l van-col van-col--8\">学位信息) (<!----></div>)",
                "$1 (" + xwRecords.size() + ")$2"
            );
            
            // 移除"还有学位没有显示出来？尝试绑定"的右侧提示
            html = html.replaceFirst(
                "(<div class=\"list-tit van-row\"><div class=\"l van-col van-col--8\">学位信息[^<]*</div>) <div class=\"r van-col van-col--16\">还有学位没有显示出来？<a[^>]*>尝试绑定</a></div>",
                "$1"
            );
            
            // 移除"没有学位信息"的占位符 (包含完整的嵌套结构)
            html = html.replaceFirst(
                " <div class=\"no-data\"><h4[^>]*>.*?您还未绑定学位信息[\\s\\S]*?<div class=\"find-xjxl-bottom\">[\\s\\S]*?</div></div>",
                ""
            );
            
            // 构建学位卡片HTML
            StringBuilder xwCards = new StringBuilder();
            for (StudentRecord record : xwRecords) {
                xwCards.append(" <div class=\"xw list-card\"><a href=\"/archive/wap/gdjy/xj/detail.action?id=")
                    .append(escapeHtml(record.getId()))
                    .append("\"><div class=\"yxmc-wrap\"><div class=\"yxmc\">")
                    .append(escapeHtml(record.getSchool()))
                    .append("</div> <div class=\"cc\">")
                    .append(escapeHtml(record.getLevel()))
                    .append("</div></div> <div class=\"des\">")
                    .append(escapeHtml(record.getMajor()));
                
                // 只有当description不为空时,才添加竖线和description
                if (record.getDescription() != null && !record.getDescription().trim().isEmpty()) {
                    xwCards.append("　|　").append(escapeHtml(record.getDescription()));
                }
                
                xwCards.append("</div></a></div>");
            }
            
            // 在学位信息标题后插入卡片
            html = html.replaceFirst(
                "(<div class=\"list-tit van-row\"><div class=\"l van-col van-col--8\">学位信息[\\s\\S]*?</div></div>)",
                "$1" + xwCards.toString()
            );
        }
        return html;
    }
    
    /**
     * 处理考研信息区域
     */
    private String handleKySection(String html, List<StudentRecord> kyRecords) {
        if (kyRecords.isEmpty()) {
            // 没有考研信息，不显示计数
            html = html.replaceFirst(
                "(<div class=\"l van-col van-col--24\">考研信息) (<!----></div>)",
                "$1 $2"
            );
        } else {
            // 有考研信息，显示计数
            html = html.replaceFirst(
                "(<div class=\"l van-col van-col--24\">考研信息) (<!----></div>)",
                "$1 (" + kyRecords.size() + ")$2"
            );
            
            // 移除"没有考研信息"的占位符
            html = html.replaceFirst(
                " <div class=\"no-data list-card\"><div class=\"yxmc\">您没有考研信息！</div>[\\s\\S]*?</div></div>",
                ""
            );
            
            // 构建考研卡片HTML
            StringBuilder kyCards = new StringBuilder();
            for (StudentRecord record : kyRecords) {
                kyCards.append(" <div class=\"ky list-card\"><a href=\"/archive/wap/gdjy/xj/detail.action?id=")
                    .append(escapeHtml(record.getId()))
                    .append("\"><div class=\"yxmc-wrap\"><div class=\"yxmc\">")
                    .append(escapeHtml(record.getSchool()))
                    .append("</div> <div class=\"cc\">")
                    .append(escapeHtml(record.getLevel()))
                    .append("</div></div> <div class=\"des\">")
                    .append(escapeHtml(record.getMajor()));
                
                // 只有当description不为空时,才添加竖线和description
                if (record.getDescription() != null && !record.getDescription().trim().isEmpty()) {
                    kyCards.append("　|　").append(escapeHtml(record.getDescription()));
                }
                
                kyCards.append("</div></a></div>");
            }
            
            // 在考研信息标题后插入卡片
            html = html.replaceFirst(
                "(<div class=\"list-tit van-row\"><div class=\"l van-col van-col--24\">考研信息[\\s\\S]*?</div></div>)",
                "$1" + kyCards.toString()
            );
        }
        return html;
    }
    
    /**
     * 替换学籍/学历块内容
     * @param type "xj" 学籍 或 "xl" 学历
     */
    private String replaceAcademicBlock(String html, StudentRecord record, String type) {
        // 更新链接指向本地服务
        String pattern = "<a href=\"https://my\\.chsi\\.com\\.cn/archive/wap/gdjy/xj/detail\\.action\\?id=" 
                + Pattern.quote(record.getId()) + "\"[^>]*>([\\s\\S]*?)</a>";
        
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(html);
        
        if (m.find()) {
            String originalBlock = m.group(0);
            String inner = m.group(1);
            
            // 替换院校名称
            inner = inner.replaceFirst(
                "<div class=\"yxmc\">[\\s\\S]*?</div>",
                "<div class=\"yxmc\">" + escapeHtml(record.getSchool()) + "</div>"
            );
            
            // 替换层次
            inner = inner.replaceFirst(
                "<div class=\"cc\">[\\s\\S]*?</div>",
                "<div class=\"cc\">" + escapeHtml(record.getLevel()) + "</div>"
            );
            
            // 替换专业和描述
            inner = inner.replaceFirst(
                "<div class=\"des\">[\\s\\S]*?</div>",
                "<div class=\"des\">" + escapeHtml(record.getMajor()) + "　|　" + escapeHtml(record.getDescription()) + "</div>"
            );
            
            // 构建新的卡片块，根据类型使用不同的CSS class
            // 在卡片外层添加正确的样式类
            String cardPattern = "<div class=\"[^\"]*list-card\">\\s*" + Pattern.quote(originalBlock);
            Pattern cardP = Pattern.compile(cardPattern);
            Matcher cardM = cardP.matcher(html);
            
            if (cardM.find()) {
                // 找到完整的 card div
                String fullCard = cardM.group(0);
                String newCard = "<div class=\"" + type + " list-card\"><a href=\"/archive/wap/gdjy/xj/detail.action?id=" 
                    + record.getId() + "\">" + inner + "</a>";
                html = html.replace(fullCard, newCard);
            } else {
                // 如果找不到外层div，就只替换链接
                String replacement = "<a href=\"/archive/wap/gdjy/xj/detail.action?id=" + record.getId() + "\">" + inner + "</a>";
                html = html.replace(originalBlock, replacement);
            }
        }
        
        return html;
    }
    
    /**
     * 替换个人信息
     */
    private String replacePersonalInfo(String html, StudentRecord record) {
        // 替换姓名
//        html = html.replaceFirst(
//            "(<p>姓名</p>\\s*<h5>)[^<]*(</h5>)",
//            "$1" + escapeHtml(record.getStudentName()) + "$2"
//        );
//
//        // 替换证件号码
//        html = html.replaceFirst(
//            "(<p>证件号码</p>\\s*<h5>)[^<]*(</h5>)",
//            "$1" + escapeHtml(record.getIdNumber()) + "$2"
//        );
        
        return html;
    }
    
    /**
     * 注入字体覆盖样式
     */
    private String injectStyle(String html) {
        String fontOverride = "\n<style>\n" +
            "/* 覆盖可能的私有区字体，强制显示真实文字 */\n" +
            ".yxmc,.cc,.des,.student-name,.student-id, .list-card .yxmc-wrap div {\n" +
            "  font-family: \"Helvetica Neue\",\"PingFang SC\",\"Microsoft YaHei\",\"Hiragino Sans GB\",\"WenQuanYi Micro Hei\",Arial,sans-serif !important;\n" +
            "  font-size: inherit;\n" +
            "}\n" +
            "</style>\n";
        
        return html.replaceFirst("</head>", fontOverride + "</head>");
    }
    
    /**
     * HTML 转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
