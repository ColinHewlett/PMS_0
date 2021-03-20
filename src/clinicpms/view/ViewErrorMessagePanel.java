/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author colin
 */
public class ViewErrorMessagePanel extends JPanel
{
  BorderLayout borderLayout1 = new BorderLayout();
  JScrollPane messageScrollPane = new JScrollPane();
  JTextArea messageTextArea = new JTextArea();
  String message;

  public ViewErrorMessagePanel(String message)
  {
    this.message = message;
    try
    {
      jbInit();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private ViewErrorMessagePanel()
  {
  }

  void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    messageTextArea.setEnabled(true);
    messageTextArea.setEditable(false);
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);
    messageTextArea.setText(message);
    messageTextArea.setSize(300,150);
    //messageTextArea.setSize(messageTextArea.getPreferredSize());
    messageTextArea.setPreferredSize(messageTextArea.getPreferredSize());
    this.add(messageScrollPane, BorderLayout.CENTER);
    messageScrollPane.getViewport().add(messageTextArea, null);
  }
  public void setText(String text)
  {
    this.message = text;
  }
}
