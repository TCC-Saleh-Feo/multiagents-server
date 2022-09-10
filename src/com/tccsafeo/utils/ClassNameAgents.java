package com.tccsafeo.utils;

import java.util.Arrays;

public enum ClassNameAgents {
    Adder("Adder", "com.tccsafeo.agents.AdderAgent"),
    Lobby("Lobby", "com.tccsafeo.agents.LobbyOrganizerAgent");


    private final String className;

    private final String name;

    ClassNameAgents(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public static ClassNameAgents fromName(String name) {
        return Arrays.stream(ClassNameAgents.values()).filter(v -> v.name.equals(name))
                .findFirst().orElse(null);
    }
}
