package edu.cwi.data;

import edu.cwi.Transaction;
import java.util.List;
import org.apache.ibatis.annotations.*;

public interface TransactionMapper {
    String COLUMNS = "transaction_id AS id, account_id, transaction_type, amount, description, created_at";

    @Select("SELECT " + COLUMNS + " FROM transactions WHERE account_id=#{accountId} ORDER BY transaction_id")
    List<Transaction> findByAccountId(long accountId);

    @Select("SELECT " + COLUMNS + " FROM transactions WHERE transaction_id=#{id}")
    Transaction findById(long id);

    @Select("SELECT " + COLUMNS + " FROM transactions WHERE transaction_id=#{id} FOR UPDATE")
    Transaction lockById(long id);

    @Insert("""
        INSERT INTO transactions (account_id, transaction_type, amount, description)
        VALUES (#{accountId}, #{transactionType}, #{amount}, #{description})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "transaction_id")
    int insert(Transaction transaction);

    @Update("""
        UPDATE transactions SET transaction_type=#{transactionType}, amount=#{amount},
        description=#{description} WHERE transaction_id=#{id}
        """)
    int update(Transaction transaction);

    @Delete("DELETE FROM transactions WHERE transaction_id=#{id}")
    int delete(long id);

    @Delete("""
        DELETE FROM transactions
        WHERE account_id IN (SELECT account_id FROM accounts WHERE user_id=#{userId})
        """)
    int deleteByUserId(long userId);
}
