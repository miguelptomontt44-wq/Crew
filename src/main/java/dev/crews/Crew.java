package dev.crews;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class Crew {
    private String name;
    private UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();

    public Crew(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public String name() { return name; }
    public void name(String name) { this.name = name; }
    public String key() { return name.toLowerCase(); }
    public UUID leader() { return leader; }
    public void leader(UUID leader) { this.leader = leader; }
    public boolean isLeader(UUID id) { return leader.equals(id); }
    public Set<UUID> members() { return members; }
}
