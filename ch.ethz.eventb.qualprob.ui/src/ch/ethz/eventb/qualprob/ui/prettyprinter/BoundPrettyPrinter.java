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
package ch.ethz.eventb.qualprob.ui.prettyprinter;

import static org.eventb.ui.prettyprint.PrettyPrintUtils.getHTMLBeginForCSSClass;
import static org.eventb.ui.prettyprint.PrettyPrintUtils.getHTMLEndForCSSClass;
import static org.eventb.ui.prettyprint.PrettyPrintUtils.wrapString;

import org.eventb.ui.prettyprint.DefaultPrettyPrinter;
import org.eventb.ui.prettyprint.IPrettyPrintStream;
import org.eventb.ui.prettyprint.PrettyPrintAlignments.HorizontalAlignment;
import org.eventb.ui.prettyprint.PrettyPrintAlignments.VerticalAlignement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.qualprob.basis.IBound;

public class BoundPrettyPrinter extends DefaultPrettyPrinter {

	private static final String BOUND_EXPRESSION = "variantExpression";
	private static final String BOUND_EXPRESSION_SEPARATOR_BEGIN = null;
	private static final String BOUND_EXPRESSION_SEPARATOR_END = null;

	@Override
	public void prettyPrint(IInternalElement elt, IInternalElement parent,
			IPrettyPrintStream ps) {
		if (elt instanceof IBound) {
			IBound bound = (IBound) elt;
			try {
				appendBoundExpression(ps,
						wrapString(bound.getExpressionString()));
			} catch (RodinDBException e) {
				System.err
						.println("Cannot get the expression string for the bound element."
								+ e.getMessage());
			}
		}

	}
	
	private static void appendBoundExpression(IPrettyPrintStream ps, String expression) {
	    ps.appendString(expression, //
	      getHTMLBeginForCSSClass(BOUND_EXPRESSION, //
	                              HorizontalAlignment.LEFT, //
	                              VerticalAlignement.MIDDLE),//
	      getHTMLEndForCSSClass(BOUND_EXPRESSION, //
	                              HorizontalAlignment.LEFT, //
	                              VerticalAlignement.MIDDLE),//
	                              BOUND_EXPRESSION_SEPARATOR_BEGIN, //
	                              BOUND_EXPRESSION_SEPARATOR_END);
	}

}
