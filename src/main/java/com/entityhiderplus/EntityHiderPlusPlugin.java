/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.entityhiderplus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.WorldEntity;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.party.PartyService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Entity Hider Plus",
	description = "Hide players, NPCs, and/or projectiles with per-NPC control",
	tags = {"npcs", "players", "projectiles", "hide"}
)
public class EntityHiderPlusPlugin extends Plugin
{
	private static final Set<Integer> THRALL_IDS = ImmutableSet.of(
		10878, 10879, 10880,  // Lesser thralls (ghost, skeleton, zombie)
		10881, 10882, 10883,  // Superior thralls
		10884, 10885, 10886   // Greater thralls
	);

	private static final Set<Integer> RANDOM_EVENT_NPC_IDS = ImmutableSet.<Integer>builder()
		.add(6747)  // Beekeeper invitation
		.add(5426)  // Pirate (combo lock)
		.add(307, 314)  // Jekyll, Jekyll underwater
		.add(322)   // Drunken dwarf
		.add(6749)  // Pattern invitation
		.add(390, 6754)  // Evil Bob outside, Evil Bob prison
		.add(6744)  // Pinball invitation
		.add(6748)  // Forester invitation
		.add(5429, 5430, 5431, 5432, 312)  // Frogs (crier, generic, sulking, noncombat, nohat)
		.add(5899, 5900, 5901, 5902, 5903)  // Frogs (variants)
		.add(13443, 13444, 13445, 13446)  // Frog princes/princesses
		.add(326, 327)  // Genie, Genie underwater
		.add(5438, 5441)  // Giles, Giles underwater
		.add(6746)  // Gravedigger invitation
		.add(5437, 5440)  // Miles, Miles underwater
		.add(6750, 6751)  // Mysterious old man, underwater
		.add(6752, 6753)  // Maze invitation, Mime invitation
		.add(5436, 5439)  // Niles, Niles underwater
		.add(380)   // Pillory guard
		.add(6738)  // Postie Pete
		.add(6755)  // Magneson invitation
		.add(375, 376)  // Highwayman, Highwayman underwater
		.add(5510)  // Sandwich lady
		.add(6743)  // Drill demon invitation
		.add(12551, 12552)  // Count Check surface, underwater
		.build();

	@Inject
	private Client client;

	@Inject
	private EntityHiderPlusConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Hooks hooks;

	@Inject
	private NpcUtil npcUtil;

	@Inject
	private PartyService partyService;

	@Inject
	private ClientToolbar clientToolbar;

	private EntityHiderPlusPanel panel;
	private NavigationButton navButton;

	private boolean hideOthers;
	private boolean hideOthers2D;
	private boolean hidePartyMembers;
	private boolean hideFriends;
	private boolean hideFriendsChatMembers;
	private boolean hideClanMembers;
	private boolean hideIgnoredPlayers;
	private boolean hideLocalPlayer;
	private boolean hideLocalPlayer2D;
	private boolean hideNPCs;
	private boolean hideNPCs2D;
	private boolean hideBoats;
	private boolean hideDeadNpcs;
	private boolean hidePets;
	private boolean hideThralls;
	private boolean hideRandomEvents;
	private boolean hideAttackers;
	private boolean hideProjectiles;

	private final Set<Integer> manuallyHiddenNpcIds = ConcurrentHashMap.newKeySet();

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	@Provides
	EntityHiderPlusConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EntityHiderPlusConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateConfig();
		hooks.registerRenderableDrawListener(drawListener);

		panel = new EntityHiderPlusPanel(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Entity Hider Plus")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		if (!config.hideDynamicPanel())
		{
			clientToolbar.addNavigation(navButton);
		}
	}

