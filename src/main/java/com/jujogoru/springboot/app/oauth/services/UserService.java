package com.jujogoru.springboot.app.oauth.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jujogoru.springboot.app.commons.users.daos.models.entity.User;
import com.jujogoru.springboot.app.oauth.client.UserFeignClient;

import brave.Tracer;
import feign.FeignException;

@Service
public class UserService implements IUserService, UserDetailsService {

	private Logger log = LoggerFactory.getLogger(UserService.class);
	
	private static final String ERROR_MESSAGE_TAG_ZEPKIN = "error.message";

	@Autowired
	private Tracer tracer;
	
	@Autowired
	private UserFeignClient client;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		try {
			User user = client.findByUsername(username);

			List<GrantedAuthority> authorities = user.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority(role.getName()))
					.peek(autorithy -> log.info("Role: " + autorithy.getAuthority())).collect(Collectors.toList());

			log.info("Logged user: " + username);

			return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
					user.getEnabled(), true, true, true, authorities);

		} catch (FeignException e) {
			String error = "User '" + username + "' not found.";
			log.error(error);
			tracer.currentSpan().tag(ERROR_MESSAGE_TAG_ZEPKIN, error + ": " + e.getMessage());
			throw new UsernameNotFoundException("User '" + username + "' not found.");
		}
	}

	@Override
	public User findByUsername(String username) {
		return client.findByUsername(username);
	}

	@Override
	public User update(User user, Long id) {
		return client.update(user, id);
	}

}
