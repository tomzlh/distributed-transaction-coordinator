package com.ops.sc.ta.bean;

import com.ops.sc.common.bean.ScRequestMessage;
import com.ops.sc.common.enums.Participant;

public class TaRegistryBean {

    private Participant participant;
    private String address;
    private ScRequestMessage message;


    public TaRegistryBean(Participant participant, String address) {
        this.participant = participant;
        this.address = address;
    }


    public TaRegistryBean(Participant participant, String address, ScRequestMessage message) {
        this.participant = participant;
        this.address = address;
        this.message = message;
    }


    public Participant getParticipant() {
        return participant;
    }


    public TaRegistryBean setParticipant(Participant participant) {
        this.participant = participant;
        return this;
    }


    public String getAddress() {
        return address;
    }


    public TaRegistryBean setAddress(String address) {
        this.address = address;
        return this;
    }


    public ScRequestMessage getMessage() {
        return message;
    }


    public void setMessage(ScRequestMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Participant:");
        sb.append(participant.name());
        sb.append(",");
        sb.append("address:");
        sb.append(address);
        sb.append(",");
        sb.append("msg:< ");
        sb.append(message.toString());
        sb.append(" >");
        return sb.toString();
    }


}
