/*
 * This file is not part of the ItsNat framework.
 *
 * Original source code use and closed source derivatives are authorized
 * to third parties with no restriction or fee.
 * The original source code is owned by the author.
 *
 * This program is distributed AS IS in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * (C) Innowhere Software a service of Jose Maria Arranz Santamaria, Spanish citizen.
 */

package org.itsnat.feashow;

import org.itsnat.comp.tree.ItsNatFreeTree;

/**
 *
 * @author jmarranz
 */
public class MainTreeDecorator extends FreeTreeDecorator
{
    public MainTreeDecorator(ItsNatFreeTree comp)
    {
        super(comp);
    }

    public int getMaxLevelInitiallyShown()
    {
        return 3;
    }
}
