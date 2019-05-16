package ru.ifmo.rain.yatcheniy.rmi;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.rmi.RemoteException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class RemotePersonTestBase implements RemoteTestBase {
    public RemotePersonTestBase() {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void createAndGetAccount() throws RemoteException {
        Account account = wire.getClientPerson().createAccount("1");
        assertEquals("1", account.getId());
        assertNull(wire.getClientBank().getAccount("2"));
    }

    @Test
    public void toLocal() throws RemoteException {
        LocalPerson localPerson = wire.getRemotePerson().toLocal();
        assertEquals(wire.getRemotePerson().name, localPerson.getName());
        assertEquals(wire.getRemotePerson().lastName, localPerson.getLastName());
        assertEquals(wire.getRemotePerson().passportId, localPerson.getPassportId());
    }
}