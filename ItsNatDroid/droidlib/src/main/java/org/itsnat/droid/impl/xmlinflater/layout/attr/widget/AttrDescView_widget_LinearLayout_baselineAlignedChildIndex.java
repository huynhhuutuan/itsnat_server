package org.itsnat.droid.impl.xmlinflater.layout.attr.widget;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescViewReflecMethodInt;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_widget_LinearLayout_baselineAlignedChildIndex extends AttrDescViewReflecMethodInt
{
    public AttrDescView_widget_LinearLayout_baselineAlignedChildIndex(ClassDescViewBased parent)
    {
        super(parent,"baselineAlignedChildIndex",-1);
    }

    public void setAttribute(final View view,final String value,final OneTimeAttrProcess oneTimeAttrProcess,final PendingPostInsertChildrenTasks pending)
    {
        if (pending != null)
        {
            // Necesitamos añadir los children antes para poder referenciarlo por su índice de posición
            pending.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    AttrDescView_widget_LinearLayout_baselineAlignedChildIndex.super.setAttribute(view,value,oneTimeAttrProcess,pending);
                }
            });
        }
        else super.setAttribute(view,value,oneTimeAttrProcess,pending);
    }

}