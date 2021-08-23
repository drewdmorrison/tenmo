package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Foo;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {

    @Autowired
    private TransferDao transferDao;

    @Autowired
    private UserDao userDao;

    @RequestMapping(path = "/account/transfers/{userId}", method = RequestMethod.GET)
    public List<Transfer> getAllTransfers(@PathVariable int userId) {
        return transferDao.getAllTransfers(userId);
    }

    @RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
    public Transfer getTransfer(@PathVariable int transferId) {
        return transferDao.getTransferById(transferId);
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public String sendTransfer(@RequestBody Transfer transfer) {
        return transferDao.sendTransfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
    }

    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public String requestTransfer(@RequestBody Transfer transfer) {
        return transferDao.requestTransfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
    }

    @RequestMapping(path = "/request/{userId}", method = RequestMethod.GET)
    public List getPendingRequest(@PathVariable int userId) {
        return transferDao.getPendingRequests(userId);
    }

    @RequestMapping(path = "transfer/status/{transferStatusId}", method = RequestMethod.PUT)
    public String updateTransferRequest(@RequestBody Transfer transfer, @PathVariable int transferStatusId) {
        return transferDao.updateTransferRequest(transfer, transferStatusId);
    }
}
