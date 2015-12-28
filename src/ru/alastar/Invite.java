package ru.alastar;

import org.bukkit.entity.Player;

/**
 * Created by Alastar on 24.06.2015.
 */
public class Invite {

    public Player inviter;
    public Player invited;
    public String to;

    public Invite(Player f, Player s, String housingName) {
        inviter = f;
        invited = s;
        to = housingName;
    }
}
