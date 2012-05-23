/* Main.java
   Copyright (C) 2008 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.javaws.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;


public class Main extends JPanel  {

	private final String notes = "/net/sourceforge/javaws/about/resources/notes.html";
	private final String apps = "/net/sourceforge/javaws/about/resources/applications.html";
	private final String about = "/net/sourceforge/javaws/about/resources/about.html";
	JTabbedPane tabbedPane;

	public Main() throws IOException {
		super(new BorderLayout());
		
		HTMLPanel notesPanel = new HTMLPanel(getClass().getResource(notes));
		HTMLPanel appsPanel = new HTMLPanel(getClass().getResource(apps));
		HTMLPanel aboutPanel = new HTMLPanel(getClass().getResource(about));
		
	
		
		tabbedPane = new JTabbedPane();

		tabbedPane.add("About IcedTea-Web and NetX", aboutPanel);
		tabbedPane.add("Applications", appsPanel);
		tabbedPane.add("Notes", notesPanel);
		
		tabbedPane.setPreferredSize(new Dimension(550,410));
		add(tabbedPane, BorderLayout.CENTER);
	}

	private static void createAndShowGUI() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {	
		}
		
		JFrame frame = new JFrame("About IcedTea-Web and NetX");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Main demo = null;
		try {
			demo = new Main();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		demo.setOpaque(true);
		frame.setContentPane(demo);
		frame.pack();
		centerDialog(frame);
		frame.setVisible(true);
	}
	
    private static void centerDialog(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = frame.getSize();

        frame.setLocation((screen.width - dialogSize.width)/2,
            (screen.height - dialogSize.height)/2);
    }

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}


}
