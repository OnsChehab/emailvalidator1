package com.example.email_validator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/login")
    public String login() {
        logger.info("Serving login page: /login.html");
        return "forward:/login.html";
    }

    @GetMapping(value = {"/", "/index", "/main", "/app", "/application"})
    public String index() {
        logger.info("Serving index page: /index.html");
        return "forward:/index.html";
    }

    @GetMapping("/{path:^(?!api|h2-console|static|css|js|images).*$}/**")
    public String catchAll() {
        logger.info("Catch-all route triggered, forwarding to /index.html");
        return "forward:/index.html";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        logger.error("Error occurred: Status={}, Message={}, Path={}", status, message, path);

        // Rediriger vers le login pour les erreurs d'authentification
        if (status != null && Integer.valueOf(status.toString()) == HttpStatus.UNAUTHORIZED.value()) {
            return "redirect:/login.html";
        }

        if (status != null && Integer.valueOf(status.toString()) == HttpStatus.FORBIDDEN.value()) {
            return "redirect:/login.html";
        }

        if (status != null && Integer.valueOf(status.toString()) == HttpStatus.NOT_FOUND.value()) {
            logger.info("404 error for path {}, forwarding to /index.html", path);
            return "forward:/index.html";
        }

        logger.info("Non-404 error, forwarding to /index.html");
        return "forward:/index.html";
    }
}