package ru.sortix.parkourbeat.levels.gen;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class EmptyChunkGenerator extends ChunkGenerator {
    @Override
    @NotNull public ChunkData generateChunkData(
            @NotNull World world, @NotNull Random random, int x, int z, @NotNull BiomeGrid biome) {
        return Bukkit.getServer().createChunkData(world);
    }

    @NotNull public ChunkData createVanillaChunkData(@NotNull World world, int x, int z) {
        return Bukkit.getServer().createChunkData(world);
    }

    @Override
    public boolean canSpawn(@NotNull World world, int x, int z) {
        return true;
    }
}
