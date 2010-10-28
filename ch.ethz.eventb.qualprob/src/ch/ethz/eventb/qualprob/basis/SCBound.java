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

import org.eventb.core.basis.SCExpressionElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;

public class SCBound extends SCExpressionElement implements ISCBound {

	/**
	 * Constructor for the Rodin Database
	 * 
	 */
	public SCBound(String name, IRodinElement parent) {
		super(name, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rodinp.core.basis.InternalElement#getElementType()
	 */
	@Override
	public IInternalElementType<? extends IInternalElement> getElementType() {
		return ELEMENT_TYPE;
	}

}
