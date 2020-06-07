package lab04.database;

import lab04.entities.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoCategory {

    private final Connection connection;

    DaoCategory(final Connection connection) {
        this.connection = connection;
    }

    public int insertCategory(final Category category) {
        try (PreparedStatement insertStatement = connection.prepareStatement(
                "insert into 'categories'('title', 'description') values (?, ?)")) {
            insertStatement.setString(1, category.getTitle());
            insertStatement.setString(2, category.getDescription());
            insertStatement.execute();

            final ResultSet result = insertStatement.getGeneratedKeys();
            return result.getInt("last_insert_rowid()");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert category!", e);
        }
    }

    public Category getCategory(String title) {
        try (final Statement statement = connection.createStatement()) {

            final String    query     = "SELECT * FROM 'categories' WHERE title = '" + title + "'";
            final ResultSet resultSet = statement.executeQuery(query);

            return resultSet.next() ? new Category(
                    resultSet.getString("title"),
                    resultSet.getString("description"))
                    : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get category!", e);
        }
    }


    public List<Category> getCategoryList(final int page, final int size) {
        try (final Statement statement = connection.createStatement()) {

            final String query = "SELECT * FROM 'categories' LIMIT " + size + " OFFSET " + page * size;
            final ResultSet resultSet = statement.executeQuery(query);


            final List<Category> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getString("title"),
                        resultSet.getString("description")));
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get list!", e);
        }
    }

    public void update(String updateColumnName, String newValue, String searchColumnName, String searchValue){
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("update 'categories' set " + updateColumnName + " = '" + newValue +
                    "' where " + searchColumnName + " = '" + searchValue + "'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table!", e);
        }
    }

    public void delete(final String title) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from 'categories' where title = '" + title + "'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete by title!", e);
        }
    }

    public void deleteAll(){
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from 'categories'");
            statement.executeUpdate("delete from sqlite_sequence where name='categories'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all!", e);
        }
    }


}
