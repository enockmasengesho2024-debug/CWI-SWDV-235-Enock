package edu.cwi.data;

import edu.cwi.Account;
import java.util.List;
import org.apache.ibatis.annotations.*;

public interface AccountMapper {
    String COLUMNS = "account_id AS id, user_id, account_type, balance";

    @Select("SELECT " + COLUMNS + " FROM accounts WHERE user_id=#{userId} ORDER BY account_id")
    List<Account> findByUserId(long userId);

    @Select("SELECT " + COLUMNS + " FROM accounts WHERE account_id=#{id} FOR UPDATE")
    Account lockById(long id);

    @Insert("""
        INSERT INTO accounts (user_id, account_type, balance)
        VALUES (#{userId}, #{accountType}, #{balance})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "account_id")
    int insert(Account account);

    @Update("UPDATE accounts SET balance=#{balance} WHERE account_id=#{id}")
    int updateBalance(Account account);

    @Delete("DELETE FROM accounts WHERE user_id=#{userId}")
    int deleteByUserId(long userId);
}

