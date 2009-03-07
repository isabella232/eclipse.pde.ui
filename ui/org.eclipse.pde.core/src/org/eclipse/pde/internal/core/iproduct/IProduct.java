/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Code 9 Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.core.iproduct;

public interface IProduct extends IProductObject {

	String P_ID = "id"; //$NON-NLS-1$
	String P_NAME = "name"; //$NON-NLS-1$
	String P_APPLICATION = "application"; //$NON-NLS-1$
	String P_USEFEATURES = "useFeatures"; //$NON-NLS-1$
	String P_INCLUDE_FRAGMENTS = "includeFragments"; //$NON-NLS-1$
	String P_INTRO_ID = "introId"; //$NON-NLS-1$
	String P_VERSION = "version"; //$NON-NLS-1$

	String getId();

	String getName();

	String getApplication();

	String getVersion();

	String getDefiningPluginId();

	boolean useFeatures();

	IAboutInfo getAboutInfo();

	IConfigurationFileInfo getConfigurationFileInfo();

	IArgumentsInfo getLauncherArguments();

	IJREInfo getJREInfo();

	IWindowImages getWindowImages();

	ISplashInfo getSplashInfo();

	IIntroInfo getIntroInfo();

	ILauncherInfo getLauncherInfo();

	ILicenseInfo getLicenseInfo();

	void addPlugins(IProductPlugin[] plugin);

	void addFeatures(IProductFeature[] feature);

	void addPluginConfigurations(IPluginConfiguration[] configurations);

	void removePlugins(IProductPlugin[] plugins);

	void removeFeatures(IProductFeature[] feature);

	void removePluginConfigurations(IPluginConfiguration[] configurations);

	IPluginConfiguration findPluginConfiguration(String id);

	IProductPlugin[] getPlugins();

	IProductFeature[] getFeatures();

	IPluginConfiguration[] getPluginConfigurations();

	void setId(String id);

	void setVersion(String version);

	void setName(String name);

	void setAboutInfo(IAboutInfo info);

	void setApplication(String application);

	void setConfigurationFileInfo(IConfigurationFileInfo info);

	void setLauncherArguments(IArgumentsInfo info);

	void setJREInfo(IJREInfo info);

	void setWindowImages(IWindowImages images);

	void setSplashInfo(ISplashInfo info);

	void setIntroInfo(IIntroInfo introInfo);

	void setLauncherInfo(ILauncherInfo info);

	void setLicenseInfo(ILicenseInfo info);

	void setUseFeatures(boolean use);

	void reset();

	void swap(IProductFeature feature1, IProductFeature feature2);

	boolean containsPlugin(String id);

	boolean containsFeature(String id);

}
