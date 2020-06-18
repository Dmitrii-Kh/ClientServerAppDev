package lab04.database;

import lab04.entities.User;

import java.sql.*;

public class DaoUser {

    private final Connection connection;

    public DaoUser(final Connection connection) { this.connection = connection; }



    public int insertUser(final User user) {
        try (final PreparedStatement insertStatement = connection.prepareStatement("insert into 'users'('login', 'password', 'role') values (?, ?, ?)")) {
            insertStatement.setString(1, user.getLogin());
            insertStatement.setString(2, user.getPassword());
            insertStatement.setString(3, user.getRole());

            insertStatement.execute();

            final ResultSet result = insertStatement.getGeneratedKeys();
            return result.getInt("last_insert_rowid()");
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert user", e);
        }
    }



    public User getByLogin(final String login) {
        try (final PreparedStatement getStatement = connection.prepareStatement("select * from 'users' where login = ?")) {
            getStatement.setString(1, login);
            final ResultSet resultSet = getStatement.executeQuery();

            if (resultSet.next()) {
                return User.builder()
                        .id(resultSet.getInt("id"))
                        .login(resultSet.getString("login"))
                        .password(resultSet.getString("password"))
                        .role(resultSet.getString("role"))
                        .build();
            }
            return null;
        } catch (final SQLException e) {
            throw new RuntimeException("Can't get user by login: " + login, e);
        }
    }

    public void deleteAll() {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from 'users'");
            statement.executeUpdate("delete from sqlite_sequence where name='users'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all users!", e);
        }
    }

}
