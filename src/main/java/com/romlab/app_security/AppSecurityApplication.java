package com.romlab.app_security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
/* Esta anotación es para incorporar
* la seguridad ya está colocado por default asi
* que colocarla ya será dependiendo de
* cada persona la opcion debug esta esta por
* defaultnos en false y nos ayuda
* para saber porque filtros pasann las peticiones
* NOTA: solo usarlo en desarrollo ya que
* contiene datos sensibles.
*/
//@EnableWebSecurity(debug = true)
@EnableWebSecurity
public class AppSecurityApplication {
	public static void main(String[] args) {
		SpringApplication.run(AppSecurityApplication.class, args);
	}
}
