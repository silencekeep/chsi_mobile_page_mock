package com.wsh.fuckyou;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.FilterHolder;
import com.wsh.fuckyou.service.ConfigService;
import com.wsh.fuckyou.servlet.*;
import jakarta.servlet.*;
import java.io.IOException;

/**
 * Jetty 服务器主入口
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("学信档案模拟服务启动中...");
        System.out.println("===========================================");
        
        // 预加载配置服务
        System.out.println("正在加载配置...");
        ConfigService configService;
        try {
            configService = ConfigService.getInstance();
            System.out.println("✓ 配置服务初始化成功");
        } catch (Exception e) {
            System.err.println("✗ 配置服务初始化失败:");
            e.printStackTrace();
            return;
        }
        
        // 从配置读取服务器参数
        String host = configService.getServerHost();
        int port = configService.getServerPort();
        
        Server server = new Server(port);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        // 添加 UTF-8 编码过滤器
        FilterHolder encodingFilter = new FilterHolder(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {}
            
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
                    throws IOException, ServletException {
                request.setCharacterEncoding("UTF-8");
                response.setCharacterEncoding("UTF-8");
                chain.doFilter(request, response);
            }
            
            @Override
            public void destroy() {}
        });
        context.addFilter(encodingFilter, "/*", null);
        
        // 静态资源服务 (CSS, JS, 图片等)
        ServletHolder staticHolder = new ServletHolder("static", DefaultServlet.class);
        staticHolder.setInitParameter("resourceBase", "./resources");
        staticHolder.setInitParameter("dirAllowed", "false");
        staticHolder.setInitParameter("pathInfoOnly", "true");
        context.addServlet(staticHolder, "/resources/*");
        
        // 学信档案主页
        context.addServlet(new ServletHolder(new ArchiveServlet()), "/index.html");
        context.addServlet(new ServletHolder(new ArchiveServlet()), "/archive");
        context.addServlet(new ServletHolder(new ArchiveServlet()), "/");
        
        // 学籍详情页
        context.addServlet(new ServletHolder(new DetailServlet()), "/archive/wap/gdjy/xj/detail.action");
        
        // Admin 后台管理
        context.addServlet(new ServletHolder(new AdminPageServlet()), "/admin");
        
        System.out.println("===========================================");
        System.out.println("服务器配置完成，准备启动...");
        System.out.println("配置信息:");
        System.out.println("  - 主机: " + host);
        System.out.println("  - 端口: " + port);
        System.out.println("访问地址:");
        System.out.println("  - 学信档案: http://localhost:" + port + "/");
        System.out.println("  - 管理后台: http://localhost:" + port + "/admin");
        System.out.println("===========================================");
        
        server.start();
        System.out.println("✓ 服务器启动成功！");
        server.join();
    }
}
