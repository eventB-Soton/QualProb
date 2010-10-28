/*******************************************************************************
 * Copyright (c) 2010 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ch.ethz.eventb.qualprob.ui.navigator.labelProviders;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eventb.internal.ui.eventbeditor.imageprovider.IImageProvider;
import org.eventb.ui.EventBUIPlugin;
import org.eventb.ui.IEventBSharedImages;
import org.rodinp.core.IRodinElement;

import fr.systerel.rodinextension.sample.basis.IBound;

public class BoundLabelProvider implements ILabelProvider, IImageProvider {

	private static final String BOUND_LABEL = "Bound";

	@Override
	public Image getImage(Object element) {
		if (element instanceof IBound) {
			return getImage();
		}
		return null;
	}

	public static Image getImage() {
		final ImageRegistry reg = EventBUIPlugin.getDefault()
				.getImageRegistry();
		return reg.get(IEventBSharedImages.IMG_CONSTANT);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IBound) {
			return BOUND_LABEL;
		}
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(IRodinElement element) {
		final Image image = getImage(element);
		if (image == null) {
			return null;
		}
		final ImageDescriptor desc = ImageDescriptor.createFromImage(image);
		return desc;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// ignore
	}

	@Override
	public void dispose() {
		// ignore
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// ignore
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// ignore
	}

}
