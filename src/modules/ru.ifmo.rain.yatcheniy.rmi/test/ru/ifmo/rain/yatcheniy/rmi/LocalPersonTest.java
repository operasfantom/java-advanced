package ru.ifmo.rain.yatcheniy.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.rmi.RemoteException;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class LocalPersonTest extends PersonTestBase {
    @Before
    public void setUp() throws Exception {
        setPerson(new LocalPerson("Georgiy", "Korneev", "150519"));
    }

    @org.junit.Test
    public void getName() throws RemoteException {
        assertEquals("Georgiy", getPerson().getName());
    }

    @org.junit.Test
    public void getLastName() throws RemoteException {
        assertEquals("Korneev", getPerson().getLastName());
    }

    @org.junit.Test
    public void getPassportId() throws RemoteException {
        assertEquals("150519", getPerson().getPassportId());
    }

    @org.junit.Test
    public void createAndGetAccount() throws RemoteException {
        Account account = getPerson().createAccount("1");

        assertEquals(account, getPerson().getAccount("1"));
        assertNull(getPerson().getAccount("2"));
    }

    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutput output = new ObjectOutputStream(byteArrayOutputStream)) {
            output.writeObject(getPerson());
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        try (ObjectInput input = new ObjectInputStream(byteArrayInputStream)) {
            assertEquals(getPerson(), input.readObject());
        }
    }
}
