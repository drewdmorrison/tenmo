package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.Principal;

@Component
public class JdbcAccountDao implements AccountDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    public JdbcAccountDao() {
    }

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getBalance(int userId) {
        BigDecimal balance = null;
        String sql = "SELECT balance FROM accounts WHERE user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        if (results.next()) {
            balance = results.getBigDecimal("balance");
        }

        return balance;
    }

    @Override
    public BigDecimal addToBalance(BigDecimal amountToTransfer, int accountId) {
        Account account = findAccountByAccountId(accountId);
        BigDecimal newBalance = account.getBalance().add(amountToTransfer);
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?;";
        jdbcTemplate.update(sql, newBalance, accountId);

        return account.getBalance();
    }

    @Override
    public BigDecimal subtractFromBalance(BigDecimal amountToTransfer, int accountId) {
        Account account = findAccountByAccountId(accountId);
        BigDecimal newBalance = account.getBalance().subtract(amountToTransfer);
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?;";
        jdbcTemplate.update(sql, newBalance, accountId);

        return account.getBalance();
    }

    @Override
    public Account findAccountByUserId (int userId) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM accounts WHERE user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        if (results.next()) {
           account = mapRowToAccount(results);
        }
        return account;
    }

    @Override
    public Account findAccountByAccountId(int accountId) {
        Account account = null;
        String sql = "SELECT * FROM accounts WHERE account_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);

        if (results.next()) {
            account = mapRowToAccount(results);
        }

        return account;
    }

    private Account mapRowToAccount(SqlRowSet results) {
        Account account = new Account();
        account.setAccountId(results.getInt("account_id"));
        account.setUserId(results.getInt("user_id"));
        account.setBalance(results.getBigDecimal("balance"));

        return account;
    }

}
