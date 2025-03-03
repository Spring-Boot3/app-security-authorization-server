package com.romlab.app_security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
/* Esta anotación es para incorporar
* la seguridad ya está colocado por default asi
* que colocarla ya será dependiendo de
* cada persona
*/
@EnableWebSecurity
public class AppSecurityApplication {
	public static void main(String[] args) {
		SpringApplication.run(AppSecurityApplication.class, args);
	}
}
