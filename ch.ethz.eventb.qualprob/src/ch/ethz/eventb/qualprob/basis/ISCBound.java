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

import org.eventb.core.ISCExpressionElement;
import org.eventb.core.ITraceableElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinCore;

import ch.ethz.eventb.qualprob.QualProbPlugin;


public interface ISCBound extends ISCExpressionElement, ITraceableElement{
	
	IInternalElementType<ISCBound> ELEMENT_TYPE =
		RodinCore.getInternalElementType(QualProbPlugin.PLUGIN_ID + ".scBound");

}
