package com.jujogoru.springboot.app.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

import com.jujogoru.springboot.app.commons.users.daos.models.entity.User;
import com.jujogoru.springboot.app.oauth.services.IUserService;

import brave.Tracer;
import feign.FeignException;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher{
	
	private static Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	private static final String ERROR_MESSAGE_TAG_ZEPKIN = "error.message";
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private Tracer tracer;

	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		
		if(authentication.getName().equalsIgnoreCase(env.getProperty("config.security.oauth.client.id"))){
            return;
        }
		
		UserDetails user = (UserDetails) authentication.getPrincipal();
		String msg = "Success login: " + user.getUsername();
		System.out.println(msg);
		log.info(msg);
		
		User userFound = userService.findByUsername(authentication.getName());
		if(userFound.getLoginAttempts() !=  null && userFound.getLoginAttempts() > 0) {
			userFound.setLoginAttempts(0);
			userService.update(userFound, userFound.getId());
		}
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String msg = "Login error: " + exception.getMessage();
		System.out.println(msg);
		log.error(msg);
		
		try {
			StringBuilder errors = new StringBuilder();
			errors.append(msg);
			
			User user = userService.findByUsername(authentication.getName());
			if(user.getLoginAttempts() == null) {
				user.setLoginAttempts(0);
			}
			
			String loginAttemptsMsg = "Current login attempts: ";
			log.info(loginAttemptsMsg + user.getLoginAttempts());
			user.setLoginAttempts(user.getLoginAttempts() + 1);
			log.info(loginAttemptsMsg + user.getLoginAttempts());
			
			errors.append("  -  " + loginAttemptsMsg + user.getLoginAttempts());
			
			if(user.getLoginAttempts() >= 3) {
				String errorMaxAttempts = String.format("Maximum attempts allowed. User '%s' disabled.", authentication.getName());
				log.error(errorMaxAttempts);
				errors.append("  -  " + errorMaxAttempts);
				user.setEnabled(false);
			}
			
			userService.update(user, user.getId());
			
			tracer.currentSpan().tag(ERROR_MESSAGE_TAG_ZEPKIN, errors.toString());
			
		} catch(FeignException e) {
			log.error(String.format("User '%s' not found.", authentication.getName()));			
		}
		
	}
}
