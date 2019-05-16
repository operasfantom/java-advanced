package ru.ifmo.rain.yatcheniy.rmi;

class PersonTestBase {
    private LocalPerson localPerson;

    @SuppressWarnings("WeakerAccess")
    protected void setPerson(LocalPerson localPerson) {
        this.localPerson = localPerson;
    }

    @SuppressWarnings("WeakerAccess")
    protected Person getPerson() {
        return localPerson;
    }
}
