package centollo.model.application;

public class NotFoundException extends RuntimeException {

    public NotFoundException(Class<?> entity, Object criteria) {
        super(entity.getSimpleName() + " not found by " + criteria);
    }
}
