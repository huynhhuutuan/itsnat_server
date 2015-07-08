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

package org.itsnat.impl.comp.tree;

import java.io.Serializable;
import java.util.LinkedList;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.itsnat.comp.tree.ItsNatTree;

/**
    Por ahora no consideramos nodos ocultos (collapsed) etc.
    todos cuentan, y yo creo que aunque se oculten no conviene
    quitar los elementos del DOM pues el rendimiento cae por los suelos
 *  y sin embargo necesitamos renderizar aunque est�n ocultos y el componente
    de renderizaci�n se basa en rows.
 *  Conclusi�n: row por ahora y quiz�s para siempre representa una visi�n
 *  del �rbol de nodos como una lista, se vean o no.
 *
 * @author jmarranz
 */
public class DefaultRowMapperImpl implements RowMapper,Serializable
{
    protected ItsNatTree tree;

    /**
     * Creates a new instance of DefaultRowMapperImpl
     */
    public DefaultRowMapperImpl(ItsNatTree tree)
    {
        this.tree = tree;
    }

    public TreeModel getTreeModel()
    {
        return tree.getTreeModel();
    }

    public int[] getRowsForPaths(TreePath[] path)
    {
        int[] rows = new int[path.length];
        for(int i = 0; i < rows.length; i++)
        {
            rows[i] = getRowForPath(path[i]);
        }
        return rows;
    }

    public int getRowForPath(TreePath path)
    {
        if (path == null)
            return -1;

        TreePath parentPath = path.getParentPath();
        if (parentPath == null)
            return 0; // El root

        // Suponemos que el TreePath es correcto sino obviamente el resultado ser� err�neo
        int rowParent = getRowForPath(parentPath);
        Object parentNode = parentPath.getLastPathComponent();
        TreeModel dataModel = getTreeModel();
        int count = dataModel.getChildCount(parentNode);
        int row = rowParent;
        Object targetNode = path.getLastPathComponent();
        for(int i = 0; i < count; i++)
        {
            Object childNode = dataModel.getChild(parentNode,i);
            if (childNode == targetNode)
            {
                row++;
                break;
            }
            else
            {
                row += getRowCountSubTree(childNode);
            }
        }

        return row;
    }
/*

    public int getRowForDataNode(Object dataNode)
    {
        TreeModel dataModel = getTreeModel();
        Object rootData = dataModel.getRoot();
        if (rootData == null)
            return -1;
        if (rootData == dataNode)
            return 0;
        boolean[] found = new boolean[1];
        int row = getRowForDataNode(rootData,dataNode,found);
        if (!found[0])
            return -1;
        return row;
    }

    public int getRowForDataNode(Object parentNode,Object dataNode,boolean[] found)
    {
        int row = 0;
        if (parentNode == dataNode)
        {
            found[0] = true;
            return row;
        }
        TreeModel dataModel = getTreeModel();
        int count = dataModel.getChildCount(parentNode);
        for(int i = 0; i < count; i++)
        {
            row++;
            Object childNode = dataModel.getChild(parentNode,i);
            row += getRowForDataNode(childNode,dataNode,found);
            if (found[0])
                return row;
        }
        return row;
    }
*/

    public int getRowCount()
    {
        // Por ahora no consideramos nodos ocultos (collapsed) etc.
        // todos cuentan. El root es la row cero.
        TreeModel dataModel = getTreeModel();
        Object root = dataModel.getRoot();
        if (root == null)
            return 0;
        return getRowCountSubTree(root);
    }

    public int getRowCountSubTree(Object dataNode)
    {
        if (dataNode == null) return 0;
        int rows = 1; // El propio nodo
        TreeModel dataModel = getTreeModel();
        int count = dataModel.getChildCount(dataNode);
        for(int i = 0; i < count; i++)
        {
            Object dataNodeChild = dataModel.getChild(dataNode,i);
            rows += getRowCountSubTree(dataNodeChild);
        }
        return rows;
    }

    public TreePath getPathForRow(int row)
    {
        if (row < 0) return null;
        TreeModel dataModel = getTreeModel();
        Object root = dataModel.getRoot();
        if (root == null)
            return null;
        if (row == 0)
            return new TreePath(root);

        LinkedList<Object> path = new LinkedList<Object>();
        //path.add(root);
        int currentRow = 0;
        getPathForRow(row,currentRow,root,path,dataModel);
        if ((path == null) || (path.size() == 0))
            return null;
        return new TreePath(path.toArray());
    }

    public Object getPathForRow(int row,int currentRow,Object dataNode,LinkedList<Object> path,TreeModel dataModel)
    {
        if (row == currentRow)
        {
            path.addLast(dataNode); // est� claro que es el �ltimo, aunque de hecho la lista deber�a estar nula
            return dataNode;
        }
        int count = dataModel.getChildCount(dataNode);
        for(int i = 0; i < count; i++)
        {
            currentRow++;
            Object dataNodeChild = dataModel.getChild(dataNode,i);
            Object res = getPathForRow(row,currentRow,dataNodeChild,path,dataModel);
            if (res != null)
            {
                path.addFirst(dataNode); // A�adimos el padre al path
                return res;
            }
            else
            {
                currentRow += getRowCountSubTree(dataNodeChild) - 1; // No contamos el propio nodo child pues lo hemos contado ya
            }
        }
        return null;
    }

}
