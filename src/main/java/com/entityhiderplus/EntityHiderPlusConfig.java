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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(EntityHiderPlusConfig.GROUP)
public interface EntityHiderPlusConfig extends Config
{
	String GROUP = "entityhiderplus";

	@ConfigSection(
		name = "Dynamic Panel",
		description = "Sidebar panel for hiding individual NPCs by clicking them in a list. Use the buttons to bulk hide/unhide nearby NPCs.",
		position = 0
	)
	String dynamicPanelSection = "dynamicPanel";

	@ConfigSection(
		name = "Other Players",
		description = "Hide other players' 3D models and 2D overlays (names, health bars, etc). Exemptions for friends, clan, party, and ignored players below.",
		position = 10
	)
	String otherPlayersSection = "otherPlayers";

	@ConfigSection(
		name = "Local Player",
		description = "Hide your own character's 3D model and 2D overlays.",
		position = 20
	)
	String localPlayerSection = "localPlayer";

	@ConfigSection(
		name = "NPCs",
		description = "Hide NPC 3D models and 2D overlays. For per-NPC control, use the Dynamic Panel sidebar instead.",
		position = 30
	)
	String npcsSection = "npcs";

	@ConfigSection(
		name = "Other",
		description = "Miscellaneous hiding options for pets, thralls, projectiles, random events, and attackers.",
		position = 40
	)
	String otherSection = "other";

	@ConfigItem(
		position = 0,
		keyName = "hideDynamicPanel",
		name = "Hide sidebar panel",
		description = "Hides the NPC list panel from the sidebar. Disable to access per-NPC hide/unhide controls.",
		section = dynamicPanelSection
	)
	default boolean hideDynamicPanel()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		keyName = "hidePlayers",
		name = "Hide others",
		description = "Hides other players' 3D models (character, equipment, animations).",
		section = otherPlayersSection
	)
	default boolean hideOthers()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "hidePlayers2D",
		name = "Hide others' 2D",
		description = "Hides other players' 2D elements (overhead text, health bars, names).",
		section = otherPlayersSection
	)
	default boolean hideOthers2D()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "hidePartyMembers",
		name = "Hide party members",
		description = "Hides players in your RuneLite party.",
		section = otherPlayersSection
	)
	default boolean hidePartyMembers()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hideFriends",
		name = "Hide friends",
		description = "Hides players on your friends list.",
		section = otherPlayersSection
	)
	default boolean hideFriends()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "hideClanMates",
		name = "Hide friends chat members",
		description = "Hides players in your current friends chat channel.",
		section = otherPlayersSection
	)
	default boolean hideFriendsChatMembers()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "hideClanChatMembers",
		name = "Hide clan members",
		description = "Hides players in your clan.",
		section = otherPlayersSection
	)
	default boolean hideClanChatMembers()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "hideIgnores",
		name = "Hide ignored players",
		description = "Hides players on your ignore list.",
		section = otherPlayersSection
	)
	default boolean hideIgnores()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		keyName = "hideLocalPlayer",
		name = "Hide local player",
		description = "Hides your own character's 3D model.",
		section = localPlayerSection
	)
	default boolean hideLocalPlayer()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "hideLocalPlayer2D",
		name = "Hide local player 2D",
		description = "Hides your own character's 2D elements (overhead text, health bar, name).",
		section = localPlayerSection
	)
	default boolean hideLocalPlayer2D()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		keyName = "hideNPCs",
		name = "Hide NPCs",
		description = "Hides all NPC 3D models. Use the sidebar panel for per-NPC control instead.",
		section = npcsSection
	)
	default boolean hideNPCs()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "hideNPCs2D",
		name = "Hide NPCs 2D",
		description = "Hides all NPC 2D elements (overhead text, health bars, names).",
		section = npcsSection
	)
	default boolean hideNPCs2D()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "hideDeadNpcs",
		name = "Hide dead NPCs",
		description = "Hides NPCs when their health reaches 0, including death animations.",
		section = npcsSection
	)
	default boolean hideDeadNpcs()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hideWorldEntities",
		name = "Hide boats",
		description = "Hides boat entities in the game world.",
		section = npcsSection
	)
	default boolean hideWorldEntities()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		keyName = "hidePets",
		name = "Hide others' pets",
		description = "Hides other players' follower pets. Your own pet is not affected.",
		section = otherSection
	)
	default boolean hidePets()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "hideAttackers",
		name = "Hide attackers",
		description = "Hides NPCs and players currently attacking you.",
		section = otherSection
	)
	default boolean hideAttackers()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "hideProjectiles",
		name = "Hide projectiles",
		description = "Hides all projectile animations (arrows, spells, etc).",
		section = otherSection
	)
	default boolean hideProjectiles()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hideThralls",
		name = "Hide thralls",
		description = "Hides thrall NPCs summoned via the Arceuus spellbook.",
		section = otherSection
	)
	default boolean hideThralls()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "hideRandomEvents",
		name = "Hide others' random events",
		description = "Hides random event NPCs that belong to other players.",
		section = otherSection
	)
	default boolean hideRandomEvents()
	{
		return false;
	}

	@ConfigItem(
		keyName = "manuallyHiddenNpcIds",
		name = "",
		description = "",
		hidden = true
	)
	default String manuallyHiddenNpcIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "manuallyHiddenNpcIds",
		name = "",
		description = "",
		hidden = true
	)
	void setManuallyHiddenNpcIds(String ids);
}
