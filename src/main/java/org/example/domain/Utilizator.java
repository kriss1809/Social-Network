package org.example.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utilizator extends Entity<Long> {
    private String firstName;
    private String lastName;
    private List<Utilizator> friends;

    public Utilizator(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        friends=new ArrayList<Utilizator>();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName+ " " + lastName;
    }

    public List<Utilizator> getFriends() {
        return friends;
    }

    @Override
    public String toString() {
        String friendList = friends != null ? friends.stream()
                .map(friend -> friend.getFirstName() + " " + friend.getLastName())
                .reduce((friend1, friend2) -> friend1 + ", " + friend2)
                .orElse("-") : "-";

        return "ID: " + id + "; utilizator:" + firstName + " " + lastName + "; prieteni: " + friendList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName()) &&
                getFriends().equals(that.getFriends());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getFriends());
    }

    /*public void add_friend(Utilizator u)
    {
        if (friends.stream().anyMatch(f -> f.equals(u))) {
            throw new ValidationException("Aceasta prietenie exista deja!");
        }
        friends.add(u);
    }*/

    /*
    public void delete_friend(Utilizator u)
    {
        if (!friends.removeIf(f -> f.equals(u))) {
            throw new ValidationException("Aceasta prietenie nu exista!");
        }
    }

     */

}