package org.example.domain;

import org.example.utils.events.FriendshipStatusType;

import java.time.LocalDateTime;
import java.util.Objects;

public class Invitatie extends Entity<Tuple<Long,Long>>{

    private Long id1;
    private Long id2;
    private FriendshipStatusType status;

    public Invitatie(Long id1, Long id2, FriendshipStatusType status) {
        this.id1 = id1;
        this.id2 = id2;
        this.status = status;
        Tuple<Long,Long> IDComb=new Tuple<>(id1,id2);
        this.setId(IDComb);
    }

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public String getStatus() {
        return status.toString();
    }

    public void setStatus(FriendshipStatusType status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Invitatie{" +
                "id1=" + id1 +
                ", id2=" + id2 +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Invitatie invitatie = (Invitatie) o;
        return Objects.equals(id1, invitatie.id1) && Objects.equals(id2, invitatie.id2) && Objects.equals(status, invitatie.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id1, id2, status);
    }
}
