package br.edu.imepac.commons.config;

import br.edu.imepac.commons.exceptions.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ModelMapperConfig.class, GlobalExceptionHandler.class})
public class CommonsAutoConfiguration {
}
