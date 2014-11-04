package org.itsnat.droid.impl.xmlinflater.layout.attr.widget;

import android.os.Build;
import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDesc;
import org.itsnat.droid.impl.xmlinflater.layout.attr.MethodContainer;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_widget_CalendarView_weekDayTextAppearance extends AttrDesc
{
    protected MethodContainer<Boolean> method;

    public AttrDesc_widget_CalendarView_weekDayTextAppearance(ClassDescViewBased parent)
    {
        super(parent,"weekDayTextAppearance");

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) // 4.0.3 Level 15
            this.method = new MethodContainer<Boolean>(parent,"setUpHeader",new Class[]{int.class});
        else
            this.method = new MethodContainer<Boolean>(parent,"setWeekDayTextAppearance",new Class[]{int.class});
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        int id = getIdentifier(value,view.getContext());

        method.invoke(view, id);
    }

    public void removeAttribute(View view)
    {
        // Android tiene un estilo por defecto
    }

}