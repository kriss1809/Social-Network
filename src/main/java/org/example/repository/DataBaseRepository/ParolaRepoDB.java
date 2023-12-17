package org.example.repository.DataBaseRepository;


import org.example.domain.*;
import org.example.domain.validators.Validator;
import org.example.exceptions.RepositoryException;

import java.sql.*;
import java.util.*;

public class ParolaRepoDB implements PagingRepository<Long, Parola>{

    protected Validator validator;
    protected DB_Access data;
    protected String table;

    public ParolaRepoDB(Validator validator, DB_Access data, String table) {
        this.validator = validator;
        this.data = data;
        this.table = table;
    }

    private Parola getParolaFromStatement(ResultSet resultSet) throws SQLException
    {
        Long id = resultSet.getLong("id");
        String username = resultSet.getString("username");
        String parola = resultSet.getString("parola");
        Parola p = new Parola(parola, username);
        p.setId(id);
        return p;
    }

    public Optional<Parola> findOne(Tuple<String,String> id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findOneStatement="SELECT * FROM parole" + " WHERE (username=? and parola=?);";
        try
        {
            PreparedStatement statement=data.createStatement(findOneStatement);
            statement.setString(1,id.getLeft());
            statement.setString(2,id.getRight());
            ResultSet resultSet=statement.executeQuery();
            if(resultSet.next())
            {
               Parola p = getParolaFromStatement(resultSet);
                return Optional.of(p);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Optional<Parola> findOne(Long aLong) {
        return Optional.empty();
    }

    public Iterable<Parola> findAll() {
        String findAllStatement="SELECT * FROM parole";
        Set<Parola> parole = new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Parola p = getParolaFromStatement(resultSet);
                parole.add(p);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return parole;
    }


    public Optional<Parola> save(Parola entity) {
        String insertSQL="INSERT INTO parole(username, parola) values(?,?)";
        try
        {
            PreparedStatement statement= data.createStatement(insertSQL);
            statement.setString(1,entity.getUsername());
            statement.setString(2,entity.getParola());

            int response=statement.executeUpdate();
            return response==0?Optional.of(entity):Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<Parola> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Parola> update(Parola entity) {
        return Optional.empty();
    }

    @Override
    public Page<Parola> findAllPage(Pageable pageable) {
        return null;
    }
}

