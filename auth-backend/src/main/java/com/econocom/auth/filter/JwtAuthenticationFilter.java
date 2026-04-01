package com.econocom.auth.filter;

import com.econocom.auth.util.JwtUtil;
import com.econocom.auth.util.LoggingUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CLASS_NAME = JwtAuthenticationFilter.class.getSimpleName();


    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        

        LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Getting request header");

        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        String email = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {

            jwt = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                email = jwtUtil.getEmailFromToken(jwt);

                LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                                String.format("Email from token: %s", email));

            } catch (Exception e) {

                LoggingUtil.log(log, LoggingUtil.ERROR, CLASS_NAME, methodName, 
                                String.format("Error extracting email from token: %s", e.getMessage()));
            }
        }

        if (email != null && 
            SecurityContextHolder.getContext().getAuthentication() == null && 
            jwtUtil.validateToken(jwt).booleanValue()) {

            
            LoggingUtil.log(log, LoggingUtil.INFO, CLASS_NAME, methodName, "Data can be processed");
            
            UserDetails userDetails = new User(email, "", new ArrayList<>());
                
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            LoggingUtil.log(log, LoggingUtil.DEBUG, CLASS_NAME, methodName, 
                            String.format("'authToken' generated: %s", authToken));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}