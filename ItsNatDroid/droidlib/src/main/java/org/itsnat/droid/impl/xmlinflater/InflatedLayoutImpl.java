package org.itsnat.droid.impl.xmlinflater;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.itsnat.droid.AttrCustomInflaterListener;
import org.itsnat.droid.InflatedLayout;
import org.itsnat.droid.ItsNatDroid;
import org.itsnat.droid.impl.ItsNatDroidImpl;
import org.itsnat.droid.impl.parser.Attribute;
import org.itsnat.droid.impl.parser.ScriptParsed;
import org.itsnat.droid.impl.parser.TreeViewParsed;
import org.itsnat.droid.impl.parser.ViewParsed;
import org.itsnat.droid.impl.util.MapLight;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jmarranz on 16/06/14.
 */
public abstract class InflatedLayoutImpl implements InflatedLayout
{
    protected ItsNatDroidImpl itsNatDroid;
    protected TreeViewParsed treeViewParsed;
    protected View rootView;
    protected ViewMapByXMLId viewMapByXMLId;
    protected Context ctx;
    protected AttrCustomInflaterListener inflateListener;


    public InflatedLayoutImpl(ItsNatDroidImpl itsNatDroid,TreeViewParsed treeViewParsed,AttrCustomInflaterListener inflateListener,Context ctx)
    {
        // rootView se define a posteriori
        this.itsNatDroid = itsNatDroid;
        this.treeViewParsed = treeViewParsed;
        this.inflateListener = inflateListener;
        this.ctx = ctx;
    }

    public XMLLayoutInflateService getXMLLayoutInflateService()
    {
        return itsNatDroid.getXMLLayoutInflateService();
    }

    public String getAndroidNSPrefix()
    {
        return treeViewParsed.getAndroidNSPrefix();
    }

    public MapLight<String,String> getNamespacesByPrefix()
    {
        return treeViewParsed.getNamespacesByPrefix();
    }

    public String getNamespace(String prefix)
    {
        return getNamespacesByPrefix().get(prefix);
    }

    @Override
    public ItsNatDroid getItsNatDroid()
    {
        return getItsNatDroidImpl();
    }

    public ItsNatDroidImpl getItsNatDroidImpl()
    {
        return itsNatDroid;
    }

    @Override
    public View getRootView()
    {
        return rootView;
    }

    public void setRootView(View rootView)
    {
        this.rootView = rootView;
    }

    public AttrCustomInflaterListener getAttrCustomInflaterListener()
    {
        return inflateListener;
    }

    public Context getContext()
    {
        return ctx;
    }

    private ViewMapByXMLId getViewMapByXMLId()
    {
        if (viewMapByXMLId == null) viewMapByXMLId = new ViewMapByXMLId(this);
        return viewMapByXMLId;
    }

    public String unsetXMLId(View view)
    {
        return getViewMapByXMLId().unsetXMLId(view);
    }

    public String getXMLId(View view)
    {
        return getViewMapByXMLId().getXMLId(view);
    }

    public void setXMLId(String id, View view)
    {
        getViewMapByXMLId().setXMLId(id, view);
    }

    public View findViewByXMLId(String id)
    {
        if (viewMapByXMLId == null) return null;
        return viewMapByXMLId.findViewByXMLId(id);
    }

    public static void fillScriptList(TreeViewParsed treeViewParsed,List<String> scriptList)
    {
        List<ScriptParsed> scriptListFromTree = treeViewParsed.getScriptList();
        if (scriptListFromTree != null)
        {
            for (ScriptParsed script : scriptListFromTree)
                scriptList.add(script.getCode());
        }
    }

    public View inflate(String[] loadScript, List<String> scriptList)
    {
        if (loadScript != null)
            loadScript[0] = treeViewParsed.getLoadScript();

        if (scriptList != null)
            fillScriptList(treeViewParsed,scriptList);

        View rootView = inflateRootView(treeViewParsed);
        return rootView;
    }

    private View inflateRootView(TreeViewParsed treeViewParsed)
    {
        ViewParsed rootViewParsed = treeViewParsed.getRootView();

        String viewName = rootViewParsed.getName(); // viewName lo normal es que sea un nombre corto por ej RelativeLayout

        PendingPostInsertChildrenTasks pending = new PendingPostInsertChildrenTasks();

        View rootView = createRootViewObjectAndFillAttributes(viewName,rootViewParsed,pending);

        processChildViews(rootViewParsed,rootView);

        pending.executeTasks();

        return rootView;
    }

