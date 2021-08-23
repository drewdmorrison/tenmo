package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private UserDao userDao;


    @Override
    public List<Transfer> getAllTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.*, u.username AS userFrom, v.username AS userTo " +
                     "FROM transfers t " +
                     "JOIN accounts a ON t.account_from = a.account_id " +
                     "JOIN accounts b ON t.account_to = b.account_id " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "JOIN users v ON b.user_id = v.user_id " +
                     "WHERE a.user_id = ? OR b.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
        while (results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }

        return transfers;
    }

    @Override
    public Transfer getTransferById(int transferID) {
        Transfer transfer = new Transfer();

        String sql = "SELECT t.*, u.username AS userFrom, v.username AS userTo, tt.transfer_type_desc, ts.transfer_status_desc " +
                "FROM transfers t " +
                "JOIN accounts a ON t.account_from = a.account_id " +
                "JOIN accounts b ON t.account_to = b.account_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "JOIN users v ON b.user_id = v.user_id " +
                "JOIN transfer_statuses ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN transfer_types tt ON t.transfer_type_id = tt.transfer_type_id " +
                "WHERE t.transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferID);
        if (results.next()) {
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }

    @Override
    public String sendTransfer(int userFrom, int userTo, BigDecimal amount) {
        if (userFrom == userTo) {
            System.out.println("You cannot send money to yourself.");
        }

        int accountFrom = accountDao.findAccountByUserId(userFrom).getAccountId();
        int accountTo = accountDao.findAccountByUserId(userTo).getAccountId();


        if (amount.compareTo(accountDao.getBalance(userFrom)) < 0 && amount.compareTo(new BigDecimal("0")) > 0) {
            String sql = "INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                         "VALUES(2, 2, ?, ?, ?);";
            jdbcTemplate.update(sql, accountFrom, accountTo, amount);
            accountDao.addToBalance(amount, accountTo);
            accountDao.subtractFromBalance(amount, accountFrom);
            return "Transfer Complete";
        } else {
            return "Transfer Failed";
        }
    }

    @Override
    public String requestTransfer(int userFrom, int userTo, BigDecimal amount) {
        if (userFrom == userTo) {
            return "You cannot request money from yourself.";
        }

        int accountFrom = accountDao.findAccountByUserId(userFrom).getAccountId();
        int accountTo = accountDao.findAccountByUserId(userTo).getAccountId();

        if (amount.compareTo(new BigDecimal("0")) > 0) {
            String sql = "INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                         "VALUES(1, 1, ?, ?, ?);";
            jdbcTemplate.update(sql, accountFrom, accountTo, amount);
            return "Request was submitted.";
        } else {
            return "Request Failed. Please try again.";
        }
    }

    @Override
    public List<Transfer> getPendingRequests(int userId) {
        int accountId = accountDao.findAccountByUserId(userId).getAccountId();

        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql = "SELECT t.*, u.username AS userFrom, v.username AS userTo " +
                     "FROM transfers t " +
                     "JOIN accounts a ON t.account_from = a.account_id " +
                     "JOIN accounts b ON t.account_to = b.account_id " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "JOIN users v ON b.user_id = v.user_id " +
                     "WHERE t.transfer_status_id = 1 AND (account_from = ? OR account_to = ?)";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
        while (results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            pendingTransfers.add(transfer);
        }
        return pendingTransfers;
    }

    @Override
    public String updateTransferRequest(Transfer transfer, int transferStatusId) {
        BigDecimal balance = accountDao.findAccountByAccountId(transfer.getAccountFrom()).getBalance();
        if (transferStatusId == 3) {
            String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?;";
            jdbcTemplate.update(sql, transferStatusId, transfer.getTransferId());
            return "Transfer Rejected.";
        }
        /*(accountDao.getBalance(transfer.getAccountFrom()).compareTo(transfer.getAmount()) >= 0)*/
        if (transferStatusId == 2 && balance.compareTo(transfer.getAmount()) >= 0) {
            String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?;";
            jdbcTemplate.update(sql, transferStatusId, transfer.getTransferId());
            accountDao.addToBalance(transfer.getAmount(), transfer.getAccountTo());
            accountDao.subtractFromBalance(transfer.getAmount(), transfer.getAccountFrom());
            return "Transfer Updated";
        } else {
            return "Transfer Failed to Update";
        }
    }

    public Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        try {
            transfer.setUserFrom(results.getString("userFrom"));
            transfer.setUserTo(results.getString("userTo"));
        } catch (Exception ex) {}
        try {
            transfer.setTransferType(results.getString("transfer_type_desc"));
            transfer.setTransferStatus(results.getString("transfer_status_desc"));
        } catch (Exception ex) {}

        return transfer;
    }

}
