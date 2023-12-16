package org.example.utils.events;

import org.example.domain.Prietenie;

public class FriendshipStatusEvent implements Event{
    private FriendshipStatusType type;
    private Prietenie friendship;
    public FriendshipStatusEvent(FriendshipStatusType type, Prietenie friendship)
    {
        this.type=type;
        this.friendship=friendship;
    }

    public FriendshipStatusType getType() {
        return type;
    }

    public void setType(FriendshipStatusType type) {
        this.type = type;
    }

    public Prietenie getFriendship() {
        return friendship;
    }

    public void setFriendship(Prietenie friendship) {
        this.friendship = friendship;
    }
}
