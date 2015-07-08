/*
 * TestListMultipleBase.java
 *
 * Created on 5 de diciembre de 2006, 18:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.comp;

import org.itsnat.comp.list.ItsNatListMultSel;
import org.itsnat.core.html.ItsNatHTMLDocument;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import test.web.shared.TestBaseHTMLDocument;
import test.shared.TestUtil;

/**
 *
 * @author jmarranz
 */
public abstract class TestListMultipleBase extends TestBaseHTMLDocument implements EventListener,ListDataListener,ListSelectionListener
{
    /**
     * Creates a new instance of TestListMultipleBase
     */
    public TestListMultipleBase(ItsNatHTMLDocument itsNatDoc)
    {
        super(itsNatDoc);
    }

    public abstract ItsNatListMultSel getItsNatList();

    public void initListMultiple()
    {
        ItsNatListMultSel comp = getItsNatList();

        DefaultListModel dataModel = new DefaultListModel();
        dataModel.addListDataListener(this); // A�adimos antes pues el componente a�ade el suyo y el DefaultListModel llama antes al �ltimo registrado

        comp.setListModel(dataModel);

        ListSelectionModel selModel = comp.getListSelectionModel();
        selModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                // .SINGLE_SELECTION .MULTIPLE_INTERVAL_SELECTION

        dataModel.addElement("One");
        dataModel.addElement("Two");
        dataModel.addElement("Three");
        dataModel.addElement("Five");
        dataModel.addElement("BAD");
        dataModel.removeElement("BAD");
        dataModel.insertElementAt("Four",3);


        comp.setSelectedIndices(new int[]{0,2,3}); // One,Three y Four seleccionados. Usa el ListSelectionModel interno que har� que est�n seleccionados el Three y el Four que son los dos �ltimos y contiguos

        dataModel.insertElementAt("Zero",0); // Para testear el desplazamiendo de los seleccionados

        TestUtil.checkError(!selModel.isSelectedIndex(0));

        selModel.addListSelectionListener(this);

    }

    public void handleEvent(Event evt)
    {
        ItsNatListMultSel comp = getItsNatList();
        int[] selected = comp.getSelectedIndices();

        outText("OK " + evt.getType() + " (n.sel. " + selected.length + ") "); // Para que se vea
    }

    public void addNewRow()
    {
        ItsNatListMultSel comp = getItsNatList();
        DefaultListModel model = (DefaultListModel)comp.getListModel();
        int size = model.getSize();
        // model.addElement(new Integer(size)); // Para ver si se manifiesta en el navegador y genera un evento ListDataEvent
        model.add(0,new Integer(size));
    }

    public void removeLastRow()
    {
        ItsNatListMultSel comp = getItsNatList();
        DefaultListModel model = (DefaultListModel)comp.getListModel();
        int size = model.getSize();
        if (size > 0)
        {
            // model.removeElementAt(size - 1);
             model.removeElementAt(0);
        }
    }

    public void intervalAdded(ListDataEvent e)
    {
        int index0 = e.getIndex0();
        int index1 = e.getIndex1();

        outText("OK added " + index0 + " " + index1 + " "); // Para que se vea en el navegador

        ItsNatListMultSel comp = getItsNatList();
        if (comp.getListSelectionModel().isSelectedIndex(index0))
            System.out.println("PARAR");
    }

    public void intervalRemoved(ListDataEvent e)
    {
    }

    public void contentsChanged(ListDataEvent e)
    {
    }

    public void valueChanged(ListSelectionEvent e)
    {
        ListSelectionModel selModel = (ListSelectionModel)e.getSource();
        if (selModel.getValueIsAdjusting())
            return;

        int first = e.getFirstIndex();
        int last = e.getLastIndex();

        String msg = "OK ";
        for(int i = first; i <= last; i++)
        {
            boolean selected = selModel.isSelectedIndex(i);
            if (selected)
                msg += "(sel " + i + ") ";
            else
                msg += "(desel " + i + ") ";
        }

        outText(msg);
    }
}
