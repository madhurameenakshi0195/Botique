package com.madhura;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class AuthController {
    private static final long WINDOW_MS = 15 * 60 * 1000L;
    private static final int MAX_ATTEMPTS = 5;

    private final AdminRepository admins;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    public AuthController(AdminRepository admins, JwtService jwt) {
        this.admins = admins;
        this.jwt = jwt;
    }

    @GetMapping("/")
    public Map<String, Object> health() {
        return Map.of("ok", true, "name", "Madhura Sarees API");
    }

    @PostMapping("/api/admin/login")
    public Map<String, String> login(@RequestBody Map<String, String> body, HttpServletRequest req) {
        if (tooMany(clientIp(req))) throw new ApiException(429, "Too many attempts. Try again in 15 minutes.");
        String email = String.valueOf(body.getOrDefault("email", "")).toLowerCase().trim();
        String password = String.valueOf(body.getOrDefault("password", ""));
        Admin a = admins.findByEmail(email).orElse(null);
        if (a == null || !encoder.matches(password, a.getPasswordHash())) {
            throw new ApiException(401, "Wrong email or password.");
        }
        return Map.of("token", jwt.create(email));
    }

    private static String clientIp(HttpServletRequest req) {
        String f = req.getHeader("X-Forwarded-For");
        return f != null && !f.isBlank() ? f.split(",")[0].trim() : req.getRemoteAddr();
    }

    private boolean tooMany(String ip) {
        Deque<Long> q = attempts.computeIfAbsent(ip, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) q.pollFirst();
            if (q.size() >= MAX_ATTEMPTS) return true;
            q.addLast(now);
            return false;
        }
    }
}
