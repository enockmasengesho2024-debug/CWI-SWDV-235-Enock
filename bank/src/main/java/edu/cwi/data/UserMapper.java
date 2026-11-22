package edu.cwi.data;

import edu.cwi.User;
import java.util.List;
import org.apache.ibatis.annotations.*;

public interface UserMapper {
    String COLUMNS = "user_id AS id, username, password, email, first_name, last_name, role";

    @Select("SELECT " + COLUMNS + " FROM users ORDER BY user_id")
    List<User> findAll();

    @Select("SELECT " + COLUMNS + " FROM users WHERE user_id = #{id}")
    User findById(long id);

    @Select("SELECT " + COLUMNS + " FROM users WHERE user_id = #{id} FOR UPDATE")
    User lockById(long id);

    @Select("SELECT " + COLUMNS + " FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("""
        INSERT INTO users (username, password, email, first_name, last_name, role)
        VALUES (#{username}, #{password}, #{email}, #{firstName}, #{lastName}, #{role})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "user_id")
    int insert(User user);

    @Update("""
        UPDATE users SET username=#{username}, password=#{password}, email=#{email},
        first_name=#{firstName}, last_name=#{lastName}, role=#{role}
        WHERE user_id=#{id}
        """)
    int update(User user);

    @Delete("DELETE FROM users WHERE user_id=#{id}")
    int delete(long id);
}
