package org.example.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Prietenie extends Entity<Tuple<Long,Long>> {

    private LocalDateTime date;
    private Utilizator user1;
    private Utilizator user2;
    public Prietenie(Utilizator user1, Utilizator user2, LocalDateTime date)
    {
        this.user1=user1;
        this.user2=user2;
        this.date=date;
        Long uuid1=user1.getId();
        Long uuid2=user2.getId();
        Tuple<Long,Long> UUIDComb=new Tuple<>(uuid1,uuid2);
        this.setId(UUIDComb);
    }

    private String formatter(LocalDateTime time)
    {
        String time_printed;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        time_printed=time.format(formatter);
        return time_printed;
    }

    @Override
    public String toString() {
        return "Prietenie intre: " + user1.getId() + " si " + user2.getId() + ", data: "+formatter(getDate());
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Utilizator getUser1() {
        return user1;
    }

    public void setUser1(Utilizator user1) {
        this.user1 = user1;
    }

    public Utilizator getUser2() {
        return user2;
    }

    public void setUser2(Utilizator user2) {
        this.user2 = user2;
    }
}
