package org.example.repository.DataBaseRepository;

import org.example.domain.*;
import org.example.domain.validators.Validator;
import org.example.exceptions.RepositoryException;
import org.example.utils.events.FriendshipStatusType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class InvitationRepoDB implements PagingRepository<Tuple<Long,Long>, Invitatie> {
    protected Validator validator;
    protected DB_Access data;
    protected String table;

    public InvitationRepoDB(Validator validator, DB_Access data, String table) {
        this.validator = validator;
        this.data = data;
        this.table = table;
    }

    private Invitatie getInvitationFromStatement(ResultSet resultSet) throws SQLException
    {
        Long id1 = resultSet.getLong("id_user1");
        Long id2 = resultSet.getLong("id_user2");
        String statusString = resultSet.getString("status");
        FriendshipStatusType status = FriendshipStatusType.valueOf(statusString);

        Invitatie inv = new Invitatie(id1, id2, status);
        return inv;
    }

    public Optional<Invitatie> findOne(Tuple<Long,Long> id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findOneStatement="SELECT * FROM invitations" + " WHERE (id_user1=? and id_user2=?);";
        try
        {
            PreparedStatement statement=data.createStatement(findOneStatement);
            statement.setLong(1,id.getLeft());
            statement.setLong(2,id.getRight());
            ResultSet resultSet=statement.executeQuery();
            if(resultSet.next())
            {
                Invitatie inv = getInvitationFromStatement(resultSet);
                return Optional.of(inv);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Invitatie> findAllByReceiverId(Long id) {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is  null!");
        }
        String findAllStatement="SELECT * FROM invitations" + " WHERE (id_user2=?);";
        Set<Invitatie> invitatii = new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            statement.setLong(1,id);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Invitatie i = getInvitationFromStatement(resultSet);
                invitatii.add(i);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return invitatii;
    }

    public Iterable<Invitatie> findAll() {
        String findAllStatement="SELECT * FROM invitations";
        Set<Invitatie> invitatii = new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Invitatie i = getInvitationFromStatement(resultSet);
                invitatii.add(i);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return invitatii;
    }

    @Override
    public Page<Invitatie> findAllPage(Pageable pageable)
    {
        String findAllPageStatement="SELECT * FROM invitations LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) AS count FROM invitations";
        List<Invitatie> inv = new ArrayList<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllPageStatement);
            statement.setInt(1, pageable.getPageSize());
            statement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
                inv.add(getInvitationFromStatement(resultSet));

            PreparedStatement statementcount = data.createStatement(count);
            ResultSet resultSetCount = statementcount.executeQuery();
            int totalCount = 0;
            if(resultSetCount.next()) {
                totalCount = resultSetCount.getInt("count");
            }

            return new Page<>(inv, totalCount);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Optional<Invitatie> save(Invitatie entity) {
        String insertSQL="INSERT INTO invitations(id_user1, id_user2, status) values(?,?,?)";
        try
        {
            PreparedStatement statement= data.createStatement(insertSQL);
            statement.setLong(1, entity.getId1());
            statement.setLong(2,entity.getId2());
            statement.setString(3,entity.getStatus());
            int response=statement.executeUpdate();
            return response==0?Optional.of(entity):Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RepositoryException(e);
        }
    }

    public Optional<Invitatie> update(Invitatie entity) {
        if (entity == null) {
            throw new RepositoryException("Enitity must not be null");
        }
        String updateStatement = "UPDATE invitations set status=?" +
                " where (id_user1=? and id_user2=?);";
        try {
            PreparedStatement statement = data.createStatement(updateStatement);
            statement.setString(1,entity.getStatus());
            statement.setLong(2,entity.getId1());
            statement.setLong(3,entity.getId2());
            int response=statement.executeUpdate();
            return response==0? Optional.of(entity):Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Optional<Invitatie> delete(Tuple<Long, Long> longLongTuple) {
        return Optional.empty();
    }
}
