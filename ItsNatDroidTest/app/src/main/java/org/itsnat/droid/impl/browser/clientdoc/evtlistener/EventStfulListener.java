package org.itsnat.droid.impl.browser.clientdoc.evtlistener;

import org.itsnat.droid.impl.browser.clientdoc.ItsNatDocImpl;
import org.itsnat.droid.impl.browser.clientdoc.event.EventGeneric;

/**
 * Created by jmarranz on 6/07/14.
 */
public abstract class EventStfulListener extends EventGenericListener
{
    protected String eventType;

    public EventStfulListener(ItsNatDocImpl parent,String eventType, int commMode, long timeout)
    {
        super(parent,"event",commMode, timeout);

        this.eventType = eventType;
    }

    public String getEventType()
    {
        return eventType;
    }

    public String genParamURL(EventGeneric evt)
    {
        String url = super.genParamURL(evt);
        url += "&itsnat_eventType=" + getEventType();
        return url;
    }
}