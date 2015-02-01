package com.fuzzoland.BungeePortals.Listeners;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.fuzzoland.BungeePortals.BungeePortals;

public class EventListener implements Listener{

	private BungeePortals plugin;
	private Map<String, Boolean> statusData = new HashMap<String, Boolean>();
	private final CooldownHandler cooldown = new CooldownHandler();
	
	public EventListener(BungeePortals plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) throws IOException{
		Player player = event.getPlayer();
		String playerName = player.getName();

		if(!this.statusData.containsKey(playerName)){
			this.statusData.put(playerName, false);
		}
		Block block = player.getWorld().getBlockAt(player.getLocation());
		String data = block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ());
		if(plugin.portalData.containsKey(data)){
			if(!this.statusData.get(playerName)){
				this.statusData.put(playerName, true);
				// Check if the player is on cooldown
				if (cooldown.isOnCooldown(player)) {
					player.sendMessage(ChatColor.RED + "Please wait " + CooldownHandler.COOLDOWN_SECONDS + " seconds until attempting to join again.");
					return; // Player is on cooldown.
				}

				String destination = plugin.portalData.get(data);
				if(player.hasPermission("BungeePortals.portal." + destination) || player.hasPermission("BungeePortals.portal.*")){
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);
					dos.writeUTF("Connect");
					dos.writeUTF(destination);
					player.sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());
					baos.close();
					dos.close();

					// Put the player on cooldown
					cooldown.addToCooldown(player);
				}else{
					player.sendMessage(plugin.configFile.getString("NoPortalPermissionMessage").replace("{destination}", destination).replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
				}
			}
		}else{
			if(this.statusData.get(playerName)){
				this.statusData.put(playerName, false);
			}
		}
	}


	private class CooldownHandler {
		public static final int COOLDOWN_SECONDS = 10;
		private List<Player> players = new ArrayList<Player>();

		public synchronized void addToCooldown(final Player player) {
			this.players.add(player);

			new BukkitRunnable() {
				public void run() {
					removeFromCooldown(player);
				}
			}.runTaskLater(plugin, COOLDOWN_SECONDS * 20);
		}

		public boolean isOnCooldown(Player player) {
			return this.players.contains(player);
		}

		public synchronized void removeFromCooldown(Player player) {
			this.players.remove(player);
		}
	}
}
