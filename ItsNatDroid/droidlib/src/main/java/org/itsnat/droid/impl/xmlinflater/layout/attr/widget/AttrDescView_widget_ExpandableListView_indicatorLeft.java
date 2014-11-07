package org.itsnat.droid.impl.xmlinflater.layout.attr.widget;

import android.view.View;
import android.widget.ExpandableListView;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescViewReflecFieldGet;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_widget_ExpandableListView_indicatorLeft extends AttrDescViewReflecFieldGet
{
    public AttrDescView_widget_ExpandableListView_indicatorLeft(ClassDescViewBased parent)
    {
        super(parent,"indicatorLeft","mIndicatorRight");
    }

    private void callMethod(View view,int value)
    {
        ((ExpandableListView) view).setIndicatorBounds(value, (Integer)getField(view));
    }

    @Override
    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        int convValue = getDimensionInt(value, view.getContext());

        callMethod(view,convValue);
    }

    @Override
    public void removeAttribute(View view)
    {
        callMethod(view,-1);
    }

}