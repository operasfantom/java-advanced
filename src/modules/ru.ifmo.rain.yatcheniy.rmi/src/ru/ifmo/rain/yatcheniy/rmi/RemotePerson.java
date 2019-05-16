package ru.ifmo.rain.yatcheniy.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RemotePerson extends AbstractPerson {
    private Bank bank = null;

    @SuppressWarnings("WeakerAccess")
    public RemotePerson(String name, String lastName, String passportId) {
        super(name, lastName, passportId);
    }

    private String buildAccountId(String subId) {
        return passportId + ":" + subId;
    }

    @Override
    synchronized public Account getAccount(String subId) throws RemoteException {
        return getBank().getAccount(buildAccountId(subId));
    }

    @Override
    public Account createAccount(String subId) throws RemoteException {
        if (getAccount(subId) == null) {
            accountSubIds.add(subId);
        }
        return getBank().createAccount(buildAccountId(subId));
    }

    private synchronized Bank getBank() throws RemoteException {
        if (bank == null) {
            try {
                bank = (Bank) Naming.lookup("//localhost/bank");
            } catch (NotBoundException e) {
                System.err.println("Bank is not bound:" + e.getMessage());
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL has occurred:" + e.getMessage());
            }
        }
        return bank;
    }

    synchronized LocalPerson toLocal() throws RemoteException {
        var person = (AbstractPerson) this;
        final List<RemoteException> exceptions = new ArrayList<>();
        Map<String, Account> accounts = getAccountSubIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> {
                    try {
                        return getAccount(s);
                    } catch (RemoteException e) {
                        exceptions.add(e);
                        return null;
                    }
                }));
        if (!exceptions.isEmpty()) {
            for (int i = 1; i < exceptions.size(); i++) {
                exceptions.get(0).addSuppressed(exceptions.get(i));
            }
            throw exceptions.get(0);
        }
        return new LocalPerson(super.name, super.lastName, super.passportId, super.accountSubIds, accounts);
    }
}
