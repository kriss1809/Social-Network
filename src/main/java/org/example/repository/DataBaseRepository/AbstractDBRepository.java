package org.example.repository.DataBaseRepository;

import org.example.domain.Entity;
import org.example.domain.validators.Validator;
import org.example.repository.Repository;

import java.util.Optional;

public abstract class AbstractDBRepository <ID, E extends Entity<ID>> implements Repository<ID, E>
{
    protected Validator validator;
    protected DB_Access data;
    protected String table;

    public AbstractDBRepository(Validator validator, DB_Access data, String table) {
        this.validator = validator;
        this.data = data;
        this.table = table;
    }
    @Override
    public  abstract Optional<E> findOne(ID id);

    @Override
    public abstract Iterable<E> findAll();

    @Override
    public abstract Optional<E> save(E entity) ;

    @Override
    public  abstract Optional<E> delete(ID id);

    @Override
    public abstract Optional<E> update(E entity) ;
}
