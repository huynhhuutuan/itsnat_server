package org.itsnat.droid.impl.xmlinflater.attr.view;

import android.view.View;
import android.widget.RelativeLayout;

import org.itsnat.droid.impl.xmlinflater.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDesc;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_view_View_layout_alignWithParentIfMissing extends AttrDesc
{
    public AttrDesc_view_View_layout_alignWithParentIfMissing(ClassDescViewBased parent)
    {
        super(parent,"layout_alignWithParentIfMissing");
    }

    public void setAttribute(final View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        final boolean convValue = getBoolean(value, view.getContext());

        Runnable task = new Runnable(){
            @Override
            public void run()
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
                params.alignWithParent = convValue;
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
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();

        params.alignWithParent = false;

        view.setLayoutParams(view.getLayoutParams());
    }
}
