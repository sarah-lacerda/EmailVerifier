package me.sarahlacerda.main.service;

import me.sarahlacerda.main.manager.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static me.sarahlacerda.main.message.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_LOGIN;
import static me.sarahlacerda.main.message.ConsoleMessages.NO_PASSWORD_SET_YET;
import static me.sarahlacerda.main.message.ConsoleMessages.WRONG_PASSWORD;
import static me.sarahlacerda.main.message.ConsoleMessages.YOU_ARE_IN;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class PlayerLoginService {
    private final PasswordService passwordService;
    private final PlayerManager playerManager;

    public PlayerLoginService(PasswordService passwordService, PlayerManager playerManager) {
        this.passwordService = passwordService;
        this.playerManager = playerManager;
    }

    public boolean login(Player player, String password) {
        if (playerManager.playerAlreadyRegistered(player.getUniqueId())) {
            if (passwordsMatch(password, player)) {
                playerManager.removeFromOnlineUnauthenticatedPlayers(player);
                player.sendMessage(ChatColor.GREEN + get(YOU_ARE_IN));
            } else {
                player.sendMessage(ChatColor.RED + get(WRONG_PASSWORD));
            }
            return true;
        } else if (playerManager.playerAlreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + get(NO_PASSWORD_SET_YET));
            return true;
        }

        player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_LOGIN));
        return true;
    }

    private boolean passwordsMatch(String passwordProvided, Player player) {
        return passwordService.validate(passwordProvided, playerManager.getPlayerPassword(player.getUniqueId().toString()));
    }
}
