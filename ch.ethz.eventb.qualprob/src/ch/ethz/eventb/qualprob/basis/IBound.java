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
package ch.ethz.eventb.qualprob.basis;

import org.eventb.core.ICommentedElement;
import org.eventb.core.IExpressionElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinCore;

import ch.ethz.eventb.qualprob.QualProbPlugin;



public interface IBound extends ICommentedElement, IExpressionElement {

	IInternalElementType<IBound> ELEMENT_TYPE = RodinCore
			.getInternalElementType(QualProbPlugin.PLUGIN_ID + ".bound");

	// No additional method

}
