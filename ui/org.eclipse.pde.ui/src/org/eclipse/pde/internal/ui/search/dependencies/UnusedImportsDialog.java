/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.search.dependencies;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.text.bundle.*;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.elements.DefaultTableProvider;
import org.eclipse.pde.internal.ui.parts.WizardCheckboxTablePart;
import org.eclipse.pde.internal.ui.wizards.ListUtil.PluginSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class UnusedImportsDialog extends Dialog {
	private IPluginModelBase model;
	private Object[] unused;
	private WizardCheckboxTablePart checkboxTablePart;
	private CheckboxTableViewer choiceViewer;
	
	static class Sorter extends PluginSorter {

		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1.getClass() == e2.getClass())
				return super.compare(viewer, e1, e2);
			else if (e1 instanceof ImportPackageObject)
				return 1;
			else
				return -1;
		}
	}
	
	class ContentProvider extends DefaultTableProvider {
		public Object[] getElements(Object parent) {
			return unused;
		}
	}

	public UnusedImportsDialog(
		Shell parentShell,
		IPluginModelBase model,
		Object[] unused) {
		super(parentShell);
		this.model = model;
		this.unused = unused;
		checkboxTablePart =
			new WizardCheckboxTablePart(
				PDEUIMessages.UnusedDependencies_remove); 
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 9;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);

		checkboxTablePart.createControl(container);
		choiceViewer = checkboxTablePart.getTableViewer();
		choiceViewer.setContentProvider(new ContentProvider());
		choiceViewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
		choiceViewer.setSorter(new Sorter());

		gd = (GridData) checkboxTablePart.getControl().getLayoutData();
		gd.widthHint = 250;
		gd.heightHint = 275;

		choiceViewer.setInput(PDEPlugin.getDefault());
		checkboxTablePart.setSelection(unused);
		return container;
	}

	protected void okPressed() {
		try {
			ImportPackageHeader pkgHeader = null;
			Object[] elements = choiceViewer.getCheckedElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IPluginImport)
					model.getPluginBase().remove((IPluginImport) elements[i]);
				else {
					if (pkgHeader == null) 
						pkgHeader = (ImportPackageHeader)((ImportPackageObject)elements[i]).getHeader();
					pkgHeader.removePackage((ImportPackageObject)elements[i]);			
				}
			}
			super.okPressed();
		} catch (CoreException e) {
		}
	}

}
