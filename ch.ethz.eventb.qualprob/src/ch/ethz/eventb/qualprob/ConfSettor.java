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
package ch.ethz.eventb.qualprob;

import org.eventb.core.IMachineRoot;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IElementType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;


/**
 * Class that updates the configuration of files, to add the static checker
 * modules for our qualitative probabilistic reasoning plugin.
 */
public class ConfSettor implements IElementChangedListener {

	private static final String QUAL_PROB_CONFIG = QualProbPlugin.PLUGIN_ID
			+ ".qpConfig";

	public void elementChanged(ElementChangedEvent event) {

		final IRodinElementDelta d = event.getDelta();
		try {
			processDelta(d);
		} catch (final RodinDBException e) {
			// TODO add exception log
		}
	}

	private void processDelta(final IRodinElementDelta d)
			throws RodinDBException {
		final IRodinElement e = d.getElement();

		final IElementType<? extends IRodinElement> elementType = e
				.getElementType();
		if (elementType.equals(IRodinDB.ELEMENT_TYPE)
				|| elementType.equals(IRodinProject.ELEMENT_TYPE)) {
			for (final IRodinElementDelta de : d.getAffectedChildren()) {
				processDelta(de);
			}
		} else if (elementType.equals(IRodinFile.ELEMENT_TYPE)) {
			final IInternalElement root = ((IRodinFile) e).getRoot();

			if (root.getElementType().equals(IMachineRoot.ELEMENT_TYPE)) {
				final IMachineRoot mch = (IMachineRoot) root;
				final String conf = mch.getConfiguration();
				if (!conf.contains(QUAL_PROB_CONFIG)) {
					mch.setConfiguration(conf + ";" + QUAL_PROB_CONFIG, null);
				}
			}
		}
	}
}
