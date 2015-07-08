/*
 * TestTable.java
 *
 * Created on 6 de diciembre de 2006, 20:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.comp.free;

import org.itsnat.core.html.ItsNatHTMLDocument;
import org.itsnat.comp.table.ItsNatFreeTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import org.itsnat.comp.ItsNatComponentManager;
import org.w3c.dom.Document;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.html.HTMLElement;
import test.web.comp.TestTableBase;

/**
 *
 * @author jmarranz
 */
public class TestFreeTable2 extends TestTableBase implements EventListener,TableModelListener,ListSelectionListener
{

    /**
     * Creates a new instance of TestTable
     */
    public TestFreeTable2(ItsNatHTMLDocument itsNatDoc)
    {
        super(itsNatDoc);

        initTable();
    }

    public void initTable()
    {
        Document doc = itsNatDoc.getDocument();
        HTMLElement tableElem = (HTMLElement)doc.getElementById("freeTableId2");
        ItsNatComponentManager componentMgr = itsNatDoc.getItsNatComponentManager();
        ItsNatFreeTable comp = (ItsNatFreeTable)componentMgr.findItsNatComponent(tableElem);
        initTable(comp,"addRowFreeTableId2","removeRowFreeTableId2","joystickModeFreeTableId2");
    }


}
