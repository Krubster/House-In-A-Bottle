package ru.alastar;

import org.bukkit.entity.Player;

/**
 * Created by Alastar on 24.06.2015.
 */
public class InviteClick {

    public Player sender;
    public String housingName;

    public InviteClick(Player sender, String arg) {
        this.sender = sender;
        this.housingName = arg;
    }
}
