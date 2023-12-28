package org.example.repository.DataBaseRepository;

import org.example.domain.Utilizator;
import org.example.domain.validators.Validator;
import org.example.exceptions.RepositoryException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UtilizatorRepoDB extends AbstractDBRepository<Long, Utilizator> implements PagingRepository<Long, Utilizator>
{
    public UtilizatorRepoDB(Validator validator, DB_Access data, String table) {
        super(validator, data, table);
    }

    private Optional<Utilizator> getUtilizator(ResultSet r, Long id) throws SQLException
    {
        String firstName = r.getString("first_name");
        String lastName = r.getString("last_name");
        String username = r.getString("username");
        Utilizator u = new Utilizator(firstName, lastName, username);
        u.setId(r.getLong("id"));
        fetchFriendsForUtilizator(u);
        return Optional.of(u);
    }

    private void fetchFriendsForUtilizator(Utilizator utilizator) {
        String fetchFriendsStatement =
                "SELECT u.id, u.first_name, u.last_name, u.username " +
                        "FROM friendship f " +
                        "JOIN users u ON (f.id_user1 = u.id OR f.id_user2 = u.id) " +
                        "WHERE f.id_user1 = ? OR f.id_user2 = ?";

        try {
            PreparedStatement st = data.createStatement(fetchFriendsStatement);
            st.setLong(1, utilizator.getId());
            st.setLong(2, utilizator.getId());

            ResultSet resultSet = st.executeQuery();

            while (resultSet.next()) {
                Long friendId = resultSet.getLong("id");
                String friendFirstName = resultSet.getString("first_name");
                String friendLastName = resultSet.getString("last_name");
                String friendUsername= resultSet.getString("username");

                Utilizator friend = new Utilizator(friendFirstName, friendLastName, friendUsername);
                friend.setId(friendId);

                if(friendId!=utilizator.getId())
                    utilizator.getFriends().add(friend);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public Optional<Utilizator> findOne(Long id)
    {
        if (id==null)
        {
            throw new IllegalArgumentException("Id of entity is null!");
        }

        String findOneStatement = "SELECT * FROM users WHERE id = ?";
        try{
            PreparedStatement st = data.createStatement(findOneStatement);
            st.setLong(1, id);
            ResultSet resultSet=st.executeQuery();
            if(resultSet.next())
                return getUtilizator(resultSet,id);
            return Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Optional<Utilizator> findOneUsername(String username)
    {
        if (username==null)
        {
            throw new IllegalArgumentException("Username is null!");
        }

        String findOneStatement = "SELECT * FROM users WHERE username = ?";
        try{
            PreparedStatement st = data.createStatement(findOneStatement);
            st.setString(1, username);
            ResultSet r=st.executeQuery();
            if(r.next()) {
                String firstName = r.getString("first_name");
                String lastName = r.getString("last_name");
                Utilizator u = new Utilizator(firstName, lastName, username);
                u.setId(r.getLong("id"));
               // fetchFriendsForUtilizator(u);
                return Optional.of(u);
            }
            return Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Utilizator> findAll() {
        String findAllStatement="SELECT * FROM users WHERE username NOT LIKE 'admin'";
        Set<Utilizator> users=new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Long id=resultSet.getLong("id");
                users.add(getUtilizator(resultSet,id).get());
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return users;
    }

    public Iterable<Utilizator> findAllNotInvitedByID(Long ID)
    {
        // utilizatorii care nu au primit nicio invitatie de la utilizatorul curent SAU nu au trimis nicio invitatie utilizatorului curent
        String findAllStatement="\n" +
                "SELECT * FROM users\n" +
                "WHERE users.username NOT LIKE 'admin' AND users.id != ? AND users.id NOT IN ((SELECT id_user2 FROM invitations WHERE id_user1 = ?) UNION (SELECT id_user1 FROM invitations WHERE id_user2 = ?) )";

        Set<Utilizator> users=new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            statement.setLong(1, ID);
            statement.setLong(2, ID);
            statement.setLong(3, ID);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                users.add(getUtilizator(resultSet,id).get());
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return users;
    }

    public Iterable<Utilizator> findAllPending(Long ID)
    {
        String findAllStatement="\n" +
                "SELECT * FROM users\n" +
                "WHERE users.username NOT LIKE 'admin' AND users.id != ? AND users.id IN (SELECT id_user1 FROM invitations WHERE id_user2 = ? AND status LIKE 'pending')";

        Set<Utilizator> users=new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            statement.setLong(1, ID);
            statement.setLong(2, ID);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                users.add(getUtilizator(resultSet,id).get());
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return users;
    }

    public Iterable<String> findAllFriendsForUser(Long ID)
    {
        String findAllStatement=
                "SELECT u.id, u.first_name, u.last_name, u.username " +
                        "FROM friendship f " +
                        "JOIN users u ON (f.id_user1 = u.id OR f.id_user2 = u.id) " +
                        "WHERE f.id_user1 = ? OR f.id_user2 = ? " +
                "EXCEPT " +
                "SELECT u.id, u.first_name, u.last_name, u.username " +
                "FROM users u WHERE u.id = ? ";

        Set<String> users=new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            statement.setLong(1, ID);
            statement.setLong(2, ID);
            statement.setLong(3, ID);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                users.add(getUtilizator(resultSet,id).get().getUsername());
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public Page<Utilizator> findAllPage(Pageable pageable)
    {
        String findAllPageStatement="SELECT * FROM users WHERE username NOT LIKE 'admin' LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) AS count FROM users";
        List<Utilizator> users = new ArrayList<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllPageStatement);
            statement.setInt(1, pageable.getPageSize());
            statement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                Long id=resultSet.getLong("id");
                users.add(getUtilizator(resultSet,id).get());
            }

            PreparedStatement statementcount = data.createStatement(count);
            ResultSet resultSetCount = statementcount.executeQuery();
            int totalCount = 0;
            if(resultSetCount.next()) {
                totalCount = resultSetCount.getInt("count");
            }

            return new Page<>(users, totalCount);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        String insertSQL="INSERT INTO users(first_name,last_name, username) values(?,?,?)";
        try
        {
            PreparedStatement statement= data.createStatement(insertSQL);
            statement.setString(1,entity.getFirstName());
            statement.setString(2,entity.getLastName());
            statement.setString(3, entity.getUsername());
            int response=statement.executeUpdate();
            return response==0?Optional.of(entity):Optional.empty();
        }
        catch (SQLException e)
        {
            throw new RepositoryException(e);
        }
    }

    @Override
    public  Optional<Utilizator> delete(Long id){
        Optional<Utilizator> entity=findOne(id);
        if(id!=null)
        {
            String deleteStatement="DELETE FROM users where id="+id;
            int response=0;
            try
            {
                PreparedStatement statement=data.createStatement(deleteStatement);

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
    public Optional<Utilizator> update(Utilizator entity) {
        if (entity == null) {
            throw new RepositoryException("Entity must not be null");
        }
        String updateStatement = "UPDATE users SET first_name=?, last_name=? WHERE username=?";
        try {
            PreparedStatement statement = data.createStatement(updateStatement);
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getUsername());
            int response = statement.executeUpdate();
            return response == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }


    public Iterable<Utilizator> utilizatori_string(String text) {
        String findAllStatement="SELECT * FROM users WHERE (first_name like '%" + text + "%' OR last_name like '%" + text + "%') AND username NOT LIKE 'admin'";
        Set<Utilizator> users=new HashSet<>();
        try
        {
            PreparedStatement statement= data.createStatement(findAllStatement);
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next())
            {
                Long id=resultSet.getLong("id");
                users.add(getUtilizator(resultSet,id).get());
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return users;
    }

}
