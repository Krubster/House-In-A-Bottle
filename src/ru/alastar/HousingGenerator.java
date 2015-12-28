package ru.alastar;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Alastar on 24.06.2015.
 */
public class HousingGenerator extends ChunkGenerator{

    private List<BlockPopulator> list = null;

    public HousingGenerator(){
        list = new ArrayList<>();
        list.add(new VoidPopulator());
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return list;
    }

    @Override
    public byte[] generate(World world, Random random, int x, int z) {
        return new byte[]{0};
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return super.generateBlockSections(world, random, x, z, biomes);
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return super.generateExtBlockSections(world, random, x, z, biomes);
    }
}
