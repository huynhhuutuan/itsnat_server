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

package org.itsnat.impl.core.scriptren.shared;

import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocStfulTask;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.NodeCacheRegistryImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public class JSAndBSScriptUtilFromDocSharedImpl
{
    protected ScriptUtilImpl scriptUtil;
    protected ItsNatStfulDocumentImpl itsNatDoc;
    
    public JSAndBSScriptUtilFromDocSharedImpl(ScriptUtilImpl scriptUtil,ItsNatStfulDocumentImpl itsNatDoc)
    {
        this.scriptUtil = scriptUtil;
        this.itsNatDoc = itsNatDoc;
    }
    
    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return itsNatDoc;
    }

    public ClientDocumentStfulDelegateImpl getCurrentClientDocumentStfulDelegate()    
    {
        ClientDocumentStfulImpl clientDoc = itsNatDoc.getRequestingClientDocumentStful();
        return clientDoc.getClientDocumentStfulDelegate();        
    }
    
    public void checkAllClientsCanReceiveScriptCode()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        if (!itsNatDoc.allClientDocumentWillReceiveCodeSent())
            throw new ItsNatException("Some client cannot receive JavaScript code",this);
    }    
    
    public boolean preventiveNodeCaching2(Node node)
    {
        if (itsNatDoc.hasClientDocumentAttachedClient())
            return preventiveNodeCachingTwoOrMoreClient(node);
        else
            return preventiveNodeCachingOneClient(node); // Caso de �nico cliente (el owner)
    }

    protected boolean preventiveNodeCachingTwoOrMoreClient(final Node node)
    {
        // Evitamos cachear con un nuevo id si todos los clientes ya usan
        // dicho id, es el caso de un nodo que ya fue "cacheado preventivamente"
        // por las vistas y se quiere una nueva referencia como string.
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        ClientDocumentStfulImpl clientDocOwner = itsNatDoc.getClientDocumentStfulOwner();
        ClientDocumentStfulDelegateImpl clientDocOwnerDeleg = clientDocOwner.getClientDocumentStfulDelegate();
        NodeCacheRegistryImpl nodeCache = clientDocOwnerDeleg.getNodeCacheRegistry(); // No puede ser nula
        final String oldId = nodeCache.getId(node);

        ClientDocStfulTask clientTask = new ClientDocStfulTask()
        {
            public boolean doTask(ClientDocumentStfulImpl clientDoc,Object arg)
            {
                ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();
                boolean cached = scriptUtil.isNodeCachedWithId(node,oldId,clientDocDeleg);
                return cached; // true = continuar
            }
        };
        boolean res = itsNatDoc.executeTaskOnClients(clientTask,null);
        if (res) return false; // Ya est� cacheado por todos los clientes con el mismo id

        // Debe generarse un nuevo id por el documento pues algunos ids pueden compartirse entre cach�s de un mismo documento como es este caso
        String id = NodeCacheRegistryImpl.generateUniqueId(itsNatDoc);

        ClientDocumentStfulImpl[] allClient = itsNatDoc.getAllClientDocumentStfulsCopy();
        for(int i = 0; i < allClient.length; i++)
        {
            ClientDocumentStfulImpl clientDoc = allClient[i];      
            ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();             
            scriptUtil.preventiveNodeCaching(node,id,clientDocDeleg);
        }

        return true;
    }

    public boolean preventiveNodeCachingOneClient(Node node)
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        ClientDocumentStfulImpl clientDoc = itsNatDoc.getClientDocumentStfulOwner();
        ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();         
        return scriptUtil.preventiveNodeCachingOneClient(node,clientDocDeleg);
    }

    
}
