/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.bundles;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.ui.*;

public class PluginToBundleWizard extends Wizard {
	private PluginToBundleWizardPage page1;
	private IPluginModelBase [] selected;
	private static final String STORE_SECTION = "PluginToBundleWizard";
	private static final String KEY_WTITLE = "PluginToBundleWizard.wtitle";

	public PluginToBundleWizard(IPluginModelBase[] selected) {
		IDialogSettings masterSettings = PDEPlugin.getDefault().getDialogSettings();
		setDialogSettings(getSettingsSection(masterSettings));
		setDefaultPageImageDescriptor(PDEPluginImages.DESC_CONVJPPRJ_WIZ);
		setWindowTitle(PDEPlugin.getResourceString(KEY_WTITLE));
		setNeedsProgressMonitor(true);
		this.selected = selected;
	}
	
	private IDialogSettings getSettingsSection(IDialogSettings master) {
		IDialogSettings setting = master.getSection(STORE_SECTION);
		if (setting == null) {
			setting = master.addNewSection(STORE_SECTION);
		}
		return setting;
	}
	
	public boolean performFinish() {
		Object [] finalSelected = page1.getSelected();
		page1.storeSettings();
		IPluginModelBase [] modelArray = new IPluginModelBase[finalSelected.length];
		System.arraycopy(finalSelected, 0, modelArray, 0, finalSelected.length);
		PluginToBundleAction.run(true, getContainer(), modelArray);
		return true;
	}
	
	public void addPages() {
		page1 = new PluginToBundleWizardPage(selected);
		addPage(page1);
	}
}
