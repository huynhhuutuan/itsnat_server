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

package org.itsnat.impl.core.scriptren.bsren.node;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.domutil.NodeConstraints;
import org.itsnat.impl.core.scriptren.shared.node.CannotInsertAsMarkupCauseImpl;
import org.itsnat.impl.core.scriptren.shared.node.InnerMarkupCodeImpl;
import org.itsnat.impl.core.scriptren.shared.node.InsertAsMarkupInfoImpl;
import org.itsnat.impl.core.scriptren.shared.node.JSAndBSRenderElementImpl;
import org.itsnat.impl.core.scriptren.shared.node.RenderElement;
import org.itsnat.impl.core.template.MarkupTemplateVersionImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public abstract class BSRenderElementImpl extends BSRenderHasChildrenNodeImpl implements RenderElement
{
    public static boolean SUPPORT_INSERTION_AS_MARKUP = true;
    
    protected final NodeConstraints isChildNotValidInsertedAsMarkupListener = new NodeConstraints()
    {
        @Override
        public boolean match(Node node, Object context)
        {
            return isChildNotValidInsertedAsMarkup(node,(MarkupTemplateVersionImpl)context);
        }
    };
            
    protected final NodeConstraints isElementWithRemoteAttributeListener = new NodeConstraints()
    {
        @Override
        public boolean match(Node node, Object context)
        {
            if (!(node instanceof Element))
                return false; // No es un element

            if (!node.hasAttributes()) return false; // No hay problema en meter en el setInnerXML
          
            NamedNodeMap attribList = node.getAttributes();
            int len = attribList.getLength();
            for(int i = 0; i < len; i++)
            {
                Attr attr = (Attr)attribList.item(i);
                if (BSRenderAttributeImpl.isAttrRemote(attr))
                    return true; // Es un Element con un atributo remoto
            }            
            
            return false;
        }
    };    
    
    /** Creates a new instance of BSRenderElementImpl */
    public BSRenderElementImpl()
    {
    }

    public static BSRenderElementImpl getBSRenderElement(Element elem)
    {
        if ("script".equals(elem.getTagName()))
            return BSRenderElementScriptImpl.SINGLETON;
        else
            return BSRenderElementViewImpl.SINGLETON;
    }

    @Override    
    public NodeConstraints getIsChildNotValidInsertedAsMarkupListener()
    {
        return isChildNotValidInsertedAsMarkupListener;
    }    
    
    @Override
    public String createNodeCode(Node node,ClientDocumentStfulDelegateImpl clientDoc)
    {
        Element nodeElem = (Element)node;
        return createElement(nodeElem,clientDoc);
    }

    protected String createElement(Element nodeElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        String tagName = nodeElem.getTagName();
        return createElement(nodeElem,tagName,clientDoc);
    }

    protected abstract String createElement(Element nodeElem,String tagName,ClientDocumentStfulDelegateImpl clientDoc);

    @Override
    public String addAttributesBeforeInsertNode(Node node,String elemVarName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // En Droid la renderizaci�n de atributos se hace con una �nica instancia de BSRenderAttributeImpl por lo que hay una �nica forma compartida
        // de renderizar entre atributos (eso no lo podemos hacer en web, pero en Web afortunadamente se utiliza mucho el innerHTML) 
        // podemos considerar una estrategia de definir atributos con una s�la sentencia a modo de batch, se enviar� mucho menos c�digo y ser� m�s r�pido de parsear en beanshell
        
        Element elem = (Element)node;
        BSRenderAttributeImpl renderAttr = BSRenderAttributeImpl.getBSRenderAttribute();          
        StringBuilder code = new StringBuilder();
        
        NamedNodeMap attribList = elem.getAttributes();    
        if (attribList.getLength() <= 1)
        {
            // No vale la pena el batch
            for(int i = 0; i < attribList.getLength(); i++)
            {
                Attr attr = (Attr)attribList.item(i);
                code.append(renderAttr.setAttributeCode(attr,elem,elemVarName,clientDoc) );
            }
       }
       else
       {
            Map<String,List<Attr>> mapByNamespace = new HashMap<String,List<Attr>>();
            List<Attr> listNoNamespace = new LinkedList<Attr>();            
            for(int i = 0; i < attribList.getLength(); i++)
            {
                Attr attr = (Attr)attribList.item(i);
                String ns = attr.getNamespaceURI();
                if (ns != null)
                {
                    List<Attr> list = mapByNamespace.get(ns);
                    if (list == null) 
                    {
                        list = new LinkedList<Attr>();
                        mapByNamespace.put(ns,list);
                    }
                    list.add(attr);
                }
                else listNoNamespace.add(attr);
            }       
            
            if (!mapByNamespace.isEmpty())
            {
                code.append(renderAttr.setAttributeCodeBatchNS(elem,elemVarName,mapByNamespace,clientDoc) );
            }
           
            if (!listNoNamespace.isEmpty())
            {
                code.append(renderAttr.setAttributeCodeBatch(elem,elemVarName,listNoNamespace,clientDoc) );                
            }
       }
           
        return code.toString();
    }

    @Override
    public Object appendChildNodes(Node parent, String parentVarName,boolean beforeParent,InsertAsMarkupInfoImpl insertMarkupInfo,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (SUPPORT_INSERTION_AS_MARKUP)
        {        
            CannotInsertAsMarkupCauseImpl cannotInsertMarkup = canInsertAllChildrenAsMarkup((Element)parent,clientDoc.getItsNatStfulDocument().getItsNatStfulDocumentTemplateVersion(),insertMarkupInfo);
            if (cannotInsertMarkup == null)
            {
                // Sabemos que el retorno, innerMarkup, nunca es nulo en este contexto
                InnerMarkupCodeImpl innerMarkup = appendChildrenAsMarkup(parentVarName,parent,clientDoc);
                return innerMarkup;
            }
            else
            {
                InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = cannotInsertMarkup.createInsertAsMarkupInfoNextLevel(); // Puede ser null
                return super.appendChildNodes(parent,parentVarName,beforeParent,insertMarkupInfoNextLevel,clientDoc);
            }
        }
        else
        {
            InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = null; // Puede ser null
            return super.appendChildNodes(parent,parentVarName,beforeParent,insertMarkupInfoNextLevel,clientDoc);            
        }
    }    
       
    @Override    
    public Object getInsertNewNodeCode(Node newNode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        if (SUPPORT_INSERTION_AS_MARKUP)
        {
            CannotInsertAsMarkupCauseImpl cannotInsertMarkup = canInsertSingleChildNodeAsMarkup(newNode,clientDoc);
            if (cannotInsertMarkup == null)
                return appendSingleChildNodeAsMarkup(newNode,clientDoc);
            else
            {
                InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = cannotInsertMarkup.createInsertAsMarkupInfoNextLevel(); // Puede ser null
                return super.getInsertNewNodeCode(newNode,insertMarkupInfoNextLevel,clientDoc);
            }
        }
        else
        {
            InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = null;
            return super.getInsertNewNodeCode(newNode,insertMarkupInfoNextLevel,clientDoc);
        }
    }
    
    @Override
    public boolean isAddChildNodesBeforeNode(Node parent,ClientDocumentStfulDelegateImpl clientDoc)
    {
         return false;
    }    
    
    @Override
    public String getAppendChildrenCodeAsMarkupSentence(InnerMarkupCodeImpl innerMarkupRender,ClientDocumentStfulDelegateImpl clientDoc)
    {
        Element parent = innerMarkupRender.getParentNode();
        Node elemWithRemAttr = DOMUtilInternal.getFirstContainedNodeMatching(parent,isElementWithRemoteAttributeListener,null);

        boolean isAttrRemote = elemWithRemAttr != null;
        String metadataPrefix = "";
        String metadataSuffix = "";
        String className = "";
        if (isAttrRemote)
        {
            metadataPrefix = "/*[i*/";  // i = innerXML
            metadataSuffix = "/*i]*/"; 
            className = "\"" + parent.getTagName() + "\","; // Nuevo param
        }        
        
        String parentNodeLocator = innerMarkupRender.getParentNodeLocator();
        String valueBS = toTransportableStringLiteral(innerMarkupRender.getInnerMarkup(),clientDoc.getBrowser());
        if (innerMarkupRender.isUseNodeLocation())
            return "itsNatDoc.setInnerXML2(" + parentNodeLocator + "," + metadataPrefix + className + valueBS + metadataSuffix + ");\n";
        else // Es directamente una variable
            return "itsNatDoc.setInnerXML(" + parentNodeLocator + "," + metadataPrefix + className + valueBS + metadataSuffix + ");\n";
    }

    private CannotInsertAsMarkupCauseImpl canInsertSingleChildNodeAsMarkup(Node newChildNode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        return JSAndBSRenderElementImpl.canInsertSingleChildNodeAsMarkup(newChildNode, clientDoc, this);
    }

    private InnerMarkupCodeImpl appendSingleChildNodeAsMarkup(Node newNode, ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        return JSAndBSRenderElementImpl.appendSingleChildNodeAsMarkup(newNode,clientDoc,this);
    }

    private InnerMarkupCodeImpl appendChildrenAsMarkup(String parentVarName, Node parentNode, ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderElementImpl.appendChildrenAsMarkup(parentVarName,parentNode,clientDoc,this);
    }

    @Override
    public boolean isInsertChildNodesAsMarkupCapable(Element parent,MarkupTemplateVersionImpl template)
    {
        // En principio todos los elementos tienen capacidad de insertar nodos hijos como markup
        // a trav�s de nuestro setInnerXML 
        
        return true;
    }

    public boolean isChildNotValidInsertedAsMarkup(Node childNode,MarkupTemplateVersionImpl template)
    {
        /*
        // Realmente s�lo hay elementos pues los nodos de texto son como mucho de espacios
        
        // Sin embargo si usamos setInnerXML y no DOM normal perdemos la capacidad de parsear los atributos remotos a partir del c�digo fuente DOM generado
        // Por ello parseamos si hay atributos remotos en los nodos que van a renderizarse para setInnerXML

        if (!childNode.hasAttributes()) return false; // No hay problema en meter en el setInnerXML
        
        NamedNodeMap attribList = childNode.getAttributes();
        int len = attribList.getLength();
        for(int i = 0; i < len; i++)
        {
            Attr attr = (Attr)attribList.item(i);
            String value = attr.getValue();
            if (value.startsWith("@remote:"))
                return true; // NO es v�lido
        }
        */
        
        return false; // Todos son v�lidos
    }

    @Override
    public CannotInsertAsMarkupCauseImpl canInsertChildNodeAsMarkupIgnoringOther(Element parent,Node childNode,MarkupTemplateVersionImpl template)
    {
        return JSAndBSRenderElementImpl.canInsertChildNodeAsMarkupIgnoringOther(parent, childNode, template, this);
    }

    public boolean canInsertAllChildrenAsMarkup(Element parent,MarkupTemplateVersionImpl template)
    {
        CannotInsertAsMarkupCauseImpl cannotInsertMarkup = canInsertAllChildrenAsMarkup(parent,template,null);
        return (cannotInsertMarkup == null);
    }

    public CannotInsertAsMarkupCauseImpl canInsertAllChildrenAsMarkup(Element parent,MarkupTemplateVersionImpl template,InsertAsMarkupInfoImpl insertMarkupInfo)
    {
        return JSAndBSRenderElementImpl.canInsertAllChildrenAsMarkup(parent,template,insertMarkupInfo,this);
    }

    @Override
    public Node getFirstChildIsNotValidInsertedAsMarkup(Element parent,MarkupTemplateVersionImpl template)
    {
        return DOMUtilInternal.getFirstContainedNodeMatching(parent,isChildNotValidInsertedAsMarkupListener,template);
    }

    @Override
    public InnerMarkupCodeImpl appendChildrenCodeAsMarkup(String parentVarName,Element parentNode,String childrenCode,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderElementImpl.createInnerMarkupCode(parentVarName, parentNode, childrenCode, clientDoc, this);
    }
    
}
