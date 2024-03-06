package ru.sortix.parkourbeat.levels.gen;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class EmptyChunkGenerator extends ChunkGenerator {
    private final Server server;

    @Override
    @NotNull public ChunkData generateChunkData(
            @NotNull World world, @NotNull Random random, int x, int z, @NotNull BiomeGrid biome) {
        return this.server.createChunkData(world);
    }

    @NotNull public ChunkData createVanillaChunkData(@NotNull World world, int x, int z) {
        return this.server.createChunkData(world);
    }

    @Override
    public boolean canSpawn(@NotNull World world, int x, int z) {
        return true;
    }
}
