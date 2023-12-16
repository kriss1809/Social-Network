package org.example.repository;


import org.example.domain.Entity;
import org.example.domain.validators.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID,E> {
    private Validator<E> validator;
    Map<ID,E> entities;

    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities=new HashMap<ID,E>();
    }

    public Validator<E> getValidator()
    {
        return validator;
    }


    public void setValidator(Validator<E> validator)
    {
        this.validator = validator;
    }


    public Map<ID, E> getEntities()
    {
        return entities;
    }


    public void setEntities(Map<ID, E> entities)
    {
        this.entities = entities;
    }


    @Override
    public Optional<E> findOne(ID id) {
        Predicate<ID> idNotNull = idValue -> idValue != null;
        if (!idNotNull.test(id))
            throw new IllegalArgumentException("ID-ul nu trebuie sa fie null");
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) {
        Predicate<E> entityNotNull = e -> e != null;
        Predicate<ID> idNotNull = id -> id != null;

        if (!entityNotNull.test(entity)) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        if (!idNotNull.test(entity.getId())) {
            throw new IllegalArgumentException("ID must not be null");
        }
        validator.validate(entity);
        return Optional.ofNullable(entities.putIfAbsent(entity.getId(), entity));
    }

    @Override
    public Optional<E> delete(ID id) {
        Predicate<ID> idNotNull = idValue -> idValue != null;
        if (!idNotNull.test(id))
            throw new IllegalArgumentException("ID must not be null");
        return Optional.ofNullable(entities.remove(id));
    }

    @Override
    public Optional<E> update(E entity) {
        Predicate<E> entityNotNull = e -> e != null;
        Predicate<ID> idNotNull = id -> id != null;

        if (!entityNotNull.test(entity)) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        if (!idNotNull.test(entity.getId())) {
            throw new IllegalArgumentException("ID must not be null");
        }

        validator.validate(entity);

        return Optional.ofNullable(entities.compute(entity.getId(), (id, existingEntity) -> {
            if (existingEntity != null) {
                // Entity with that ID already exists, update it and return the old one
                return entity;
            } else {
                return existingEntity;
            }
        }));
    }
}
