package ru.ifmo.rain.yatcheniy.rmi;

import org.junit.Test;

import java.rmi.RemoteException;

import static org.junit.Assert.assertEquals;

public class RemoteAccountTest implements RemoteTestBase {

    @Test
    public void getId() throws RemoteException {
        Account account = wire.getRemotePerson().createAccount("1");
        assertEquals(account.getId(), "150519:1");
    }

    @Test
    public void getAndSetAmount() throws RemoteException {
        Account account = wire.getClientPerson().createAccount("1");
        assertEquals(0, account.getAmount());
        account.setAmount(100);
        assertEquals(100, account.getAmount());
    }
}