	@Override
	protected void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(EntityHiderPlusConfig.GROUP))
		{
			updateConfig();

			if (e.getKey().equals("hideDynamicPanel"))
			{
				if (config.hideDynamicPanel())
				{
					clientToolbar.removeNavigation(navButton);
				}
				else
				{
					clientToolbar.addNavigation(navButton);
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || panel == null || config.hideDynamicPanel())
		{
			return;
		}

		// Extract NPC data on client thread (thread-safe)
		List<NpcData> npcDataList = new ArrayList<>();
		for (NPC npc : client.getNpcs())
		{
			if (npc != null && npc.getName() != null && !npc.getName().isEmpty()
				&& !(npc.getComposition() != null && npc.getComposition().isFollower()))
			{
				boolean isHidden = manuallyHiddenNpcIds.contains(npc.getId());
				npcDataList.add(new NpcData(npc.getName(), npc.getId(), isHidden));
			}
		}

		SwingUtilities.invokeLater(() -> panel.update(npcDataList));
	}

	// Thread-safe data holder for NPC info
	public static class NpcData
	{
		public final String name;
		public final int id;
		public final boolean hidden;

		public NpcData(String name, int id, boolean hidden)
		{
			this.name = name;
			this.id = id;
			this.hidden = hidden;
		}
	}

	private void updateConfig()
	{
		hideOthers = config.hideOthers();
		hideOthers2D = config.hideOthers2D();

		hidePartyMembers = config.hidePartyMembers();
		hideFriends = config.hideFriends();
		hideFriendsChatMembers = config.hideFriendsChatMembers();
		hideClanMembers = config.hideClanChatMembers();
		hideIgnoredPlayers = config.hideIgnores();

		hideLocalPlayer = config.hideLocalPlayer();
		hideLocalPlayer2D = config.hideLocalPlayer2D();

		hideNPCs = config.hideNPCs();
		hideNPCs2D = config.hideNPCs2D();
		hideDeadNpcs = config.hideDeadNpcs();

		hideBoats = config.hideWorldEntities();

		hidePets = config.hidePets();

		hideThralls = config.hideThralls();
		hideRandomEvents = config.hideRandomEvents();

		hideAttackers = config.hideAttackers();

		hideProjectiles = config.hideProjectiles();

		loadManuallyHiddenNpcIds();
	}

	private void loadManuallyHiddenNpcIds()
	{
		manuallyHiddenNpcIds.clear();
		String ids = config.manuallyHiddenNpcIds();
		if (ids != null && !ids.isEmpty())
		{
			Arrays.stream(ids.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(Integer::parseInt)
				.forEach(manuallyHiddenNpcIds::add);
		}
	}

	private void saveManuallyHiddenNpcIds()
	{
		String ids = manuallyHiddenNpcIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
		config.setManuallyHiddenNpcIds(ids);
	}

	public boolean toggleNpcHidden(int npcId)
	{
		boolean nowHidden;
		if (manuallyHiddenNpcIds.contains(npcId))
		{
			manuallyHiddenNpcIds.remove(npcId);
			nowHidden = false;
		}
		else
		{
			manuallyHiddenNpcIds.add(npcId);
			nowHidden = true;
		}
		saveManuallyHiddenNpcIds();
		return nowHidden;
	}

	public boolean isNpcHidden(int npcId)
	{
		return manuallyHiddenNpcIds.contains(npcId);
	}

	public void hideVicinityNpcs()
	{
		clientThread.invokeLater(() ->
		{
			for (NPC npc : client.getNpcs())
			{
				if (npc != null && npc.getName() != null && !npc.getName().isEmpty()
					&& !(npc.getComposition() != null && npc.getComposition().isFollower()))
				{
					manuallyHiddenNpcIds.add(npc.getId());
				}
			}
			saveManuallyHiddenNpcIds();
		});
	}

	public void unhideVicinityNpcs()
	{
		clientThread.invokeLater(() ->
		{
			for (NPC npc : client.getNpcs())
			{
				if (npc != null)
				{
					manuallyHiddenNpcIds.remove(npc.getId());
				}
			}
			saveManuallyHiddenNpcIds();
		});
	}

	public void unhideAllNpcs()
	{
		manuallyHiddenNpcIds.clear();
		saveManuallyHiddenNpcIds();
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof Player)
		{
			Player player = (Player) renderable;
			Player local = client.getLocalPlayer();

			if (player.getName() == null)
			{
				return true;
			}

			if (player == local)
			{
				return !(drawingUI ? hideLocalPlayer2D : hideLocalPlayer);
			}

			if (hideAttackers && player.getInteracting() == local)
			{
				return false;
			}

			if (partyService.isInParty() && partyService.getMemberByDisplayName(player.getName()) != null)
			{
				return !hidePartyMembers;
			}
			if (player.isFriend())
			{
				return !hideFriends;
			}
			if (player.isFriendsChatMember())
			{
				return !hideFriendsChatMembers;
			}
			if (player.isClanMember())
			{
				return !hideClanMembers;
			}
			if (client.getIgnoreContainer().findByName(player.getName()) != null)
			{
				return !hideIgnoredPlayers;
			}

			return !(drawingUI ? hideOthers2D : hideOthers);
		}
		else if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;

			// Check manually hidden NPCs first
			if (manuallyHiddenNpcIds.contains(npc.getId()))
			{
				return false;
			}

			if (npc.getComposition().isFollower() && npc != client.getFollower())
			{
				return !hidePets;
			}

			if (npcUtil.isDying(npc) && hideDeadNpcs)
			{
				return false;
			}

			if (npc.getInteracting() == client.getLocalPlayer())
			{
				boolean b = hideAttackers;
				if (hideNPCs2D || hideNPCs)
				{
					b &= drawingUI ? hideNPCs2D : hideNPCs;
				}
				return !b;
			}

			if (THRALL_IDS.contains(npc.getId()))
			{
				return !hideThralls;
			}

			if (RANDOM_EVENT_NPC_IDS.contains(npc.getId()))
			{
				return !hideRandomEvents;
			}

			return !(drawingUI ? hideNPCs2D : hideNPCs);
		}
		else if (renderable instanceof Projectile)
		{
			return !hideProjectiles;
		}
		else if (renderable instanceof GraphicsObject)
		{
			if (!hideDeadNpcs)
			{
				return true;
			}

			// Hide Nylocas death animations in ToB
			int id = ((GraphicsObject) renderable).getId();
			if (id >= 1562 && id <= 1567)
			{
				return false;
			}
			return true;
		}
		else if (renderable instanceof Scene)
		{
			if (!hideBoats)
			{
				return true;
			}

			Scene scene = (Scene) renderable;
			WorldEntity we = client.getTopLevelWorldView().worldEntities().byIndex(scene.getWorldViewId());
			if (we.getOwnerType() == WorldEntity.OWNER_TYPE_OTHER_PLAYER)
			{
				return false;
			}

			return true;
		}

		return true;
	}
}
