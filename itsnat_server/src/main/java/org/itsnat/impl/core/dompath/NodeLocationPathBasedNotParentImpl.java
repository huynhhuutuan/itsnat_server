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

package org.itsnat.impl.core.dompath;

import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import static org.itsnat.impl.core.dompath.NodeLocationImpl.isNull;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public class NodeLocationPathBasedNotParentImpl extends NodeLocationNotNullImpl
{
     protected String path;
     
    public NodeLocationPathBasedNotParentImpl(Node node,String id,String path,ClientDocumentStfulDelegateImpl clientDoc)
    {
        super(node,id,clientDoc);
        
        this.path = path;       
        
        // Se supone que esta clase es para nodos no cacheados en donde necesitamos el path, el id puede ser nulo (es el caso de no poder cachear) pero el path no
        if (isNull(path))
            throw new ItsNatException("Node not bound to document tree",node);   
    }

    public boolean isCached()
    {
        // O ya estaba cacheado o se acaba de cachear
        return !isNull(id);
    }
    
    private String getPath()
    {
        return path;
    }

    /* Este m�todo no se necesita fuera */
    protected String getPathAsScript()
    {
        return toLiteralStringScript(getPath());
    }    
    
    public boolean isJustCached()
    {
        // Si se acaba de cachear, aparte del id el path debe de estar definido porque se va a enviar al cliente para que se cachee tambi�n all�, sea absoluto o relativo respecto al padre
        return !isNull(id) && !isNull(path);
    }      
    
    public String toScriptNodeLocation(boolean errIfNull)
    {
        this.used = true;

        return toScriptArray( getIdAsScript() + "," + getPathAsScript() );
    }
}
