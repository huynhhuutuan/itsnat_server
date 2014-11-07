package org.itsnat.droid.impl.xmlinflater.layout.attr.widget;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescViewReflecFieldSetBoolean;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_widget_SlidingDrawer_orientation extends AttrDescViewReflecFieldSetBoolean
{
    public AttrDescView_widget_SlidingDrawer_orientation(ClassDescViewBased parent)
    {
        super(parent,"orientation","mVertical",true);
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        boolean vertical = true;
        if (value.equals("horizontal"))
            vertical = false;
        else if (value.equals("vertical"))
            vertical = true;

        setField(view,vertical);
    }

    public void removeAttribute(View view)
    {
        setAttribute(view, "vertical", null,null);
    }


}