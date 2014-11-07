package org.itsnat.droid.impl.xmlinflater.layout.attr.view;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescView;
import org.itsnat.droid.impl.xmlinflater.layout.attr.MethodContainer;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_view_View_scrollbars extends AttrDescView
{
    public static final Map<String,Integer> valueMap = new HashMap<String,Integer>( 14 );

    static
    {
        valueMap.put("none",0); // SCROLLBARS_NONE
        valueMap.put("horizontal", 0x00000100); // SCROLLBARS_HORIZONTAL
        valueMap.put("vertical", 0x00000200);  // SCROLLBARS_VERTICAL
    }

    protected static final int SCROLLBARS_MASK = 0x00000300;

    protected MethodContainer methodSetFlags;

    public AttrDescView_view_View_scrollbars(ClassDescViewBased parent)
    {
        super(parent,"scrollbars");
        this.methodSetFlags = new MethodContainer(parent,"setFlags",new Class[]{int.class, int.class});
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        int scrollbars = parseMultipleName(value, valueMap);

        setFlags(view, scrollbars, SCROLLBARS_MASK);
    }

    public void removeAttribute(View view)
    {
        setAttribute(view,"none",null,null );
    }


    protected void setFlags(View view,int scrollbars,int scrollbarsMask)
    {
        methodSetFlags.invoke(view, scrollbars, scrollbarsMask);
    }
}