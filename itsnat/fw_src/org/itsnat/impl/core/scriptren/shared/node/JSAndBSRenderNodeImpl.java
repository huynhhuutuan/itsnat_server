/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2014 Jose Maria Arranz Santamaria, Spanish citizen

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

package org.itsnat.impl.core.scriptren.shared.node;

import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.itsnat.impl.core.scriptren.shared.JSAndBSRenderImpl;

/**
 *
 * @author jmarranz
 */
public class JSAndBSRenderNodeImpl extends JSAndBSRenderImpl
{
   
    public static String getNodeLocation(NodeLocationImpl nodeLoc,boolean errIfNull)
    {
        String nodeRef = nodeLoc.toScriptNodeLocation(errIfNull);
        if (nodeRef.equals("null")) return "null";
        return nodeRef;
    }        
    
    public static String addNodeToCache(NodeLocationImpl nodeLoc)
    {
        return "itsNatDoc.addNodeCache(" + nodeLoc.toScriptNodeLocation(true) + ");\n";
    }
}
