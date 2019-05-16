package ru.ifmo.rain.yatcheniy.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    Person findPerson(String passportId, PersonType type) throws RemoteException;

    Person recordPerson(String name,
                        String lastName,
                        String passportId) throws RemoteException;

    @SuppressWarnings("UnnecessaryInterfaceModifier")
    public enum PersonType {
        LOCAL, REMOTE
    }
}
