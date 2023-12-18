package org.example.repository.DataBaseRepository;

import org.example.domain.*;
import org.example.domain.validators.Validator;
import org.example.exceptions.RepositoryException;

import java.sql.*;
import java.util.*;

public class MessageRepoDB implements PagingRepository<Long, Message>{

    protected Validator validator;
    protected DB_Access data;
    protected String table;

    public MessageRepoDB(Validator validator, DB_Access data, String table) {
        this.validator = validator;
        this.data = data;
        this.table = table;
    }

    private Message getMessageFromStatement(ResultSet resultSet) throws SQLException
    {
        Long id_message = resultSet.getLong("id");
        Long id_sender = resultSet.getLong("sender_id");
        Long id_receiver = resultSet.getLong("receiver_id");
        String msg = resultSet.getString("message");
        Timestamp data = resultSet.getTimestamp("data");
        Long raspuns = resultSet.getLong("raspuns_la");

        Message m = new Message(id_sender, id_receiver, msg, data.toLocalDateTime());
        m.setId(id_message);
        m.setReply(raspuns);
        return m;
    }

    public Optional<Message> findOne(Tuple<Long,Long> id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findOneStatement="SELECT * FROM  messages" + " WHERE (sender_id=? and receiver_id=?);";
        try
        {
            PreparedStatement statement=data.createStatement(findOneStatement);
            statement.setLong(1,id.getLeft());
            statement.setLong(2,id.getRight());
            ResultSet resultSet=statement.executeQuery();
            if(resultSet.next())
            {
                Message msg = getMessageFromStatement(resultSet);
                return Optional.of(msg);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Optional<Message> find(Long id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findOneStatement="SELECT * FROM  messages" + " WHERE id=?;";
        try
        {
            PreparedStatement statement=data.createStatement(findOneStatement);
            statement.setLong(1,id);
            ResultSet resultSet=statement.executeQuery();
            if(resultSet.next())
            {
                Message msg = getMessageFromStatement(resultSet);
                return Optional.of(msg);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> findOne(Long id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findOneStatement="SELECT * FROM messages  WHERE id = ?";
        try
        {
            PreparedStatement statement=data.createStatement(findOneStatement);
            statement.setLong(1,id);
            ResultSet resultSet=statement.executeQuery();
            if(resultSet.next())
            {
                Message m  = getMessageFromStatement(resultSet);
                return Optional.of(m);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Message> findAll() {
        String findAllStatement="SELECT * FROM messages";
        Set<Message> msgs = new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
               Message m = getMessageFromStatement(resultSet);
               msgs.add(m);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return msgs;
    }

    @Override
    public Page<Message> findAllPage(Pageable pageable)
    {
        String findAllPageStatement="SELECT * FROM messages LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) AS count FROM messages";
        List<Message> msg = new ArrayList<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllPageStatement);
            statement.setInt(1, pageable.getPageSize());
            statement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
                msg.add(getMessageFromStatement(resultSet));

            PreparedStatement statementcount = data.createStatement(count);
            ResultSet resultSetCount = statementcount.executeQuery();
            int totalCount = 0;
            if(resultSetCount.next()) {
                totalCount = resultSetCount.getInt("count");
            }

            return new Page<>(msg, totalCount);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Optional<Message> save(Message entity) {
        String insertSQL="INSERT INTO messages(sender_id,receiver_id, message, data, raspuns_la) values(?,?,?,?,?)";
        try
        {
            PreparedStatement statement= data.createStatement(insertSQL);
            statement.setLong(1,entity.getFrom());
            statement.setLong(2,entity.getTo());
            statement.setString(3,entity.getMessage());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getData()));
            if(entity.getReply() != null)
                statement.setLong(5,entity.getReply());
            else
                statement.setLong(5,-1);
            int response=statement.executeUpdate();
            return response==0?Optional.of(entity):Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    public  Optional<Message> delete(Tuple<Long,Long> id)
    {
        Optional<Message> entity = findOne(id);
        if(id!=null)
        {
            String deleteStatement="DELETE FROM messages where (sender_id=? and receiver_id=?)";
            int response=0;
            try
            {
                PreparedStatement statement=data.createStatement(deleteStatement);
                statement.setLong(1,id.getLeft());
                statement.setLong(2,id.getRight());

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

    public Optional<Message> update(Message entity) {
        if (entity == null) {
            throw new RepositoryException("Enitity must not be null");
        }
        String updateStatement = "UPDATE messages set message=?" +
                " where (sender_id=? and receiver_id=?);";
        try {
            PreparedStatement statement = data.createStatement(updateStatement);
            statement.setString(1,entity.getMessage());
            statement.setLong(2,entity.getFrom());
            statement.setLong(3,entity.getTo());
            int response=statement.executeUpdate();
            return response==0? Optional.of(entity):Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    public Iterable<Message> conversatii_utilizatori(Long id_user1, Long id_user2)
    {
        String findAllStatement="SELECT * FROM messages WHERE (sender_id=? and receiver_id=?) or (sender_id=? and receiver_id=?) ORDER BY data";
        List<Message> msgs = new ArrayList<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            statement.setLong(1,id_user1);
            statement.setLong(2,id_user2);
            statement.setLong(3,id_user2);
            statement.setLong(4,id_user1);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Message m = getMessageFromStatement(resultSet);
                System.out.println(m.getData());
                msgs.add(m);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return msgs;
    }
}
