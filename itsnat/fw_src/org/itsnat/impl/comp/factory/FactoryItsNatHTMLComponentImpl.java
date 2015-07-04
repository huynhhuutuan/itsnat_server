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

package org.itsnat.impl.comp.factory;

import org.itsnat.comp.ItsNatComponent;
import org.itsnat.comp.ItsNatHTMLElementComponent;
import org.itsnat.core.NameValue;
import org.itsnat.impl.comp.mgr.ItsNatDocComponentManagerImpl;
import org.itsnat.impl.comp.mgr.web.ItsNatStfulWebDocComponentManagerImpl;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLElement;

/**
 *
 * @author jmarranz
 */
public abstract class FactoryItsNatHTMLComponentImpl extends FactoryItsNatComponentImpl
{
    public FactoryItsNatHTMLComponentImpl()
    {
    }

    public static String getKey(HTMLElement element,String compType)
    {
        return getKey(element.getLocalName(),compType);
    }

    public static String getKey(String localName,String compType)
    {
        if (compType != null) // En elementos HTML reconocidos es opcional, puede ser null
            return "HTML:" + localName + ":" + compType;
        else
            return "HTML:" + localName;
    }

    public String getKey()
    {
        return getKey(getLocalName(),getCompType());
    }

    public boolean mustBeCreatedAutoBuildMode(Element element)
    {
        return declaredAsHTMLWithComponentAttribute(element);
    }

    public boolean declaredAsHTMLWithComponentAttribute(Element element)
    {
        if (isFormControl()) // Un elemento tipo form control puede ser un componente por s� mismo sin necesidad de indicarlo en el markup salvo que en el markup se diga lo contrario
            return !ItsNatDocComponentManagerImpl.explicitIsNotComponentAttribute(element);
        else
            return ItsNatDocComponentManagerImpl.isComponentAttribute(element); // Hay que marcar porque no se a�ade por defecto
    }
        
    
    public ItsNatComponent createItsNatComponent(Element element,String compType,NameValue[] artifacts,boolean autoBuildMode,boolean execCreateFilters,ItsNatDocComponentManagerImpl compMgr)
    {
        // Si no es modo auto-build nos da igual lo que el diga el markup, es el caso de orden expl�cita de creaci�n de componente (si se puede)
        // Ver el manual para entender esto que s�lo afecta a HTML aunque parezca gen�rico
        if (autoBuildMode && !mustBeCreatedAutoBuildMode(element))
            return null;

        ItsNatStfulWebDocComponentManagerImpl stfulCompMgr = (ItsNatStfulWebDocComponentManagerImpl)compMgr;
        return createItsNatHTMLComponent((HTMLElement)element,compType,artifacts,execCreateFilters,stfulCompMgr);
    }        


    protected abstract ItsNatHTMLElementComponent createItsNatHTMLComponent(HTMLElement element, String compType, NameValue[] artifacts, boolean execCreateFilters, ItsNatStfulWebDocComponentManagerImpl compMgr);


    public abstract String getLocalName();
    public abstract boolean isFormControl();
}
