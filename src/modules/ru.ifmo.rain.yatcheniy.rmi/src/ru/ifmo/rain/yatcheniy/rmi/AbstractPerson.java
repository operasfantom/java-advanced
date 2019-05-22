package ru.ifmo.rain.yatcheniy.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPerson implements Person {
    protected final String name;
    protected final String lastName;
    protected final String passportId;
    protected final List<String> accountSubIds;

    AbstractPerson(String name, String lastName, String passportId, List<String> accountSubIds) {
        this.name = name;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accountSubIds = accountSubIds;
    }

    AbstractPerson(String name, String lastName, String passportId) {
        this(name, lastName, passportId, new ArrayList<>());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportId() {
        return passportId;
    }

    public List<String> getAccountSubIds() {
        return accountSubIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o instanceof Person) {
            var person = (Person) o;

            try {
                return Objects.equals(name, person.getName()) &&
                        Objects.equals(lastName, person.getLastName()) &&
                        Objects.equals(passportId, person.getPassportId());
            } catch (RemoteException e) {
                System.err.println("Couldn't get remote fields of remote person");
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lastName, passportId);
    }
}
