package ru.ifmo.rain.yatcheniy.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Person findPerson(String passportId, PersonType type) throws RemoteException {
        System.out.println("Finding person " + passportId);
        var person = persons.get(passportId);
        if (person == null) {
            return null;
        }
        switch (type) {
            case LOCAL:
                return person.toLocal();
            case REMOTE:
                return person;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    @Override
    public Person recordPerson(String name, String lastName, String passportId) throws RemoteException {
        System.out.println(String.format("Recording person name=%s, last name=%s, passport id=%s", name, lastName, passportId));
        var person = persons.get(passportId);
        if (person == null) {
            person = new RemotePerson(name, lastName, passportId);
            persons.put(passportId, person);
            UnicastRemoteObject.exportObject(person, port);
        }
        return null;
    }
}
