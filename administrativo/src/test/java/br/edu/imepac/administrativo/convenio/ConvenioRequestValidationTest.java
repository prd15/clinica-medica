package br.edu.imepac.administrativo.convenio;

import br.edu.imepac.administrativo.convenio.dto.ConvenioRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConvenioRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void cnpjComMaisDe18Chars_deveViolacaoSize() {
        ConvenioRequest request = new ConvenioRequest("Unimed", "Desc", "12.345.678/0001-99999", "(34)99999-0000", true);

        Set<ConstraintViolation<ConvenioRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cnpj")),
                "Esperava violacao no campo cnpj");
    }

    @Test
    void cnpjComExatamente18Chars_semViolacao() {
        ConvenioRequest request = new ConvenioRequest("Unimed", "Desc", "12.345.678/0001-99", "(34)99999-0000", true);

        Set<ConstraintViolation<ConvenioRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("cnpj")),
                "Nao esperava violacao no campo cnpj");
    }

    @Test
    void cnpjVazio_deveViolacaoNotBlank() {
        ConvenioRequest request = new ConvenioRequest("Unimed", "Desc", "", "(34)99999-0000", true);

        Set<ConstraintViolation<ConvenioRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cnpj")),
                "Esperava violacao NotBlank no campo cnpj");
    }
}
