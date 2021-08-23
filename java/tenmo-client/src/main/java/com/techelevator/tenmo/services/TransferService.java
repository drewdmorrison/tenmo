package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Scanner;

public class TransferService {
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    public TransferService(String url, AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
        BASE_URL = url;
    }

    public Transfer[] listTransfers() {
        Transfer[] transfers = null;
        try {
            transfers = restTemplate.exchange(BASE_URL + "account/transfers/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();
            System.out.println("--------------------------------------------------");
            System.out.println("Transfers");
            System.out.println("ID          From/To                         Amount");
            System.out.println("--------------------------------------------------");
            String fromOrTo = "";
            String userName = "";

            for (Transfer transfer : transfers) {
                if (currentUser.getUser().getUsername().equals(transfer.getUserTo())) {
                    fromOrTo = "From: ";
                    userName = transfer.getUserFrom();
                } else {
                    fromOrTo = "To: ";
                    userName = transfer.getUserTo();
                }
                System.out.println(transfer.getTransferId() + "\t\t" + fromOrTo + userName + "\t\t\t\t\t\t$" + transfer.getAmount());
            }
            System.out.println("------------------------------------------------------------");
            System.out.println("Please enter the transfer ID to view details (0 to cancel): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            int transferId = Integer.parseInt(input);
            if (transferId != 0) {
                boolean foundTransferId = false;
                for (Transfer transfer : transfers) {
                    if (transferId == transfer.getTransferId()) {
                        Transfer transferSelected = restTemplate.exchange(BASE_URL + "transfers/" + transfer.getTransferId(), HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
                        foundTransferId = true;
                        System.out.println("----------------------------------------");
                        System.out.println("Transfer Details");
                        System.out.println("----------------------------------------");
                        System.out.println("Transfer Id: " + transferSelected.getTransferId());
                        System.out.println("From: " + transferSelected.getUserFrom());
                        System.out.println("To: " + transferSelected.getUserTo());
                        System.out.println("Type: " + transferSelected.getTransferType());
                        System.out.println("Status: " + transferSelected.getTransferStatus());
                        System.out.println("Amount: $" + transfer.getAmount());
                    }
                }
                if (!foundTransferId) {
                    System.out.println("Not a valid Transfer Id");
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("There was an error getting the transfers list. Please try again");
        }
        return transfers;
    }

    public void sendBucks() {
        User[] users = null;
        Transfer transfer = new Transfer();

        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(BASE_URL + "userslist/", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------------");
            System.out.println("Users");
            System.out.println("ID\t\t\tName");
            System.out.println("-------------------------------------------------");

            for (User user : users) {
                if (!user.getId().equals(currentUser.getUser().getId())) {
                    System.out.println(user.getId() + "\t\t" + user.getUsername());
                }
            }
            System.out.println("-------------------------------------------------");
            System.out.println("Enter ID of user you are sending TE Bucks to (0 to cancel): ");
            String accountToStr = scanner.nextLine();
            int accountTo = Integer.parseInt(accountToStr);
            transfer.setAccountTo(accountTo);
            transfer.setAccountFrom(currentUser.getUser().getId());
            if (transfer.getAccountTo() != 0) {
                System.out.println("Enter Amount: ");
                try {
                    String amountStr = scanner.nextLine();
                    BigDecimal amount = new BigDecimal(amountStr);
                    transfer.setAmount(amount);
                } catch (NumberFormatException ex) {
                    System.out.println("There was an error with the amount entered.");
                }

                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.APPLICATION_JSON);
                headers2.setBearerAuth(currentUser.getToken());

                HttpEntity entity2 = new HttpEntity(transfer, headers2);

                String results = restTemplate.exchange(BASE_URL + "transfers", HttpMethod.POST, entity2, String.class).getBody();
                System.out.println(results);
            }
        } catch (Exception ex){
            //ex.printStackTrace();
            System.out.println("Error");
        }
    }

    public void requestBucks() {
        User[] users = null;
        Transfer transfer = new Transfer();

        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(BASE_URL + "userslist/", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------------");
            System.out.println("Users");
            System.out.println("ID\t\t\tName");
            System.out.println("-------------------------------------------------");

            for (User user : users) {
                if (!user.getId().equals(currentUser.getUser().getId())) {
                    System.out.println(user.getId() + "\t\t" + user.getUsername());
                }
            }
            System.out.println("-------------------------------------------------");
            System.out.println("Enter ID of user you are requesting TE Bucks from (0 to cancel): ");
            String accountFromStr = scanner.nextLine();
            int accountFrom = Integer.parseInt(accountFromStr);
            transfer.setAccountFrom(accountFrom);
            transfer.setAccountTo(currentUser.getUser().getId());

            if (transfer.getAccountFrom() != 0) {
                System.out.println("Enter Amount: ");
                try {
                    String amountStr = scanner.nextLine();
                    BigDecimal amount = new BigDecimal(amountStr);
                    transfer.setAmount(amount);
                } catch (NumberFormatException ex) {
                    System.out.println("There was an error with the amount entered.");
                }
                String results = restTemplate.exchange(BASE_URL + "request", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(results);
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("Invalid entry. Please try again.");
        }
    }

    public Transfer[] transferRequestList() {
        Transfer[] transfers = null;
        try {
            transfers = restTemplate.exchange(BASE_URL + "request/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();
            System.out.println("--------------------------------------------------");
            System.out.println("Pending Transfers");
            System.out.println("ID          From/To                         Amount");
            System.out.println("--------------------------------------------------");

            String fromOrTo = "";
            String userName = "";

            for (Transfer transfer : transfers) {
                if (currentUser.getUser().getUsername().equals(transfer.getUserTo())) {
                    fromOrTo = "To: ";
                    userName = transfer.getUserFrom();
                } else {
                    fromOrTo = "From: ";
                    userName = transfer.getUserTo();
                }
                System.out.println(transfer.getTransferId() + "\t\t" + fromOrTo + userName + "\t\t\t\t\t\t$" + transfer.getAmount());
            }
            System.out.println("------------------------------------------------------------");
            System.out.println("Please enter the ID to approve/rejects requests (0 to cancel): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            int transferId = Integer.parseInt(input);

            if (transferId != 0) {
                boolean foundTransferId = false;
                for (Transfer transfer : transfers) {
                    if (!transfer.getUserTo().equals(currentUser.getUser().getUsername())) {
                        if (transferId == transfer.getTransferId()) {
                            System.out.println("------------------------------------");
                            System.out.println(transfer.getTransferId() + "\t" + fromOrTo + userName + "\t\t\t\t$" + transfer.getAmount());
                            System.out.println("1. Approve");
                            System.out.println("2. Reject");
                            System.out.println("0. Cancel");
                            System.out.println("------------------------------------");
                            System.out.println("Please enter a choice: ");

                            try {
                                String choiceStr = scanner.nextLine();
                                int choice = Integer.parseInt(choiceStr) + 1;
                                if (choice != 1) {
                                    String results = restTemplate.exchange(BASE_URL + "transfer/status/" + choice, HttpMethod.PUT, makeTransferEntity(transfer), String.class).getBody();
                                    System.out.println(results);
                                    foundTransferId = true;
                                }
                            } catch (NumberFormatException ex) {
                                System.out.println("Invalid transfer choice.");
                            }
                            if (!foundTransferId) {
                                System.out.println("Update Cancelled");
                            }
                        }
                    } else {
                        System.out.println("You cannot request/reject your own request");
                    }
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("Error getting pending transfers.");
        }
        return transfers;
    }

    private HttpEntity makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity(transfer, headers);
        return entity;
    }



    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity(headers);
        return entity;
    }
}
