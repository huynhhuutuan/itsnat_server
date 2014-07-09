package org.itsnat.droid.impl.browser.clientdoc;

import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.itsnat.droid.impl.browser.clientdoc.ItsNatViewImpl;
import org.itsnat.droid.impl.browser.clientdoc.evtlistener.DOMStdEventListener;

import java.util.List;

/**
 * Created by jmarranz on 4/07/14.
 */
public class EventListenerViewAdapter implements View.OnClickListener,View.OnTouchListener,View.OnKeyListener
{
    protected ItsNatViewImpl viewData;
    protected View.OnClickListener clickListener;
    protected View.OnTouchListener touchListener;
    protected View.OnKeyListener keyboardListener;

    public EventListenerViewAdapter(ItsNatViewImpl viewData)
    {
        this.viewData = viewData;
    }

    @Override
    public void onClick(View view)
    {
        if (clickListener != null) clickListener.onClick(viewData.getView());

        dispatch("click",null);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (touchListener != null) touchListener.onTouch(viewData.getView(), motionEvent);

        String type = "";
        int action = motionEvent.getAction();
        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                type = "mousedown";
                break;
            case MotionEvent.ACTION_UP:
                type = "mouseup";
                break;
            case MotionEvent.ACTION_MOVE:
                type = "mousemove";
                break;
            /*
            case MotionEvent.ACTION_CANCEL:
                type = "";
                break;
                */
        }

        dispatch(type,motionEvent);

        return false; // No lo tengo claro si true o false
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent)
    {
        if (keyboardListener != null) keyboardListener.onKey(viewData.getView(), i, keyEvent);

        String type = "";
        int action = keyEvent.getAction();
        switch(action)
        {
            case KeyEvent.ACTION_DOWN:
                type = "keydown";
                break;
            case KeyEvent.ACTION_UP:
                type = "keyup";
                break;
            // keypress ??
        }

        dispatch(type,keyEvent);

        return false; // No lo tengo claro si true o false
    }

    private void dispatch(String type,InputEvent nativeEvt)
    {
        // nativeEvt puede ser null

        List<DOMStdEventListener> list = viewData.getEventListeners(type);
        if (list == null) return;
        View view = viewData.getView();
        for(DOMStdEventListener listener : list)
        {
System.out.println("PROVISIONAL: REMOTE EVENT OK " + type);
            listener.dispatchEvent(view,nativeEvt);
        }
    }

    public void setOnClickListener(View.OnClickListener clickListener)
    {
        this.clickListener = clickListener;
    }

    public void setOnTouchListener(View.OnTouchListener touchListener)
    {
        this.touchListener = touchListener;
    }

    public void setOnKeyListener(View.OnKeyListener keyboardListener)
    {
        this.keyboardListener = keyboardListener;
    }
}