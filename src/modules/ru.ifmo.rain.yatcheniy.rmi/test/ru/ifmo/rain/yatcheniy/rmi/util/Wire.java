package ru.ifmo.rain.yatcheniy.rmi.util;

import ru.ifmo.rain.yatcheniy.rmi.Bank;
import ru.ifmo.rain.yatcheniy.rmi.Person;
import ru.ifmo.rain.yatcheniy.rmi.RemoteBank;
import ru.ifmo.rain.yatcheniy.rmi.RemotePerson;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Wire {
    private static final int MOCK_PORT = NetUtils.findFreePort();
    private RemoteBank remoteBank;
    private Bank clientBank;
    private static Registry registry;
    private RemotePerson remotePerson;
    private Person clientPerson;

    public Wire() {
        startServer();
        runClient();
    }

    public void startServer() {
        remoteBank = new RemoteBank(MOCK_PORT);
        remotePerson = new RemotePerson("Georgiy", "Korneev", "150519");
        try {
            registry = LocateRegistry.createRegistry(MOCK_PORT);
            UnicastRemoteObject.exportObject(remoteBank, MOCK_PORT);
            UnicastRemoteObject.exportObject(remotePerson, MOCK_PORT);
            registry.rebind("//localhost/bank", remoteBank);
            registry.rebind("//localhost/person", remotePerson);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void runClient() {
        try {
            clientBank = (Bank) registry.lookup("//localhost/bank");
            clientPerson = (Person) registry.lookup("//localhost/person");
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public Bank getClientBank() {
        return clientBank;
    }

    public RemoteBank getRemoteBank() {
        return remoteBank;
    }

    public Person getClientPerson() {
        return clientPerson;
    }

    public RemotePerson getRemotePerson() {
        return remotePerson;
    }
}
