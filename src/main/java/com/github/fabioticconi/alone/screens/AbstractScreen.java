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
import com.artemis.managers.PlayerManager;
import com.artemis.utils.BitVector;
import com.github.fabioticconi.alone.components.Inventory;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import java.util.Collections;
import java.util.List;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_Z;

/**
 * Author: Fabio Ticconi
 * Date: 01/11/17
 */
public abstract class AbstractScreen extends PassiveSystem implements Screen
{
    PlayerManager pManager;

    public enum Letter
    {
        a,
        b,
        c,
        d,
        e,
        f,
        g,
        h,
        i,
        j,
        k,
        l,
        m,
        n,
        o,
        p,
        q,
        r,
        s,
        t,
        u,
        v,
        w,
        x,
        y,
        z;

        @Override
        public String toString()
        {
            return name() + ")";
        }
    }

    public abstract String header();

    void drawHeader(final AsciiPanel terminal)
    {
        final String header = header();
        terminal.writeCenter(header, 2);
        terminal.writeCenter(String.join("", Collections.nCopies(header.length(), "-")), 3);
    }

    void drawList(final AsciiPanel terminal, final List<String> list)
    {
        final int maxSize = AbstractScreen.Letter.values().length;
        final int size = Math.min(maxSize, list.size());

        for (int i = 0, starty = terminal.getHeightInCharacters() / 2 - size / 2; i < size; i++)
        {
            final String entry = list.get(i);

            terminal.writeCenter(entry, starty + (size < maxSize / 2 ? i * 2 : i));
        }
    }

    /**
     * Returns a number between 0 and 25, corresponding to letters A to Z (eg, possible selections).
     *
     * @param keys
     * @return
     */
    int getTargetIndex(final BitVector keys)
    {
        final int keyCode = keys.nextSetBit(VK_A);

        if (keyCode >= VK_A && keyCode <= VK_Z)
        {
            keys.clear(keyCode);

            return keyCode - VK_A;
        }

        return -1;
    }
}
