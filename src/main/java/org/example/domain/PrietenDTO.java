package org.example.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrietenDTO {

    Utilizator friend;
    LocalDateTime startOfFriendship;
    public Utilizator getFriend() {
        return friend;
    }

    public void setFriend(Utilizator friend) {
        this.friend = friend;
    }

    public LocalDateTime getStartOfFriendship() {

        return startOfFriendship;
    }

    public void setStartOfFriendship(LocalDateTime startOfFriendship) {
        this.startOfFriendship = startOfFriendship;
    }

    public PrietenDTO(Utilizator friend, LocalDateTime startOfFriendship) {
        this.friend = friend;
        this.startOfFriendship = startOfFriendship;
    }

    @Override
    public String toString() {
        String time_printed;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        time_printed=startOfFriendship.format(formatter);
        return friend.getFirstName()+"| "+
                friend.getLastName()+"| "+
                time_printed;
    }
}