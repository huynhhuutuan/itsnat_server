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

import java.util.ArrayList;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.NodeCacheRegistryImpl;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public class NodeLocationWithParentImpl extends NodeLocationImpl
{
    protected NodeLocationImpl nodeLocationDeleg;
    protected Node cachedParent;
    protected String cachedParentId;
    protected ArrayList<String> newCachedParentIds;

    private NodeLocationWithParentImpl(Node node,String id,String path,Node cachedParent,String cachedParentId,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        super(clientDoc);

        if (node == null) throw new ItsNatException("INTERNAL ERROR");           
        
        this.nodeLocationDeleg = getNodeLocationNotParent(node, id, path, clientDoc);

        this.cachedParent = cachedParent;
        this.cachedParentId = cachedParentId;

        NodeCacheRegistryImpl nodeCache = clientDoc.getNodeCacheRegistry();
        if ((nodeCache != null) && cacheIfPossible) // Aunque est� cacheado el nodo principal aprovechamos para cachear los padres.
        {
            // Cacheamos unos cuantos padres inmediatos para que los nodos "adyacentes" (de la zona en general)
            // puedan encontrarse m�s r�pidamente, sobre todo si el cachedParent no se encontr� o est� muy arriba.

            int maxParents = 3; // Un valor razonable para evitar cachear en exceso nodos padre (y enviar demasiado JavaScript)
                                // que a lo mejor no se usan nunca ni para el c�lculo de paths
            Node currParent = node.getParentNode();
            for(int i = 0; (currParent != null) && (currParent != cachedParent) && (i < maxParents); i++)
            {
                String parentId = nodeCache.getId(currParent);
                if (parentId == null) // No cacheado
                {
                    parentId = nodeCache.addNode(currParent);
                    if (parentId != null)
                    {
                        // Hemos cacheado un nuevo nodo, DEBEMOS LLAMAR toJSArray y enviar al cliente
                        // de otra manera el cliente NO se enterar� de este cacheado.
                        if (newCachedParentIds == null)
                            this.newCachedParentIds = new ArrayList<String>(maxParents);
                        newCachedParentIds.add(parentId);
                        currParent = currParent.getParentNode();
                        i++;
                    }
                    else currParent = null; // No se puede cachear, paramos
                }
                else currParent = null; // Ya cacheado, paramos
            }
        }
        
        if ((nodeLocationDeleg instanceof NodeLocationAlreadyCachedNotParentImpl) && !isNull(cachedParentId)) throw new ItsNatException("INTERNAL ERROR");        
    }

    public NodeLocationWithParentImpl(Node node,String id,String path,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        this(node,id,path,null,null,cacheIfPossible,clientDoc);
    }

    @Override
    public Node getNode()
    {
        return nodeLocationDeleg.getNode();
    }
    
    public boolean isJustCached()
    {
        return nodeLocationDeleg.isJustCached();
    }    
    
    private String getCachedParentId()
    {
        return cachedParentId;
    }

    /* Este m�todo no se necesita fuera */
    protected String getCachedParentIdAsScript()
    {
        return toLiteralStringScript(getCachedParentId());
    }

    public String toScriptNodeLocation(boolean errIfNull)
    {
        this.used = true;

        nodeLocationDeleg.setUsed();
        
        if (nodeLocationDeleg instanceof NodeLocationAlreadyCachedNotParentImpl)
        {
            NodeLocationAlreadyCachedNotParentImpl nodeLocDeleg = (NodeLocationAlreadyCachedNotParentImpl)nodeLocationDeleg;
            if (newCachedParentIds == null)
                return toScriptArray ( nodeLocDeleg.getIdAsScript() ); // 1 item
            else
            {
                return toScriptArray( nodeLocDeleg.getIdAsScript() + "," + toScriptArrayCachedParents() );
            }
        }
        else if (nodeLocationDeleg instanceof NodeLocationPathBasedNotParentImpl)
        {
            NodeLocationPathBasedNotParentImpl nodeLocDeleg = (NodeLocationPathBasedNotParentImpl)nodeLocationDeleg;            
            
            StringBuilder code = new StringBuilder();

            code.append( getCachedParentIdAsScript() + "," + nodeLocDeleg.getIdAsScript() + "," + nodeLocDeleg.getPathAsScript() );  // 3 items
            if (newCachedParentIds != null)
                code.append( "," + toScriptArrayCachedParents() ); // 4 items (el �ltimo un array dentro de array)

            return toScriptArray( code.toString() );
        }
        else throw new ItsNatException("INTERNAL ERROR");
    }

    protected String toScriptArrayCachedParents()
    {
        // Array dentro de array (el que llama)
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < newCachedParentIds.size(); i++)
        {
            String parentId = newCachedParentIds.get(i);
            String parentIdScript = toLiteralStringScript(parentId);
            if (i != 0) code.append(",");
            code.append(parentIdScript);
        }
        
        return toScriptArray( code.toString() );
    }

    public static NodeLocationWithParentImpl getNodeLocationWithParentUsingCache(Node node,String id,boolean cacheIfPossible,NodeCacheRegistryImpl nodeCache)
    {
        // nodeCache NO puede ser nulo

        // Se supone que el nodo no ha sido cacheado en el cliente todav�a  aunque tenga
        // un id asignado en el servidor (este id puede ser null si no ha sido cacheado en el servidor)
        // por lo que hay que obtener un path, absoluto o relativo respecto a un padre cacheado.

        // Buscamos un nodo padre que est� cacheado para evitar formar un path
        // absoluto que lleva tiempo.

        String parentId = null;
        Node parent = node;

        do
        {
            parent = parent.getParentNode();
            parentId = nodeCache.getId(parent); // si cachedParent es null devuelve null
        }
        while((parentId == null)&&(parent != null));

        ClientDocumentStfulDelegateImpl clientDoc = nodeCache.getClientDocumentStfulDelegate();
        String path = clientDoc.getStringPathFromNode(node,parent); // Si cachedParent es null (cachedParentId es null) devuelve un path absoluto

        return new NodeLocationWithParentImpl(node,id,path,parent,parentId,cacheIfPossible,clientDoc);
    }

    public static NodeLocationWithParentImpl getNodeLocationWithParent(Node node,String id,String path,Node parent,String parentId,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return new NodeLocationWithParentImpl(node,id,path,parent,parentId,cacheIfPossible,clientDoc);
    }

    public static NodeLocationWithParentImpl getNodeLocationWithParent(Node node,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // Si cacheIfPossible es true y se cachea el nodo, el location DEBE enviarse al cliente y resolverse para cachear en el cliente

        NodeCacheRegistryImpl nodeCache = clientDoc.getNodeCacheRegistry();
        if (nodeCache != null)
        {
            String id = nodeCache.getId(node);
            if (id != null)
            {
                return new NodeLocationWithParentImpl(node,id,null,cacheIfPossible,clientDoc); // S�lo se necesita el id del nodo, cuando est� en la cach� no necesitamos el string path que es una tarea que consume mucho tiempo tanto en el servidor como en el cliente
            }
            else // No cacheado
            {
                if (cacheIfPossible)
                {
                    id = nodeCache.addNode(node); // Si el nodo no es cacheable o la cach� est� bloqueada (s�lo lectural) devuelve null, no pasa nada por no poder cachear
                    return getNodeLocationWithParentUsingCache(node,id,true,nodeCache);
                }
                else
                {
                    String path = clientDoc.getStringPathFromNode(node);
                    return new NodeLocationWithParentImpl(node,null,path,false,clientDoc); // cacheIfPossible es false
                }
            }
        }
        else // El documento tiene el cache desactivado
        {
            String path = clientDoc.getStringPathFromNode(node);
            return new NodeLocationWithParentImpl(node,null,path,cacheIfPossible,clientDoc);
        }
    }

}
