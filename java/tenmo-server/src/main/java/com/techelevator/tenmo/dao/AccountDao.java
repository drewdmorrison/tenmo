package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getBalance(int userId);
    BigDecimal addToBalance(BigDecimal amountToTransfer, int accountId);
    BigDecimal subtractFromBalance(BigDecimal amountToTransfer, int accountId);
    Account findAccountByUserId(int userId);
    Account findAccountByAccountId(int accountId);
}
