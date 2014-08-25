package org.itsnat.droid.impl.xmlinflater;

import android.view.View;

/**
 * Created by jmarranz on 25/08/14.
 */
public class ItsNatViewLayoutImpl
{
    public static final int ITSNAT_VIEW_LAYOUT_KEY = 1111111112; // Notar que termina en 2 no en 1 como  ItsNatViewImpl

    protected InflatedLayoutImpl layout;
    protected View view;
    protected String xmlId;

    private ItsNatViewLayoutImpl(InflatedLayoutImpl layout,View view)
    {
        this.layout = layout;
        this.view = view;
    }

    public static ItsNatViewLayoutImpl getItsNatViewLayout(InflatedLayoutImpl layout,View view)
    {
        if (view == null) return null;

        ItsNatViewLayoutImpl viewData = (ItsNatViewLayoutImpl)view.getTag(ITSNAT_VIEW_LAYOUT_KEY);
        if (viewData == null)
        {
            viewData = new ItsNatViewLayoutImpl(layout,view);
            view.setTag(ITSNAT_VIEW_LAYOUT_KEY,viewData);
        }
        return viewData;
    }

    public String getXMLId()
    {
        return xmlId;
    }

    public void setXMLId(String xmlId)
    {
        this.xmlId = xmlId;
    }

    public void unsetXMLId()
    {
        this.xmlId = null;
    }
}
