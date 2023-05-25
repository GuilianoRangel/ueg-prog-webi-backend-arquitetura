/*
 * ApiSwaggerConfig.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

/**
 * Classe de configuração referente a geração de documentação automatida da API
 * REST.
 * 
 * @author UEG
 */
public class ApiSwaggerConfig {

	public static final String SWAGGER_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0";

	public static final String SWAGGER_LICENSE = "Apache License 2.0";
	public static final String BEARER_AUTH = "bearerAuth";

	@Value("${app.api.swagger.title}")
	private String title;

	@Value("${app.api.base}")
	private String path;

	@Value("${app.api.version}")
	private String version;

	@Value("${app.api.swagger.base-package}")
	private String basePackage;


	@Bean
	public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
		return new OpenAPI()
				.components(new Components().addSecuritySchemes(BEARER_AUTH,
								new SecurityScheme()
										.name(BEARER_AUTH)
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.in(SecurityScheme.In.HEADER)
						)
				)
				.info(new Info()
						.title(this.title)
						.version(appVersion)
						.license(new License()
								.name(SWAGGER_LICENSE)
								.url(SWAGGER_LICENSE_URL)
						)
				)
				.security(Arrays.asList(new io.swagger.v3.oas.models.security.SecurityRequirement().addList(BEARER_AUTH)));
	}

}
