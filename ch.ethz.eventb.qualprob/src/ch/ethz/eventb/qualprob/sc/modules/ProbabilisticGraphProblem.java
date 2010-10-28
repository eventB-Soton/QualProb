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
package ch.ethz.eventb.qualprob.sc.modules;

import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.rodinp.core.IRodinProblem;

import ch.ethz.eventb.qualprob.QualProbPlugin;


public enum ProbabilisticGraphProblem implements IRodinProblem {
	
	// Errors
	TooManyBoundsError(IMarker.SEVERITY_ERROR, "Too many bounds. A machine may at most have one bound."),
	NoBoundError(IMarker.SEVERITY_ERROR, "No bound is defined. A machine must have one bound."),
	ProbEventNoBoundError(IMarker.SEVERITY_ERROR, "No upper bound specified for variant"),
	BoundMustBeSpecified(IMarker.SEVERITY_ERROR, "A bound must be specified"),
	BoundMustBeConstantError(IMarker.SEVERITY_ERROR, "Bound must be constant or set"),
	VariantMustBeConstantError(IMarker.SEVERITY_ERROR, "Variant must be constant or set"),
	InvalidBoundTypeError(IMarker.SEVERITY_ERROR, "Bound cannot have type {0}"),
	VariantBoundTypeError(IMarker.SEVERITY_ERROR, "Variant type and Bound type must be same"),
	InvalidBoundExpressionError(IMarker.SEVERITY_ERROR, "Invalid bound expression for {0}"),
	BoundFreeIdentifierError(IMarker.SEVERITY_ERROR, "Identifier {0} must not occur free in the bound"),
	EventMustProb(IMarker.SEVERITY_ERROR, "This event must be probabilistic convergent."),
	ProbConvergenceUndefError(IMarker.SEVERITY_ERROR, "This event must be probabilistic convergent."),
	
	//Warnings
	ProbFaultyConvergenceWarning(IMarker.SEVERITY_WARNING, "Probabilistic event must be refined by a probabilistic event. Event {0} set to not probabilistic"),
	ProbAbsNotConvWarning(IMarker.SEVERITY_WARNING, "This event is convergent but not probabilistic"),
	ProbEventOrdinaryWarning(IMarker.SEVERITY_WARNING, "An ordinary event cannot be probabilistic. Not probabilistic assumed"),
	ProbEventAnticipatedWarning(IMarker.SEVERITY_WARNING, "An anticipated event cannot be probabilistic. Not probabilistic assumed"),
	InitialisationProbWarning(IMarker.SEVERITY_WARNING, "Initialisation cannot be probabilistic. Not probabilistic assumed"),
	ProbConvergenceUndefWarning(IMarker.SEVERITY_WARNING, "Probabilistic convergence value missing"),
	NoProbabilisticEventButBoundWarning(IMarker.SEVERITY_WARNING, "Needless bound. Machine does not contain probabilistic events"),
	NoConvergentEventButBoundWarning(IMarker.SEVERITY_WARNING, "Machine does not contain convergent events");
	
	private final String errorCode;
	
	private final String message;
	
	private final int severity;

	private ProbabilisticGraphProblem(int severity, String message) {
		this.severity = severity;
		this.message = message;
		this.errorCode = QualProbPlugin.PLUGIN_ID + "." + name(); //$NON-NLS-1$
	}

	@Override
	public int getSeverity() {
		return severity;
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String getLocalizedMessage(Object[] args) {
		return MessageFormat.format(message, args);
	}

}
