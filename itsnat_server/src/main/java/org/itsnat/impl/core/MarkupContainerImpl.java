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

package org.itsnat.impl.core;

import java.io.Serializable;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.markup.render.DOMRenderImpl;
import org.itsnat.impl.core.servlet.ItsNatServletImpl;
import org.itsnat.impl.core.template.*;
import org.itsnat.impl.core.util.MapUniqueId;
import org.itsnat.impl.core.util.UniqueId;
import org.itsnat.impl.core.util.UniqueIdGenIntList;
import org.w3c.dom.Document;

/**
 *
 * @author jmarranz
 */
public abstract class MarkupContainerImpl implements Serializable
{
    protected transient MapUniqueId<MarkupTemplateVersionImpl> usedTemplatesWithCachedNodes; // En el caso de un MarkupTemplateVersionImpl est�n los fragments incluidos, en el caso de ItsNatDocumentImpl est� tambi�n el propio template del documento (y tambi�n sus fragmentos)
    // No sincronizamos porque s�lo se modifica en carga del markup del template (monohilo) o por el ItsNatDocument (usado siempre en monohilo)
    protected UniqueIdGenIntList idGenerator = new UniqueIdGenIntList(false); // No sincronizamos porque s�lo se usa para cachear los nodos en tiempo de carga si es un template o para generar ids como ItsNatDocument (usado siempre en monohilo)
    protected UniqueId idObj;

    /** Creates a new instance of MarkupContainerImpl */
    public MarkupContainerImpl(UniqueIdGenIntList idGenerator)
    {
        this.idObj = idGenerator.generateUniqueId(getIdGenPrefix());
        // No confundir el par�metro idGenerator del constructor que genera el idObj de este objeto (template o documento)
        // con el generator propio de este objeto para generar nuevos ids de elementos dependientes
    }

    public abstract ItsNatServletImpl getItsNatServlet();

    public abstract String getIdGenPrefix();

    public UniqueIdGenIntList getUniqueIdGenerator()
    {
        return idGenerator;
    }

    public abstract Document getDocument();

    public abstract boolean hasCachedNodes();

    public MapUniqueId<MarkupTemplateVersionImpl> getUsedTemplateVersionsWithCachedNodes()
    {
        if (usedTemplatesWithCachedNodes == null)
        {
            ItsNatServletImpl itsNatServlet = getItsNatServlet();
            this.usedTemplatesWithCachedNodes = createUsedTemplateVersionsWithCachedNodesMap(itsNatServlet);
        }
        return usedTemplatesWithCachedNodes;
    }

    protected MapUniqueId<MarkupTemplateVersionImpl> createUsedTemplateVersionsWithCachedNodesMap(ItsNatServletImpl itsNatServlet)
    {
        // Los ids de los template versions deben estar generados siempre por el mismo
        // generador: el del servlet.
        // En el caso de ItsNatDocument hay un peque�o
        // retardo de sincronizaci�n a nivel de servlet al crear el usedTemplatesWithCachedNodes del documento
        // pero es asumible sobre todo en aplicaciones AJAX (se crean pocos ItsNatDocument) ????? (NO ME ACUERDO DE ESTO)

        return new MapUniqueId<MarkupTemplateVersionImpl>(itsNatServlet.getUniqueIdGenerator());
    }
    
    public MarkupTemplateVersionImpl getUsedMarkupTemplateVersion(String id)
    {
        return getUsedTemplateVersionsWithCachedNodes().get(id);
    }

    public void addUsedMarkupTemplateVersionWithCachedNodes(MarkupTemplateVersionImpl template)
    {
        // Si ya est� a�adido no hace nada, si ha cambiado el contenido del fragmento
        // como el idObj y el objeto ItsNatDocFragmentTemplateVersionImpl son diferentes, a todos los efectos
        // es como si fuera un nuevo fragmento, no intentamos quitar la anterior versi�n
        // pues no tenemos garant�as de que no quede
        // nada de la anterior versi�n en el documento.

        if (!template.hasCachedNodes()) return;

        MapUniqueId<MarkupTemplateVersionImpl> usedTemplates = getUsedTemplateVersionsWithCachedNodes();
        if (usedTemplates.containsKey(template))
            return; // Ya fue a�adido, en teor�a no puede cambiar (los templates contenidos son los mismos) pues ha de generarse una nueva versi�n, es decir un nuevo template (objecto e idObj) a todos los efectos

        usedTemplates.put(template);

        MapUniqueId<MarkupTemplateVersionImpl> templatesOfTemplate = template.getUsedTemplateVersionsWithCachedNodes();
        usedTemplates.putAll( templatesOfTemplate );
    }

    public String resolveCachedNodes(String text,boolean resolveEntities)
    {
        if (hasCachedNodes())
        {
            int start = text.indexOf(CachedSubtreeImpl.getMarkCodeStart());
            if (start == -1) return text;

            StringBuilder textRes = new StringBuilder();
            while (start != -1)
            {
                int end = text.indexOf('}',start);
                if (end == -1) throw new ItsNatException("INTERNAL ERROR"); // DEBE existir necesariamente el finalizador
                end++;
                String mark = text.substring(start,end);
                String templateId = CachedSubtreeImpl.getTemplateId(mark);
                MarkupTemplateVersionImpl template = getUsedMarkupTemplateVersion(templateId);

                MapUniqueId<CachedSubtreeImpl> cacheMap = template.getElementCacheMap();
                String nodeId = CachedSubtreeImpl.getNodeId(mark);
                CachedSubtreeImpl cachedNode = cacheMap.get(nodeId);

                textRes.append( text.substring(0,start) );
                String cachedCode = cachedNode.getCode(resolveEntities);
                cachedCode = resolveCachedNodes(cachedCode,resolveEntities); // Resuelve as� el caso de nodos cacheados de fragmentos includos con <include> dentro de nodos a su vez cacheados
                textRes.append( cachedCode );

                text = text.substring(start + mark.length());

                start = text.indexOf(CachedSubtreeImpl.getMarkCodeStart());
            }

            textRes.append( text );

            text = textRes.toString();
        }

        return text;
    }

    protected String serializeDocument(Document doc,DOMRenderImpl docRender,boolean resolveCachedNodes)
    {
        // El Document pasado ha debido ser creado a trav�s de este objeto
        // pues el docRender est� relacionado con el mismo (bueno con el original patr�n m�s exactamente)

        String text = docRender.serializeDocument(doc);

        if (resolveCachedNodes)
            text = resolveCachedNodes(text,false);

        return text;
    }

}
