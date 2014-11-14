package org.itsnat.droid.impl.xmlinflater.layout.attr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.itsnat.droid.impl.browser.PageImpl;
import org.itsnat.droid.impl.model.AttrParsed;
import org.itsnat.droid.impl.model.AttrParsedRemote;
import org.itsnat.droid.impl.util.MiscUtil;
import org.itsnat.droid.impl.xmlinflated.InflatedXML;
import org.itsnat.droid.impl.xmlinflated.layout.InflatedLayoutImpl;
import org.itsnat.droid.impl.xmlinflater.AttrDesc;
import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.XMLInflaterLayout;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;
import org.itsnat.droid.impl.xmlinflater.layout.page.XMLInflaterLayoutPage;

/**
 * Created by jmarranz on 30/04/14.
 */
public abstract class AttrDescView extends AttrDesc
{
    private static Class class_R_styleable;

    public AttrDescView(ClassDescViewBased parent, String name)
    {
        super(parent,name);

        /* Para ver si nos hemos equivocado y name no se corresponde con el nombre de la clase específica
        String className = getClass().getName();
        if (!className.contains(name) &&
            !className.contains("AttrDescReflecView") &&
            !className.contains("AttrDescView_view_View_layout_rellayout_byId") &&
            !className.contains("AttrDescView_view_View_layout_rellayout_byBoolean")  )
            System.out.println("ERROR: " + className);
        */
    }

    protected static Class getClass_R_styleable()
    {
        if (class_R_styleable == null)
            class_R_styleable = MiscUtil.resolveClass("com.android.internal.R$styleable");
        return class_R_styleable;
    }

    public Drawable getDrawable(AttrParsed attr,Context ctx,XMLInflaterLayout xmlInflaterLayout)
    {
        PageImpl page = null;
        if (xmlInflaterLayout instanceof XMLInflaterLayoutPage)
            page = ((XMLInflaterLayoutPage)xmlInflaterLayout).getPageImpl();
        return getDrawable(attr,ctx,page);
    }

    protected void processDrawableTask(AttrParsed attr,Runnable task,XMLInflaterLayout xmlInflaterLayout)
    {
        if (attr instanceof AttrParsedRemote && !((AttrParsedRemote) attr).isDownloaded())
        {
            // Es el caso de inserción dinámica post page load via ItsNat de nuevos View con atributos que especifican recursos remotos
            // Hay que cargar primero los recursos y luego ejecutar la task que definirá el drawable
            downloadResources((AttrParsedRemote) attr, task, xmlInflaterLayout);
        }
        else
        {
            task.run();
        }
    }

    private static void downloadResources(AttrParsedRemote attr,Runnable task,XMLInflaterLayout xmlInflaterLayout)
    {
        InflatedLayoutImpl inflated = xmlInflaterLayout.getInflatedLayoutImpl();
        PageImpl page = ClassDescViewBased.getPageImpl(inflated); // NO puede ser nulo
        page.getItsNatDocImpl().downloadResources(attr,task);
    }

    protected void setAttribute(View view, String value, XMLInflaterLayout xmlInflaterLayout, Context ctx, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        AttrParsed attr = AttrParsed.create(InflatedXML.XMLNS_ANDROID,getName(),value);
        setAttribute(view, attr, xmlInflaterLayout,ctx,oneTimeAttrProcess,pending);
    }

    public abstract void setAttribute(View view, AttrParsed attr, XMLInflaterLayout xmlInflaterLayout, Context ctx, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending);

    public abstract void removeAttribute(View view, XMLInflaterLayout xmlInflaterLayout, Context ctx);
}


