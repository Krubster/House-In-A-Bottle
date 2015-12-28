package ru.alastar;

import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

/**
 * Created by Alastar on 24.06.2015.
 */
public class HousingWorldCreator extends WorldCreator {

    private HousingGenerator generator = null;

    public HousingWorldCreator(String name) {
        super(name);
        generator = new HousingGenerator();
    }

    @Override
    public boolean generateStructures() {
        return false;
    }

    @Override
    public ChunkGenerator generator() {
        return generator;
    }
}
