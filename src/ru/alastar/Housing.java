package ru.alastar;

import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.entity.Player;

import java.io.Serializable;

/**
 * Created by Alastar on 24.06.2015.
 */
public class Housing implements Serializable {

    public double _x_tel, _y_tel, _z_tel;
    public int _x_grid, _y_grid;
    public String _name;

    public Housing(double x, double y, double z, int xg, int yg, String name)
    {
        this._x_tel = x;
        this._y_tel = y;
        this._z_tel = z;
        this._x_grid = xg;
        this._y_grid = yg;
        this._name = name; // Player name
    }

    public boolean checkOwner(Player sender) {
        if(sender.getName().equalsIgnoreCase(_name))
            return true;

        return false;
    }
}
