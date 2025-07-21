package com.auth.jwtsecurity.config;

import com.auth.jwtsecurity.service.JwtService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwksConfig {

    private final JwtService jwtService;

    public JwksConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public JWKSet jwkSet() {
        RSAPublicKey publicKey = jwtService.getRsaPublicKey();
        return new JWKSet(
            new RSAKey.Builder(publicKey)
                .keyID("default")
                .build()
        );
    }
}
