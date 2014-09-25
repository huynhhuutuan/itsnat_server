package org.itsnat.droid.impl.xmlinflater.attr.view;

import android.view.View;
import android.view.ViewGroup;

import org.itsnat.droid.impl.xmlinflater.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDesc;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public abstract class AttrDesc_view_View_layout_margin_Base extends AttrDesc
{
    public AttrDesc_view_View_layout_margin_Base(ClassDescViewBased parent,String name)
    {
        super(parent,name);
    }

    public void setAttribute(final View view, String value,final OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        final int valueInt = getDimensionInt(value, view.getContext());

        Runnable task = new Runnable(){
            @Override
            public void run()
            {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
                setAttribute(params, valueInt);
            }};

        if (oneTimeAttrProcess != null)
        {
            oneTimeAttrProcess.addLayoutParamsTask(task);
        }
        else
        {
            task.run();
            view.setLayoutParams(view.getLayoutParams());
        }
    }

    protected abstract void setAttribute(ViewGroup.MarginLayoutParams params,int value);


    public void removeAttribute(View view)
    {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        removeAttribute(params);

        view.setLayoutParams(params);
    }

    protected abstract void removeAttribute(ViewGroup.MarginLayoutParams params);
}