    public View insertFragment(ViewParsed rootViewFragmentParsed)
    {
        return inflateNextView(rootViewFragmentParsed,null);
    }

    private View inflateNextView(ViewParsed viewParsed, View viewParent)
    {
        // Es llamado también para insertar fragmentos
        PendingPostInsertChildrenTasks pending = new PendingPostInsertChildrenTasks();

        View view = createViewObjectAndFillAttributesAndAdd((ViewGroup) viewParent, viewParsed, pending);

        // No funciona, sólo funciona con XML compilados:
        //AttributeSet attributes = Xml.asAttributeSet(parser);
        //LayoutInflater inf = LayoutInflater.from(ctx);

        processChildViews(viewParsed,view);

        pending.executeTasks();

        return view;
    }

    protected void processChildViews(ViewParsed viewParsedParent, View viewParent)
    {
        LinkedList<ViewParsed> childList = viewParsedParent.getChildViewList();
        if (childList != null)
        {
            for (ViewParsed childViewParsed : childList)
            {
                View childView = inflateNextView(childViewParsed, viewParent);
            }
        }
    }

    private View createViewObject(ClassDescViewBased classDesc,ViewParsed viewParsed,PendingPostInsertChildrenTasks pending)
    {
        return classDesc.createViewObjectFromParser(this,viewParsed,pending);
    }

    public View createRootViewObjectAndFillAttributes(String viewName,ViewParsed viewParsed,PendingPostInsertChildrenTasks pending)
    {
        ClassDescViewMgr classDescViewMgr = getXMLLayoutInflateService().getClassDescViewMgr();
        ClassDescViewBased classDesc = classDescViewMgr.get(viewName);
        View rootView = createViewObject(classDesc,viewParsed,pending);

        setRootView(rootView); // Lo antes posible porque los inline event handlers lo necesitan, es el root View del template, no el View.getRootView() pues una vez insertado en la actividad de alguna forma el verdadero root cambia

        fillAttributesAndAddView(rootView,classDesc,null,viewParsed,pending);

        return rootView;
    }

    public View createViewObjectAndFillAttributesAndAdd(ViewGroup viewParent, ViewParsed viewParsed, PendingPostInsertChildrenTasks pending)
    {
        // viewParent es null en el caso de parseo de fragment, por lo que NO tengas la tentación de llamar aquí
        // a setRootView(view); cuando viewParent es null "para reutilizar código"
        ClassDescViewMgr classDescViewMgr = getXMLLayoutInflateService().getClassDescViewMgr();
        ClassDescViewBased classDesc = classDescViewMgr.get(viewParsed.getName());
        View view = createViewObject(classDesc,viewParsed,pending);

        fillAttributesAndAddView(view,classDesc,viewParent,viewParsed,pending);

        return view;
    }

    private void fillAttributesAndAddView(View view,ClassDescViewBased classDesc,ViewGroup viewParent,ViewParsed viewParsed,PendingPostInsertChildrenTasks pending)
    {
        OneTimeAttrProcess oneTimeAttrProcess = classDesc.createOneTimeAttrProcess(view,viewParent);
        fillViewAttributes(classDesc,view,viewParsed,oneTimeAttrProcess,pending); // Los atributos los definimos después porque el addView define el LayoutParameters adecuado según el padre (LinearLayout, RelativeLayout...)
        classDesc.addViewObject(viewParent,view,-1,oneTimeAttrProcess,ctx);
    }

    private void fillViewAttributes(ClassDescViewBased classDesc,View view,ViewParsed viewParsed,OneTimeAttrProcess oneTimeAttrProcess,PendingPostInsertChildrenTasks pending)
    {
        ArrayList<Attribute> attribList = viewParsed.getAttributeList();
        if (attribList != null)
        {
            for (int i = 0; i < attribList.size(); i++)
            {
                Attribute attr = attribList.get(i);
                String namespaceURI = attr.getNamespaceURI();
                String name = attr.getName(); // El nombre devuelto no contiene el namespace
                String value = attr.getValue();
                setAttribute(classDesc, view, namespaceURI, name, value, oneTimeAttrProcess, pending);
            }
        }

        oneTimeAttrProcess.executeLastTasks();
    }

    public boolean setAttribute(ClassDescViewBased classDesc,View view,String namespaceURI,String name,String value,
                                OneTimeAttrProcess oneTimeAttrProcess,PendingPostInsertChildrenTasks pending)
    {
        return classDesc.setAttribute(view,namespaceURI, name, value, oneTimeAttrProcess,pending,this);
    }

}
