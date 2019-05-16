package ru.ifmo.rain.yatcheniy.rmi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalPerson extends AbstractPerson {
    private final Map<String, Account> accounts;

    public LocalPerson(String name, String lastName, String passportId) {
        super(name, lastName, passportId);
        this.accounts = new HashMap<>();
    }

    @SuppressWarnings("WeakerAccess")
    public LocalPerson(String name, String lastName, String passportId, List<String> accountSubIds) {
        super(name, lastName, passportId, accountSubIds);
        this.accounts = new HashMap<>();
    }

    LocalPerson(String name, String lastName, String passportId, List<String> accountSubIds, Map<String, Account> accounts) {
        super(name, lastName, passportId, accountSubIds);
        this.accounts = accounts;
    }

    @Override
    public Account getAccount(String subId) {
        return accounts.get(subId);
    }

    @Override
    public Account createAccount(String subId) {
        var account = accounts.get(subId);
        if (account == null) {
            Account newAccount = new RemoteAccount(subId);
            accounts.put(subId, newAccount);
            accountSubIds.add(subId);
            account = newAccount;
        }
        return account;
    }
}
