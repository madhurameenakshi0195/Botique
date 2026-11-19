package com.madhura;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtService jwt;
    private final String[] origins;

    public WebConfig(JwtService jwt, @Value("${app.frontend-urls}") String urls) {
        this.jwt = jwt;
        this.origins = Arrays.stream(urls.split(",")).map(String::trim)
                .map(s -> s.replaceAll("/$", "")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*");
    }

    /** Only the admin may change products: every non-GET call to /api/products needs a valid admin token. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                String m = req.getMethod();
                if (m.equals("GET") || m.equals("OPTIONS") || m.equals("HEAD")) return true;
                String h = req.getHeader("Authorization");
                if (h != null && h.startsWith("Bearer ") && jwt.isAdmin(h.substring(7))) return true;
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Please log in as admin.\"}");
                return false;
            }
        }).addPathPatterns("/api/products", "/api/products/**");
    }
}
