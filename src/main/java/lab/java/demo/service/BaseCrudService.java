package lab.java.demo.service;

import java.util.List;
import java.util.Optional;

/**
 * Generic base service for simple CRUD-style operations.
 * Concrete services only need to plug in how they talk to
 * their specific repository.
 */
public abstract class BaseCrudService<T, ID> {

    /**
     * Methods that delegate to the concrete repository.
     */
    protected abstract List<T> rawFindAll();
    protected abstract Optional<T> rawFindById(ID id);
    protected abstract T rawSave(T entity);
    protected abstract boolean rawDeleteById(ID id);

    // ---- Reusable CRUD API ----

    public List<T> findAll() {
        return rawFindAll();
    }

    public Optional<T> findById(ID id) {
        return rawFindById(id);
    }

    public T save(T entity) {
        return rawSave(entity);
    }

    public boolean deleteById(ID id) {
        return rawDeleteById(id);
    }
}
