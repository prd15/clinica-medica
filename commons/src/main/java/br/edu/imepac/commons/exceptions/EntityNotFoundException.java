package br.edu.imepac.commons.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Object id) {
        super(entity + " não encontrado com id: " + id);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
