package com.dineshsawant.pg.cdc.state;

import java.util.Date;

public class State {
    private String slotName;
    private String lastLogSequenceNumber;
    private Date createdAt;
    private Date updatedAt;

    public State() {}

    public State(String slotName, String lastLogSequenceNumber) {
        this.slotName = slotName;
        this.lastLogSequenceNumber = lastLogSequenceNumber;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public String getSlotName() {
        return slotName;
    }

    public String getLastLogSequenceNumber() {
        return lastLogSequenceNumber;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void merge(State newState) {
        this.lastLogSequenceNumber = newState.lastLogSequenceNumber;
        this.updatedAt = newState.updatedAt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("State{");
        sb.append("slotName='").append(slotName).append('\'');
        sb.append(", lastLogSequenceNumber='").append(lastLogSequenceNumber).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append('}');
        return sb.toString();
    }
}
