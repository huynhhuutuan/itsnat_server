/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2011 Jose Maria Arranz Santamaria, Spanish citizen

  This software is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3 of
  the License, or (at your option) any later version.
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details. You should have received
  a copy of the GNU Lesser General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
*/

package org.itsnat.impl.core.domutil;

import java.util.ArrayList;
import org.itsnat.core.domutil.ListElementInfo;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class ElementTableFreeMasterImpl extends ElementTableFreeImpl
{

    /**
     * Creates a new instance of ElementTableFreeMasterImpl
     */
    public ElementTableFreeMasterImpl(ItsNatDocumentImpl itsNatDoc,Element parentElement)
    {
        super(itsNatDoc,true,parentElement);

        this.columnListOfRow = new ArrayList<ElementListBaseImpl>();
        createAndSyncColumnArrayList();
    }

    @Override
    public Element addRow2(Element rowElem)
    {
        rowElem = super.addRow2(rowElem);

        addColumnListOfRow(rowElem);

        return rowElem;
    }

    @Override
    public Element insertRowAt2(int row,Element rowElem)
    {
        rowElem = super.insertRowAt2(row,rowElem);

        insertColumnListOfRow(row,rowElem);

        return rowElem;
    }

    @Override
    public ElementPair setRowAt2(int row,Element rowElem)
    {
        ElementPair res = super.setRowAt2(row,rowElem);

        rowElem = res.getNewElem(); // El verdaderamente insertado

        updateColumnListOfRow(row,rowElem);

        return res;
    }

    public void updateColumnListOfRow(int row,Element rowElem)
    {
        // Hay que crear una nueva lista porque ha cambiado el padre de la lista
        ElementListBaseImpl columsOfRow = newColumnsOfRowElementList(row,rowElem);
        columnListOfRow.set(row,columsOfRow);
    }

    public ElementListBaseImpl getColumnsOfRowElementList(int row,Element rowElem)
    {
        return columnListOfRow.get(row);
    }

    public TableCellElementInfoImpl getTableCellElementInfo(ListElementInfo rowInfo, ListElementInfo cellInfo)
    {
        return TableCellElementInfoMasterImpl.getTableCellElementInfoMaster((ListElementInfoMasterImpl)rowInfo,(ListElementInfoMasterImpl)cellInfo,this);
    }
}
