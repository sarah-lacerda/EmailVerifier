package me.sarahlacerda.main.listener;

import me.sarahlacerda.main.manager.PlayerManager;
import me.sarahlacerda.main.message.ConsoleMessages;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

import static java.text.MessageFormat.format;
import static me.sarahlacerda.main.message.ConsoleMessages.KICKED_TO_MAKE_ROOM;
import static me.sarahlacerda.main.message.ConsoleMessages.NOT_AUTHENTICATED;
import static me.sarahlacerda.main.message.ConsoleMessages.SERVER_IS_FULL;
import static me.sarahlacerda.main.message.ConsoleMessages.WELCOME_BACK_ALREADY_REGISTERED;
import static me.sarahlacerda.main.message.ConsoleMessages.WELCOME_NEW_PLAYER;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerEventListener implements Listener {

    private final PlayerManager playerManager;

    public PlayerEventListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent playerLoginEvent) {
        if (isServerFull()) {
            handleLoginWhenServerIsFull(playerLoginEvent);
        }

        if (isNotPriorityPlayer(playerLoginEvent.getPlayer())) {
            playerManager.addUnauthenticated(playerLoginEvent.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        if (isNotPriorityPlayer(playerJoinEvent.getPlayer())) {
            handlePlayerMessagesBasedOnContext(playerJoinEvent);
        }
    }

    //Do not let player move around if not authenticated
    @EventHandler
    public void onMove(PlayerMoveEvent playerMoveEvent) {
        if (playerManager.isUnauthenticated(playerMoveEvent.getPlayer())) {
            playerMoveEvent.setCancelled(true);
        }
    }

    //Do not let player drop items if not authenticated
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent playerDropItemEvent) {
        if (playerManager.isUnauthenticated(playerDropItemEvent.getPlayer())) {
            playerDropItemEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent playerQuitEvent) {
        if (playerManager.playerAlreadyEmailVerifiedButHasNoPasswordSet(playerQuitEvent.getPlayer().getUniqueId())) {
            playerManager.removePlayer(playerQuitEvent.getPlayer().getUniqueId().toString());
        }

        playerManager.removeFromOnlineUnauthenticatedPlayers(playerQuitEvent.getPlayer());
    }

    //Block all interaction from unauthenticated players
    @EventHandler
    public void onPlayerDropItem(PlayerInteractEvent playerInteractEvent) {
        if (playerManager.isUnauthenticated(playerInteractEvent.getPlayer())) {
            playerInteractEvent.getPlayer().sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
            playerInteractEvent.setCancelled(true);
        }
    }

    //Block all chat from default players
    @EventHandler
    public void onChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
        if (playerManager.isUnauthenticated(asyncPlayerChatEvent.getPlayer())) {
            asyncPlayerChatEvent.getPlayer().sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
            asyncPlayerChatEvent.setCancelled(true);
        }
    }

    //Prevent default players from picking up itmes
    @EventHandler
    public void onCollect(EntityPickupItemEvent entityPickupItemEvent) {
        if (entityPickupItemEvent.getEntity() instanceof Player player) {
            if (playerManager.isUnauthenticated(player)) {
                entityPickupItemEvent.setCancelled(true);
            }
        }
    }

    //Prevent default players from receiving damage
    @EventHandler
    public void onReceiveDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity() instanceof Player player) {
            if (playerManager.isUnauthenticated(player)) {
                player.sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
                entityDamageEvent.setCancelled(true);
            }
        }
    }

    //Prevent default players from dealing damage
    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (entityDamageByEntityEvent.getDamager() instanceof Player player) {

            if (playerManager.isUnauthenticated(player)) {
                player.sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
                entityDamageByEntityEvent.setCancelled(true);
            }
        }
    }

    //Hide default players from new players.
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent entityTargetLivingEntityEvent) {
        if (entityTargetLivingEntityEvent.getTarget() instanceof Player player) {
            if (playerManager.isUnauthenticated(player)) {
                entityTargetLivingEntityEvent.setCancelled(true);
                entityTargetLivingEntityEvent.setTarget(null);
            }
        }
    }

    //Disallow all commands (except ones related to login/register) if player is not authenticated
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        if (playerManager.isUnauthenticated(playerCommandPreprocessEvent.getPlayer())) {
            List<String> allowedCommands = List.of(
                    "/registrar ",
                    "/reg ",
                    "/register ",
                    "/email ",
                    "/code ",
                    "/password ",
                    "/senha ",
                    "/pass ",
                    "/resetsenha",
                    "/resetpassword",
                    "/login ");

            for (String allowedCommand : allowedCommands) {
                if (playerCommandPreprocessEvent.getMessage().contains(allowedCommand)) {
                    return;
                }
            }

            playerCommandPreprocessEvent.getPlayer().sendMessage(ChatColor.RED + get(NOT_AUTHENTICATED));
            playerCommandPreprocessEvent.setCancelled(true);
        }


    }

    private void handlePlayerMessagesBasedOnContext(PlayerJoinEvent playerJoinEvent) {
        if (playerManager.playerAlreadyRegistered(playerJoinEvent.getPlayer().getUniqueId())) {
            playerJoinEvent.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + format(get(WELCOME_BACK_ALREADY_REGISTERED), playerJoinEvent.getPlayer().getName()));
        } else {
            playerJoinEvent.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + format(get(WELCOME_NEW_PLAYER), playerJoinEvent.getPlayer().getName()));
        }
    }

    private void handleLoginWhenServerIsFull(PlayerLoginEvent playerLoginEvent) {
        //if the slot is being used by a default player
        if (playerManager.onlineUnauthenticatedPlayers() > 0) {
            //Kick the default player who was first and allow the login
            playerManager.getOldestOnlineUnauthenticatedPlayer().kickPlayer(ChatColor.RED + ConsoleMessages.get(KICKED_TO_MAKE_ROOM));
            playerLoginEvent.allow();
        } else if (playerLoginEvent.getPlayer().isOp()) {
            playerLoginEvent.allow();
        } else {
            playerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.RED + ConsoleMessages.get(SERVER_IS_FULL));
        }
    }

    //If the player has bypass permission, or is an OP
    private boolean isNotPriorityPlayer(Player player) {
        return !player.getPlayer().isOp() && !player.getPlayer().hasPermission("auth.bypass");
    }

    private boolean isServerFull() {
        return Bukkit.getServer().getMaxPlayers() == Bukkit.getOnlinePlayers().size();
    }

}
