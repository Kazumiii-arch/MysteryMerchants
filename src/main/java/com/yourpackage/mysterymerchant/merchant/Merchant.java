package com.yourpackage.mysterymerchant.merchant;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Merchant {

    private final MerchantManager manager;
    private final Location spawnLocation;
    private Villager merchantEntity;
    private UUID entityId;
    private BukkitTask despawnTask;
    private long spawnTimestamp; // To track spawn time
    private long durationSeconds; // To store total duration

    public Merchant(MerchantManager manager, Location spawnLocation) {
        this.manager = manager;
        this.spawnLocation = spawnLocation;
    }

    public void spawn() {
        this.spawnTimestamp = System.currentTimeMillis();
        this.durationSeconds = manager.getPlugin().getConfig().getLong("merchant.duration-minutes", 5) * 60;

        spawnLocation.getChunk().load();
        this.merchantEntity = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
        this.entityId = merchantEntity.getUniqueId();
        configureEntity();
        startDespawnTimer();
        playSpawnEffects();
    }

    private void configureEntity() {
        if (merchantEntity == null) return;
        String name = ChatColor.translateAlternateColorCodes('&', manager.getPlugin().getConfig().getString("merchant.name", "&5&lMystery Merchant"));
        merchantEntity.setCustomName(name);
        merchantEntity.setCustomNameVisible(true);
        merchantEntity.setProfession(Villager.Profession.NITWIT);
        merchantEntity.setVillagerType(Villager.Type.SWAMP);
        merchantEntity.setAI(false);
        merchantEntity.setSilent(true);
        merchantEntity.setInvulnerable(true);
        merchantEntity.setCollidable(false);
    }
    
    private void startDespawnTimer() {
        this.despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                manager.despawnMerchant();
            }
        }.runTaskLater(manager.getPlugin(), durationSeconds * 20);
    }

    public long getRemainingSeconds() {
        long elapsedMillis = System.currentTimeMillis() - spawnTimestamp;
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
        return Math.max(0, durationSeconds - elapsedSeconds);
    }

    public void despawn(boolean withEffects) {
        if (merchantEntity != null && merchantEntity.isValid()) {
            if (withEffects) playDespawnEffects();
            merchantEntity.remove();
        }
        if (despawnTask != null && !despawnTask.isCancelled()) {
            despawnTask.cancel();
        }
        this.merchantEntity = null;
        this.entityId = null;
    }

    private void playSpawnEffects() {
        Location loc = merchantEntity.getLocation().add(0, 1, 0);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc, 100, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
    }

    private void playDespawnEffects() {
        Location loc = merchantEntity.getLocation().add(0, 1, 0);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 50, 0.5, 0.5, 0.5, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_FOX_TELEPORT, 1.0f, 1.0f);
    }

    public boolean isSpawned() {
        return merchantEntity != null && merchantEntity.isValid();
    }

    public UUID getEntityId() {
        return entityId;
    }
}
