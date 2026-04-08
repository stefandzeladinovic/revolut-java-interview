package com.revolut.ledger;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LedgerLockFree {
    private final BlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue<>();
    private final Thread processingThread;
    private volatile boolean running = true;

    public LedgerLockFree() {
        processingThread = new Thread(this::processTransactions, "TransactionProcessor");
        processingThread.setDaemon(true);
        processingThread.start();
    }

    public void transferMoney(AccountLockFree fromAccount, AccountLockFree toAccount, BigDecimal amount) {
        try {
            transactionQueue.put(new Transaction(fromAccount, toAccount, amount));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processTransactions() {
        while (running) {
            try {
                Transaction transaction = transactionQueue.take();
                if (transaction.fromAccount.withdraw(transaction.amount)) {
                    transaction.toAccount.deposit(transaction.amount);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        processingThread.interrupt();
    }

    private static class Transaction {
        final AccountLockFree fromAccount;
        final AccountLockFree toAccount;
        final BigDecimal amount;

        Transaction(AccountLockFree fromAccount, AccountLockFree toAccount, BigDecimal amount) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
        }
    }
}