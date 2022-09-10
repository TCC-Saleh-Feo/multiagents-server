package com.tccsafeo.persistence.entities;

import java.util.Arrays;

public enum AgentType
{
    adder(1, "Adder"),
    lobbyOrganizer(2, "Lobby"),
    health(3, "Health"),
    unknown(4, "Adder");

    private final int code;

    private final String name;

    AgentType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode()
    {
        return code;
    }

    public String getName() { return name; }

    public static AgentType fromCode(int code)
    {
        return Arrays.stream(AgentType.values()).filter(v -> v.code == code)
                .findFirst().orElse(null);
    }

    public static AgentType fromName(String name)
    {
        return Arrays.stream(AgentType.values()).filter(v -> v.name.equals(name))
                .findFirst().orElse(null);
    }
}
