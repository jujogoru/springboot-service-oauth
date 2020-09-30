package com.jujogoru.springboot.app.oauth.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@RefreshScope
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{
	
	private static final String SCOPE_READ = "read";
	private static final String SCOPE_WRITE = "write";
	private static final String AUTHORIZED_GRANT_TYPE_PASSWORD = "password";
	private static final String AUTHORIZED_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
	private static final String TOKEN_ACCESS_ALL_METHOD = "permitAll()";
	private static final String TOKEN_ACCESS_CHECK_METHOD = "isAuthenticated()";
	private static final int TIME_ONE_HOUR_IN_SECONDS = 3600;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private AdditionalInfoToken additionalInfoToken;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess(TOKEN_ACCESS_ALL_METHOD)
		.checkTokenAccess(TOKEN_ACCESS_CHECK_METHOD);
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory().withClient(environment.getProperty("config.security.oauth.client.id"))
		.secret(passwordEncoder.encode(environment.getProperty("config.security.oauth.client.secret")))
		.scopes(SCOPE_READ,SCOPE_WRITE)
		.authorizedGrantTypes(AUTHORIZED_GRANT_TYPE_PASSWORD, AUTHORIZED_GRANT_TYPE_REFRESH_TOKEN)
		.accessTokenValiditySeconds(TIME_ONE_HOUR_IN_SECONDS)
		.refreshTokenValiditySeconds(TIME_ONE_HOUR_IN_SECONDS);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(additionalInfoToken, accessTokenConverter()));
		
		endpoints.authenticationManager(authenticationManager)
		.tokenStore(tokenStore())
		.accessTokenConverter(accessTokenConverter())
		.tokenEnhancer(tokenEnhancerChain);
	}

	@Bean
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		tokenConverter.setSigningKey(environment.getProperty("config.security.oauth.jwt.key"));
		return tokenConverter;
	}	
}
