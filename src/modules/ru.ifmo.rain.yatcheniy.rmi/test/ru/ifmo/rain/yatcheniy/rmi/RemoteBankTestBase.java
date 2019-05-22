package ru.ifmo.rain.yatcheniy.rmi;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static org.junit.Assert.*;

public class RemoteBankTestBase implements RemoteTestBase {
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createAndGetAccount() throws RemoteException {
        Account account = wire.getClientBank().createAccount("1");
        assertEquals(account, wire.getClientBank().getAccount("1"));
        assertNull(wire.getClientBank().getAccount("2"));
    }

    @Test
    public void findAndRecordPerson() throws RemoteException {
        Person person = wire.getClientBank().recordPerson("Georgiy", "Korneev", "150519");
        Person localPerson = wire.getClientBank().findPerson("150519", Bank.PersonType.LOCAL);
        Person remotePerson = wire.getClientBank().findPerson("150519", Bank.PersonType.REMOTE);
        assertTrue(localPerson instanceof LocalPerson);
        assertFalse(remotePerson instanceof LocalPerson);

        assertNull(wire.getClientBank().findPerson(" ", Bank.PersonType.LOCAL));
        assertNull(wire.getClientBank().findPerson(" ", Bank.PersonType.REMOTE));
    }


}