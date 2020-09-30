package com.jujogoru.springboot.app.oauth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jujogoru.springboot.app.commons.users.daos.models.entity.User;

@FeignClient(name = "service-users")
public interface UserFeignClient {

	//TODO change usersDao to users ?
	@GetMapping("/usersDao/search/findUser")
	public User findByUsername(@RequestParam String username);
	
	@PutMapping("/usersDao/{id}")
	public User update(@RequestBody User user, @PathVariable Long id);
}
