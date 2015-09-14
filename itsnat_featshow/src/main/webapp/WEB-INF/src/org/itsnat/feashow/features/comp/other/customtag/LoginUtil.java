package org.itsnat.feashow.features.comp.other.customtag;

import java.io.File;
import org.itsnat.comp.ItsNatComponentManager;
import org.itsnat.core.ItsNatDocument;
import org.itsnat.core.ItsNatServlet;
import org.itsnat.core.domutil.ItsNatTreeWalker;
import org.itsnat.core.http.ItsNatHttpServlet;
import org.itsnat.core.tmpl.ItsNatDocFragmentTemplate;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

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

/**
 *
 * @author jmarranz
 */
public class LoginUtil 
{
    
    public static void registerTemplate(ItsNatHttpServlet itsNatServlet,String name,String mime,String pathPrefix,String relPath)
    {
        String path = pathPrefix + relPath;
        if (!new File(path).exists()) throw new RuntimeException("Not found file:" + path);
        itsNatServlet.registerItsNatDocFragmentTemplate(name,mime, path);
    }        
    
    public static Element doTemplateLayout(String templateName,Element parentElem,ItsNatComponentManager compMgr)
    {
        // parentElem is a <login> element        
        ItsNatDocument itsNatDoc = compMgr.getItsNatDocument();        
        ItsNatServlet servlet = itsNatDoc.getItsNatDocumentTemplate().getItsNatServlet();
        ItsNatDocFragmentTemplate docFragTemplate = servlet.getItsNatDocFragmentTemplate(templateName);
        DocumentFragment docFrag = docFragTemplate.loadDocumentFragment(itsNatDoc);
        Element newParentElem = ItsNatTreeWalker.getFirstChildElement(docFrag);        
        parentElem.getParentNode().replaceChild(newParentElem,parentElem);
        return newParentElem;
    }    
    
    /*
    public static Element doMarkupLayout(String markup,Element parentElem,ItsNatComponentManager compMgr)
    {
        ItsNatDocument itsNatDoc = compMgr.getItsNatDocument();       
        DocumentFragment docFrag = itsNatDoc.toDOM(markup);
        Element newParentElem = ItsNatTreeWalker.getFirstChildElement(docFrag);        
        parentElem.getParentNode().replaceChild(newParentElem,parentElem);
        return newParentElem;
    }
    */
}
