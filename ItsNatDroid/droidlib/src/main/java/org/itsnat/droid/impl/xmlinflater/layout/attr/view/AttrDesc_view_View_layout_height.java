package org.itsnat.droid.impl.xmlinflater.layout.attr.view;

import android.view.View;
import android.view.ViewGroup;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDesc;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_view_View_layout_height extends AttrDesc
{
    public AttrDesc_view_View_layout_height(ClassDescViewBased parent)
    {
        super(parent,"layout_height");
    }

    public void setAttribute(final View view, String value,final OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        final int height = getDimensionWithNameInt(value,view.getContext());

        Runnable task = new Runnable(){
            @Override
            public void run()
            {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = height;
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

    public void removeAttribute(View view)
    {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        params.height = ViewGroup.LayoutParams.MATCH_PARENT; // Por poner algo, no lo tengo claro y yo creo que puede cambiar según el ViewGroup padre

        view.setLayoutParams(params);
    }
}