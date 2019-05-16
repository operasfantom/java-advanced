package ru.ifmo.rain.yatcheniy.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote, Serializable {
    String getName() throws RemoteException;

    String getLastName() throws RemoteException;

    String getPassportId() throws RemoteException;

    Account getAccount(String subId) throws RemoteException;

    Account createAccount(String subId) throws RemoteException;
}
