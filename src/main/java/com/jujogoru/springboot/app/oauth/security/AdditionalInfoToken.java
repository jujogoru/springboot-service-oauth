package com.jujogoru.springboot.app.oauth.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import com.jujogoru.springboot.app.commons.users.daos.models.entity.User;
import com.jujogoru.springboot.app.oauth.services.IUserService;

@Component
public class AdditionalInfoToken implements TokenEnhancer{

	@Autowired
	private IUserService usuarioService;
	
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> userInfoMap = new HashMap<String, Object>();
		
		User user = usuarioService.findByUsername(authentication.getName());
		userInfoMap.put("name", user.getName());
		userInfoMap.put("lastname", user.getLastname());
		userInfoMap.put("email", user.getEmail());
		
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(userInfoMap);
		
		return accessToken;
	}
}
