package pl.tenfajnybartek.deathbanplugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

public class PlayerPreBanEvent extends Event implements Cancellable {

    private final Player player;
    private long time;
    private boolean cancelled = false;

    private static final HandlerList HANDLERS = new HandlerList();

    public PlayerPreBanEvent(Player player, long time) {
        this.player = player;
        this.time = time;
    }

    public Player getPlayer() {
        return player;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}