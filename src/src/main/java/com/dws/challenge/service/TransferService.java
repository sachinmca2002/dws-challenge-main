package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.NotificationService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class  TransferService {

  private final NotificationService notificationService;

  public TransferService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  public void transfer(Account accountFrom, Account accountTo, double amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }

    Account firstLock = accountFrom.getId().compareTo(accountTo.getId()) < 0 ? accountFrom : accountTo;
    Account secondLock = accountFrom.getId().compareTo(accountTo.getId()) < 0 ? accountTo : accountFrom;

    firstLock.lock();
    try {
      secondLock.lock();
      try {
        if (accountFrom.withdraw(amount)) {
          accountTo.deposit(amount);
          notificationService.notifyAccount(accountFrom, "Transferred " + amount + " to " + accountTo.getId());
          notificationService.notifyAccount(accountTo, "Received " + amount + " from " + accountFrom.getId());
        } else {
          throw new IllegalStateException("Insufficient balance in account: " + accountFrom.getId());
        }
      } finally {
        secondLock.unlock();
      }
    } finally {
      firstLock.unlock();
    }
  }
}
