package com.jujogoru.springboot.app.oauth.services;

import com.jujogoru.springboot.app.commons.users.daos.models.entity.User;

public interface IUserService {

	public User findByUsername(String username);
	
	public User update(User user, Long id);
}
