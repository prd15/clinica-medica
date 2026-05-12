package br.edu.imepac.commons.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    // instancia compartilhada por todos os controllers — thread-safe depois de configurada
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        // estrategia padrao ja protege campos sem par no destino,
        // mas deixando explicito pra nao ter duvida na revisao de codigo
        mapper.getConfiguration().setSkipNullEnabled(true);
        return mapper;
    }
}
