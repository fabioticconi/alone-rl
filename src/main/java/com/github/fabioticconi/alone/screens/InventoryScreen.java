/*
 * Copyright (C) 2017 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.fabioticconi.alone.screens;

import asciiPanel.AsciiPanel;
import com.artemis.ComponentMapper;
import com.artemis.utils.BitVector;
import com.artemis.utils.IntBag;
import com.github.fabioticconi.alone.components.Equip;
import com.github.fabioticconi.alone.components.Inventory;
import com.github.fabioticconi.alone.components.Name;
import com.github.fabioticconi.alone.systems.ActionSystem;
import com.github.fabioticconi.alone.systems.ItemSystem;
import com.github.fabioticconi.alone.systems.ScreenSystem;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_Z;

/**
 * Author: Fabio Ticconi
 * Date: 02/11/17
 */
public abstract class InventoryScreen extends AbstractScreen
{
    ComponentMapper<Name>      mName;
    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Equip>     mEquip;

    ScreenSystem screen;
    ActionSystem sAction;
    ItemSystem   sItems;

    @Override
    public float handleKeys(final BitVector keys)
    {
        if (keys.get(KeyEvent.VK_ESCAPE))
            screen.select(PlayScreen.class);

        keys.clear();

        return 0f;
    }

    @Override
    public void display(final AsciiPanel terminal)
    {
        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();

        terminal.clear(' ');

        drawHeader(terminal);

        final Inventory inv = mInventory.get(playerId);

        final ArrayList<String> elements = new ArrayList<>();
        final IntBag            items    = inv.items;
        for (int i = 0, size = items.size(); i < size; i++)
        {
            final int itemId = items.get(i);

            if (!canDraw(itemId))
                continue;

            elements.add(String.format("%s %s %s",
                                       Letter.values()[i],
                                       mName.get(itemId).name.toLowerCase(),
                                       mEquip.has(itemId) ? " [WORN]" : ""));
        }

        drawList(terminal, elements);
    }

    public abstract String header();

    public abstract boolean canDraw(final int entityId);

    int getItem(final BitVector keys)
    {
        final int pos = getTargetIndex(keys);

        if (pos < 0)
            return -1;

        final int playerId = pManager.getEntitiesOfPlayer("player").get(0).getId();
        final Inventory i = mInventory.get(playerId);

        if (pos < i.items.size())
            return i.items.get(pos);

        return -1;
    }
}
