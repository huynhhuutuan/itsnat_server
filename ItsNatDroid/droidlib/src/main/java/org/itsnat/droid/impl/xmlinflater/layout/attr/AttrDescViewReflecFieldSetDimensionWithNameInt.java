package org.itsnat.droid.impl.xmlinflater.layout.attr;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;


/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescViewReflecFieldSetDimensionWithNameInt extends AttrDescViewReflecFieldSet
{
    protected String defaultValue;

    public AttrDescViewReflecFieldSetDimensionWithNameInt(ClassDescViewBased parent, String name, String fieldName, String defaultValue)
    {
        super(parent,name,fieldName);
        this.defaultValue = defaultValue;
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        int convertedValue = getDimensionWithNameInt(value,view.getContext());

        setField(view,convertedValue);
    }

    public void removeAttribute(View view)
    {
        if (defaultValue != null)
            setAttribute(view,defaultValue,null,null);
    }


}