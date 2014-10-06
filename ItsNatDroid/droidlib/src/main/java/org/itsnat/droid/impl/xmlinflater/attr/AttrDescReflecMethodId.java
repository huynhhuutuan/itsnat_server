package org.itsnat.droid.impl.xmlinflater.attr;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

/**
 *
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescReflecMethodId extends AttrDescReflecMethod
{
    protected Integer defaultValue;

    public AttrDescReflecMethodId(ClassDescViewBased parent, String name, String methodName,Integer defaultValue)
    {
        super(parent,name,methodName,getClassParam());
        this.defaultValue = defaultValue;
    }

    public AttrDescReflecMethodId(ClassDescViewBased parent, String name,Integer defaultValue)
    {
        super(parent,name,getClassParam());
        this.defaultValue = defaultValue;
    }

    protected static Class<?> getClassParam()
    {
        return int.class;
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        int id = getIdentifierAddIfNecessary(value, view.getContext());

        callMethod(view, id);
    }

    public void removeAttribute(View view)
    {
        if (defaultValue != null)
            callMethod(view, defaultValue);
    }

}
