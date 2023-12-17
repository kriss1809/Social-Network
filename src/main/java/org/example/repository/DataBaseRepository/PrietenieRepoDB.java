package org.example.repository.DataBaseRepository;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.domain.Utilizator;
import org.example.domain.validators.Validator;
import org.example.exceptions.RepositoryException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class PrietenieRepoDB extends AbstractDBRepository<Tuple<Long, Long>, Prietenie> implements PagingRepository<Tuple<Long, Long>, Prietenie>
{
    public PrietenieRepoDB(Validator validator, DB_Access data, String table) {
        super(validator, data, table);
    }

    private Prietenie getFriendshipFromStatement(ResultSet resultSet) throws SQLException
    {
        Long id_user1 = resultSet.getLong("id_user1");
        Long id_user2 = resultSet.getLong("id_user2");
        String firstName1 = resultSet.getString("firstNameU1");
        String lastName1 = resultSet.getString("lastNameU1");
        String firstName2 = resultSet.getString("firstNameU2");
        String lastName2 = resultSet.getString("lastNameU2");

        Utilizator user1 = new Utilizator(firstName1, lastName1, "username1");
        user1.setId(id_user1);
        Utilizator user2 = new Utilizator(firstName2, lastName2, "username2");
        user2.setId(id_user2);
        Timestamp friendsfrom = resultSet.getTimestamp("friendsfrom");
        Prietenie friendship = new Prietenie(user1, user2, friendsfrom.toLocalDateTime());
        return friendship;
    }

    @Override
    public Optional<Prietenie> findOne(Tuple<Long,Long> id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findOneStatement="SELECT * FROM  GetFriendshipInformation()" +
                " WHERE (id_user1=? and id_user2=?)" +
                " or (id_user2=? and id_user1=?) ;";
        try
        {
            PreparedStatement statement=data.createStatement(findOneStatement);
            statement.setLong(1,id.getLeft());
            statement.setLong(2,id.getRight());
            statement.setLong(3,id.getLeft());
            statement.setLong(4,id.getRight());
            ResultSet resultSet=statement.executeQuery();
            if(resultSet.next())
            {
                Prietenie friendship=getFriendshipFromStatement(resultSet);
                return Optional.of(friendship);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Prietenie> findAll() {
        String findAllStatement="SELECT * FROM GetFriendshipInformation()";
        Set<Prietenie> friendships=new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Prietenie friendship=getFriendshipFromStatement(resultSet);
                friendships.add(friendship);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return friendships;
    }

    @Override
    public Page<Prietenie> findAllPage(Pageable pageable)
    {
        String findAllPageStatement="SELECT * FROM GetFriendshipInformation() LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) AS count FROM GetFriendshipInformation()";
        List<Prietenie> prietenii = new ArrayList<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllPageStatement);
            statement.setInt(1, pageable.getPageSize());
            statement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
                prietenii.add(getFriendshipFromStatement(resultSet));

            PreparedStatement statementcount = data.createStatement(count);
            ResultSet resultSetCount = statementcount.executeQuery();
            int totalCount = 0;
            if(resultSetCount.next()) {
                totalCount = resultSetCount.getInt("count");
            }

            return new Page<>(prietenii, totalCount);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  Optional<Prietenie> delete(Tuple<Long,Long> id)
    {
        Optional<Prietenie> entity=findOne(id);
        if(id!=null)
        {
            String deleteStatement="DELETE FROM friendship where (id_user1=? and id_user2=?) or (id_user2=? and id_user1=?)";
            int response=0;
            try
            {
                PreparedStatement statement=data.createStatement(deleteStatement);
                statement.setLong(1,id.getLeft());
                statement.setLong(2,id.getRight());
                statement.setLong(3,id.getLeft());
                statement.setLong(4,id.getRight());

                if (entity.isPresent())
                {
                    response=statement.executeUpdate();
                }
                return response==0? Optional.empty() : entity;

            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            throw  new IllegalArgumentException("ID cannot be null!");
        }
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        String insertSQL="INSERT INTO friendship(id_user1,id_user2,friendsfrom) values(?,?,?)";
        try
        {
            PreparedStatement statement= data.createStatement(insertSQL);
            statement.setLong(1,entity.getId().getLeft());
            statement.setLong(2,entity.getId().getRight());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            int response=statement.executeUpdate();
            return response==0?Optional.of(entity):Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RepositoryException(e);
        }

    }


    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        if (entity == null) {
            throw new RepositoryException("Enitity must not be null");
        }
        String updateStatement = "UPDATE friends set friendsfrom=?" +
                " where (id_user1=? and id_user2=?)" +
                "or (id_user2=? and id_user1=?)";
        try {
            PreparedStatement statement = data.createStatement(updateStatement);
            statement.setTimestamp(1,Timestamp.valueOf(entity.getDate()));
            statement.setLong(2,entity.getId().getLeft());
            statement.setLong(3,entity.getId().getRight());
            statement.setLong(4,entity.getId().getLeft());
            statement.setLong(5,entity.getId().getRight());
            int response=statement.executeUpdate();
            return response==0? Optional.of(entity):Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }

    }

}
