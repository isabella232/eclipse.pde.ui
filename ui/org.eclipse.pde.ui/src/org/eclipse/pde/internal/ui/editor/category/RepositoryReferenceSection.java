/*******************************************************************************
 * Copyright (c) 2014, 2017 Rapicorp Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rapicorp Corporation - initial API and implementation
 *     Martin Karpisek <martin.karpisek@gmail.com> - Bug 351356
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.category;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.ui.dialogs.TextURLDropAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.isite.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.pde.internal.ui.parts.TablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class RepositoryReferenceSection extends TableSection {

	private class RepositoryReferenceDialog extends StatusDialog {
		private Text fLocation;
		private IRepositoryReference fEdit;

		public RepositoryReferenceDialog(Shell shell, IRepositoryReference repo) {
			super(shell);
			fEdit = repo;
			setTitle(PDEUIMessages.RepositorySection_title);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			((GridLayout) comp.getLayout()).numColumns = 2;
			SWTFactory.createLabel(comp, PDEUIMessages.UpdatesSection_Location, 1);
			fLocation = SWTFactory.createSingleText(comp, 1);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
			fLocation.setLayoutData(data);
			DropTarget target = new DropTarget(fLocation, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
			target.setTransfer(new Transfer[] {URLTransfer.getInstance(), FileTransfer.getInstance()});
			target.addDropListener(new TextURLDropAdapter(fLocation, true));
			fLocation.addModifyListener(e -> validate());

			if (fEdit != null) {
				if (fEdit.getURL() != null) {
					fLocation.setText(fEdit.getURL());
				}
			} else {
				String initialText = "http://"; //$NON-NLS-1$
				fLocation.setText(initialText);
				fLocation.setSelection(initialText.length());
			}

			validate();

			return comp;
		}

		protected void validate() {
			String location = fLocation.getText().trim();
			if (location.length() == 0) {
				updateStatus(new Status(IStatus.ERROR, IPDEUIConstants.PLUGIN_ID, PDEUIMessages.UpdatesSection_ErrorLocationNoName));
				return;
			}
			try {
				new URL(location);
			} catch (MalformedURLException e) {
				updateStatus(new Status(IStatus.ERROR, IPDEUIConstants.PLUGIN_ID, PDEUIMessages.UpdatesSection_ErrorInvalidURL));
				return;
			}
			updateStatus(Status.OK_STATUS);
		}

		@Override
		protected void okPressed() {
			try {
				if (fEdit != null) {
					// Remove the repository and add a new one
					getSite().removeRepositoryReferences(new IRepositoryReference[] {fEdit});
				}

				ISiteModelFactory factory = getModel().getFactory();
				fEdit = factory.createRepositoryReference();
				String location = fLocation.getText().trim();
				if (!location.startsWith("http://") && !location.startsWith("file:")) //$NON-NLS-1$ //$NON-NLS-2$
					location = "http://" + location; //$NON-NLS-1$
				fEdit.setURL(location);
				fEdit.setEnabled(true);
				getSite().addRepositoryReferences(new IRepositoryReference[] {fEdit});
			} catch (CoreException e) {
				PDEPlugin.log(e);
			} finally {
				super.okPressed();
			}
		}

		@Override
		protected Control createHelpControl(Composite parent) {
			return parent;
		}

		/**
		 * @return a repository info containing the values set in the dialog or <code>null</code>
		 */
		public IRepositoryReference getResult() {
			return fEdit;
		}

	}


	private class ContentProvider implements IStructuredContentProvider {

		ContentProvider() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof ISite) {
				return ((ISite) inputElement).getRepositoryReferences();
			}
			return new Object[0];
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class LabelProvider extends PDELabelProvider {
		@Override
		public Image getColumnImage(Object obj, int index) {
			if (index == 0)
				return get(PDEPluginImages.DESC_REPOSITORY_OBJ);
			return null;
		}

		@Override
		public String getColumnText(Object obj, int index) {
			IRepositoryReference repo = (IRepositoryReference) obj;
			switch (index) {
				case 0 :
					return repo.getURL();
				case 1 :
					return Boolean.toString(repo.getEnabled());
			}
			return null;
		}

	}

	private TableViewer fRepositoryTable;
	private TableEditor fEnabledColumnEditor;
	private ISiteModel fModel;

	public RepositoryReferenceSection(PDEFormPage formPage, Composite parent) {
		super(formPage, parent, Section.DESCRIPTION, getButtonLabels());
	}

	private static String[] getButtonLabels() {
		String[] labels = new String[4];
		labels[0] = PDEUIMessages.RepositorySection_add;
		labels[1] = PDEUIMessages.RepositorySection_edit;
		labels[2] = PDEUIMessages.RepositorySection_remove;
		labels[3] = PDEUIMessages.RepositorySection_removeAll;
		return labels;
	}

	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		fModel = (ISiteModel) getPage().getModel();
		fModel.addModelChangedListener(this);

		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		GridData sectionData = new GridData(GridData.FILL_BOTH);
		sectionData.verticalSpan = 2;
		section.setLayoutData(sectionData);

		Composite container = createClientContainer(section, 2, toolkit);
		createViewerPartControl(container, SWT.MULTI | SWT.FULL_SELECTION, 2, toolkit);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		TablePart tablePart = getTablePart();
		fRepositoryTable = tablePart.getTableViewer();

		final Table table = fRepositoryTable.getTable();

		final TableColumn locationColumn = new TableColumn(table, SWT.LEFT);
		locationColumn.setText(PDEUIMessages.UpdatesSection_LocationColumn);
		locationColumn.setWidth(240);

		final TableColumn enabledColumn = new TableColumn(table, SWT.LEFT);
		enabledColumn.setText(PDEUIMessages.UpdatesSection_EnabledColumn);
		enabledColumn.setWidth(120);

		GridData data = (GridData) tablePart.getControl().getLayoutData();
		data.minimumWidth = 200;

		tablePart.setButtonEnabled(0, isEditable());
		tablePart.setButtonEnabled(1, isEditable());

		table.setHeaderVisible(true);
		toolkit.paintBordersFor(container);

		table.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				int size = table.getSize().x;
				locationColumn.setWidth(size / 6 * 5);
				enabledColumn.setWidth(size / 6 * 1);
			}

		});



		fRepositoryTable.setLabelProvider(new LabelProvider());
		fRepositoryTable.setContentProvider(new ContentProvider());
		fRepositoryTable.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				IRepositoryReference r1 = (IRepositoryReference) e1;
				IRepositoryReference r2 = (IRepositoryReference) e2;
				return super.compare(viewer, r1.getURL(), r2.getURL());
			}
		});
		fRepositoryTable.setInput(getSite());
		createEditors();

		section.setClient(container);

		section.setText(PDEUIMessages.RepositorySection_title);
		section.setDescription(PDEUIMessages.RepositorySection_description);
	}


	@Override
	protected void buttonSelected(int index) {
		switch (index) {
			case 0 :
				handleAdd();
				break;
			case 1 :
				handleEdit((IStructuredSelection) fRepositoryTable.getSelection());
				break;
			case 2 :
				handleDelete();
				break;
			case 3 :
				handleRemoveAll();
				break;
		}
	}


	@Override
	protected void handleDoubleClick(IStructuredSelection selection) {
		handleEdit(selection);
	}

	@Override
	public boolean doGlobalAction(String actionId) {
		if (actionId.equals(ActionFactory.DELETE.getId())) {
			handleDelete();
			return true;
		}
		if (actionId.equals(ActionFactory.CUT.getId())) {
			handleDelete();
			return false;
		}
		return super.doGlobalAction(actionId);
	}


	private void handleEdit(IStructuredSelection selection) {
		clearEditors();
		if (selection.size() > 0) {
			Object[] objects = selection.toArray();
			RepositoryReferenceDialog dialog = new RepositoryReferenceDialog(PDEPlugin.getActiveWorkbenchShell(), (IRepositoryReference) objects[0]);
			if (dialog.open() == Window.OK) {
				IRepositoryReference result = dialog.getResult();
				if (result != null) {
					fRepositoryTable.refresh();
					fRepositoryTable.setSelection(new StructuredSelection(result));
					updateButtons();
				}
			}
		}
	}

	private void handleDelete() {
		clearEditors();
		IStructuredSelection ssel = (IStructuredSelection) fRepositoryTable.getSelection();
		if (ssel.size() > 0) {
			Object[] objects = ssel.toArray();
			IRepositoryReference[] repos = new IRepositoryReference[objects.length];
			System.arraycopy(objects, 0, repos, 0, objects.length);
			try {
				getSite().removeRepositoryReferences(repos);
			} catch (CoreException e) {
				PDEPlugin.log(e);
			}
			fRepositoryTable.refresh(false);
			updateButtons();
		}
	}

	private void handleRemoveAll() {
		clearEditors();
		try {
			getSite().removeRepositoryReferences(getSite().getRepositoryReferences());
		} catch (CoreException e) {
			PDEPlugin.log(e);
		}
		fRepositoryTable.refresh(false);
		updateButtons();
	}

	private void handleAdd() {
		clearEditors();
		RepositoryReferenceDialog dialog = new RepositoryReferenceDialog(PDEPlugin.getActiveWorkbenchShell(), null);
		if (dialog.open() == Window.OK) {
			IRepositoryReference result = dialog.getResult();
			if (result != null) {
				fRepositoryTable.refresh();
				fRepositoryTable.setSelection(new StructuredSelection(result));
				updateButtons();
			}
		}
	}

	@Override
	public void refresh() {
		fRepositoryTable.refresh();
		updateButtons();
		super.refresh();
	}


	@Override
	protected void selectionChanged(IStructuredSelection selection) {
		getPage().getPDEEditor().setSelection(selection);
		updateButtons();
	}

	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof ISite) {
			fRepositoryTable.setSelection(new StructuredSelection(input), true);
			return true;
		}
		return super.setFormInput(input);
	}

	@Override
	public void modelChanged(IModelChangedEvent e) {
		// No need to call super, handling world changed event here
		fRepositoryTable.setInput(getSite());
		fRepositoryTable.refresh();
		updateButtons();
		clearEditors();
	}

	private void updateButtons() {
		TablePart tablePart = getTablePart();
		ISelection selection = getViewerSelection();
		boolean enabled = isEditable() && !selection.isEmpty() && selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof IRepositoryReference;
		tablePart.setButtonEnabled(1, enabled);
		tablePart.setButtonEnabled(2, enabled);
		tablePart.setButtonEnabled(3, isEditable() && getSite().getRepositoryReferences().length > 0);
	}

	private void clearEditors() {
		Control oldEditor = fEnabledColumnEditor.getEditor();
		if (oldEditor != null && !oldEditor.isDisposed())
			oldEditor.dispose();

	}

	private void createEditors() {
		final Table table = fRepositoryTable.getTable();

		fEnabledColumnEditor = new TableEditor(table);
		fEnabledColumnEditor.horizontalAlignment = SWT.CENTER;
		fEnabledColumnEditor.grabHorizontal = true;
		fEnabledColumnEditor.minimumWidth = 50;

		table.addSelectionListener(widgetSelectedAdapter(e -> showControls()));
	}

	private void showControls() {
		// Clean up any previous editor control
		clearEditors();

		// Identify the selected row
		Table table = fRepositoryTable.getTable();
		IStructuredSelection selection = (IStructuredSelection) fRepositoryTable.getSelection();
		if (selection.isEmpty())
			return;
		final TableItem item = table.getSelection()[0];
		if (item != null && !isEditable())
			return;

		if (item != null) {
			final IRepositoryReference repo = (IRepositoryReference) selection.getFirstElement();
			final CCombo combo = new CCombo(table, SWT.BORDER | SWT.READ_ONLY);
			combo.setItems(new String[] {Boolean.toString(true), Boolean.toString(false)});
			combo.setText(item.getText(1));
			combo.pack();
			combo.addSelectionListener(widgetSelectedAdapter(e -> {
				item.setText(1, combo.getText());
				try {
					repo.setEnabled(Boolean.valueOf(combo.getText()).booleanValue());
				} catch (CoreException ex) {
					PDEPlugin.log(ex);
				}
			}));
			fEnabledColumnEditor.setEditor(combo, item, 1);
		}
	}

	public ISiteModel getModel() {
		return fModel;
	}

	public ISite getSite() {
		return fModel.getSite();
	}
}
