package org.example.user;

public class User {
    private String name;
    private int countOfTickets;

    public User(String name, int countOfTickets) {
        this.name = name;
        this.countOfTickets = countOfTickets;
    }

    @Override
    public String toString() {
        return "User{" +
               "name='" + name + '\'' +
               ", countOfTickets=" + countOfTickets +
               '}';
    }
}
