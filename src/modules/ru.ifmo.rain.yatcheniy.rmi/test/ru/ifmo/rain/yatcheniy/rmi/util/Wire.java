package ru.ifmo.rain.yatcheniy.rmi.util;

import ru.ifmo.rain.yatcheniy.rmi.Bank;
import ru.ifmo.rain.yatcheniy.rmi.Person;
import ru.ifmo.rain.yatcheniy.rmi.RemoteBank;
import ru.ifmo.rain.yatcheniy.rmi.RemotePerson;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Wire {
    private static final int MOCK_PORT = 1099;
//    private static Registry registry;
    private RemoteBank remoteBank;
    private Bank clientBank;
    private RemotePerson remotePerson;
    private Person clientPerson;

    public Wire() {
//        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        startServer();
        runClient();
    }

    public void startServer() {
        remoteBank = new RemoteBank(MOCK_PORT);
        remotePerson = new RemotePerson("Georgiy", "Korneev", "150519");
        try {
            UnicastRemoteObject.exportObject(remoteBank, MOCK_PORT);
            UnicastRemoteObject.exportObject(remotePerson, MOCK_PORT);
            LocateRegistry.createRegistry(MOCK_PORT);
            Naming.rebind("//localhost/bank", remoteBank);
            Naming.rebind("//localhost/person", remotePerson);
//            Naming.lookup("//localhost/bank");
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void runClient() {
        try {
            clientBank = (Bank) Naming.lookup("//localhost/bank");
            clientPerson = (Person) Naming.lookup("//localhost/person");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
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
