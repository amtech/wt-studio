package com.wt.studio.plugin.wizard.projects.services.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.wt.studio.plugin.wizard.projects.services.util.HTML5Model;


public class ListView implements Component{
	private ComponentType type = ComponentType.ListView;


	private static String[] popmenus = new String[] { "ADD SubTitle",  "ADD TemplateSubTitle", "Delete ListView" };

	public Menu getSubMenu(final Tree tree) {
		Menu menu = null;
		if (menu != null)
			return menu;

		menu = new Menu(tree);
		tree.setMenu(menu);
		MenuItem menu1 = new MenuItem(menu, SWT.PUSH);
		menu1.setText(popmenus[0]);
		menu1.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				
				TreeItem i = new TreeItem(tree.getSelection()[0], SWT.NULL);
				i.setText(ComponentType.SubTitle.toString());
				i.setText(1,"title");
				tree.getSelection()[0].setExpanded(true);
			}
		});
		
		MenuItem menu2 = new MenuItem(menu, SWT.PUSH);
		menu2.setText(popmenus[1]);
		menu2.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				TreeItem iroot = new TreeItem(tree.getSelection()[0], SWT.NULL);
				iroot.setText(ComponentType.TemplateSubTitle.toString());
				iroot.setText(1,"title");
				tree.getSelection()[0].setExpanded(true);
			}
		});
		
		MenuItem delMenu = new MenuItem(menu, SWT.PUSH);
		delMenu.setText(popmenus[2]);
		delMenu.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				TreeItem i = tree.getSelection()[0];
				i.removeAll();
				i.dispose();
			}
		});		

		return menu;
	}

	public String getBeforeHTML5Code(HTML5Model model) {
		return "<ul data-role=\"listview\" data-style=\"inset\" data-type=\"group\">\n";
	}

	public String getAfterHTML5Code(HTML5Model model) {
		return "</ul>\n";

	}

	@Override
	public String getJavaScriptCode(HTML5Model model) {
		return "";
	}

	@Override
	public String getJavaCode(HTML5Model model) {
		return  "";
	}

	@Override
	public String getAfterJavaCode(HTML5Model model) {
		return  "";
	}

}
