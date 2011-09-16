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

import static ch.ethz.eventb.qualprob.sc.modules.MachineBoundModule.PROB_ATTRIBUTE;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.IConvergenceElement;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCFilterModule;
import org.eventb.core.sc.state.IAbstractEventInfo;
import org.eventb.core.sc.state.IConcreteEventInfo;
import org.eventb.core.sc.state.IConcreteEventTable;
import org.eventb.core.sc.state.ILabelSymbolInfo;
import org.eventb.core.sc.state.ILabelSymbolTable;
import org.eventb.core.sc.state.IMachineLabelSymbolTable;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.sc.state.IVariantInfo;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.qualprob.basis.IBound;


public class MachineEventProbConvergenceModule extends SCFilterModule {

	public static final IModuleType<MachineEventProbConvergenceModule> MODULE_TYPE = SCCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".machineEventProbConvergenceModule"); //$NON-NLS-1$

	private IVariantInfo variantInfo;
	private ILabelSymbolTable labelSymbolTable;
	private IConcreteEventTable concreteEventTable;
	private Convergence concreteCvg;
	private boolean abstractCvg;
	private boolean concreteProb;
	private boolean abstractProb;
	private IMachineRoot machineRoot;

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	public boolean accept(IRodinElement element, ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {

		final IEvent event = (IEvent) element;
//		if (event.hasAttribute(PROB_ATTRIBUTE)) {
			variantInfo = (IVariantInfo) repository
					.getState(IVariantInfo.STATE_TYPE);
			labelSymbolTable = (ILabelSymbolTable) repository
					.getState(IMachineLabelSymbolTable.STATE_TYPE);
			concreteEventTable = (IConcreteEventTable) repository
					.getState(IConcreteEventTable.STATE_TYPE);
			machineRoot = (IMachineRoot) event.getRoot();
			final String eventLabel = event.getLabel();
			final ILabelSymbolInfo eventSymbolInfo = labelSymbolTable
					.getSymbolInfo(eventLabel);
			final IConcreteEventInfo eventInfo = concreteEventTable
					.getConcreteEventInfo(eventLabel);
			checkConvergence(event, eventInfo, eventSymbolInfo);
			return true;
//		} else {
//			createProblemMarker(event, PROB_ATTRIBUTE,
//					ProbabilisticGraphProblem.ProbConvergenceUndefError);
//			return true;
//		}
	}

	private void checkConvergence(IEvent event,
			IConcreteEventInfo concreteEventInfo,
			ILabelSymbolInfo eventSymbolInfo) throws CoreException {

		abstractCvg = false;
		concreteCvg = concreteEventInfo.getEvent().getConvergence();
		concreteProb = false;
		abstractProb = false;
		if (concreteEventInfo.getEvent().hasAttribute(PROB_ATTRIBUTE)) {
			concreteProb = concreteEventInfo.getEvent().getAttributeValue(
					PROB_ATTRIBUTE);
		}
		boolean origConcreteProb = concreteProb;
		if (concreteEventInfo.isInitialisation()) {
			if (concreteProb) {
				concreteProb = false;
				createProblemMarker(concreteEventInfo.getEvent(),
						PROB_ATTRIBUTE,
						ProbabilisticGraphProblem.InitialisationProbWarning);
			}
		} else {

			final List<IAbstractEventInfo> abstractEventInfos = concreteEventInfo
					.getAbstractEventInfos();
			if (abstractEventInfos.size() != 0) { // not a new event
				checkAbstractConvergence(concreteEventInfo, abstractEventInfos);

			}
			checkOrdiProbConvergence(concreteEventInfo);
			checkAntiProbConvergence(concreteEventInfo);
			checkBoundConvergence(concreteEventInfo);
			checkAbstractProb1(concreteEventInfo);
			checkAbstractProb2(concreteEventInfo);
			checkAbsProbConvergence(concreteEventInfo);
		}
		if (concreteProb != origConcreteProb) {
			concreteEventInfo.setNotAccurate();
		}
		eventSymbolInfo.setAttributeValue(PROB_ATTRIBUTE, concreteProb);

	}

	private void checkAbstractConvergence(IConcreteEventInfo concreteEventInfo,
			List<IAbstractEventInfo> abstractEventInfos) throws CoreException {
		getAbstractConvergence(concreteEventInfo.getRefinesClauses(),
				abstractEventInfos);
	}

	private void getAbstractConvergence(List<IRefinesEvent> refinesClauses,
			List<IAbstractEventInfo> abstractEventInfos)
			throws RodinDBException {
		for (IAbstractEventInfo abstractEventInfo : abstractEventInfos) {
			if (abstractEventInfo.getEvent().hasConvergence()) {
				if (abstractEventInfo.getEvent().getConvergence() == Convergence.CONVERGENT) {
					abstractCvg = true;
					if (abstractEventInfo.getEvent().hasAttribute(
							PROB_ATTRIBUTE)) {
						if (abstractEventInfo.getEvent().getAttributeValue(
								PROB_ATTRIBUTE)) {
							abstractProb = true;
						}
					}
				}
			}
		}

	}

	// If there is probabilistic event but no bound.
	private void checkBoundConvergence(IConcreteEventInfo concreteEventInfo)
			throws CoreException {
		final IBound[] bounds = machineRoot
				.getChildrenOfType(IBound.ELEMENT_TYPE);
		if (bounds.length == 0 && variantInfo.getExpression() != null)
			if (concreteCvg == IConvergenceElement.Convergence.CONVERGENT
					&& concreteProb) {
				createProblemMarker(concreteEventInfo.getEvent(),
						PROB_ATTRIBUTE,
						ProbabilisticGraphProblem.ProbEventNoBoundError,
						concreteEventInfo.getEventLabel());
			}
	}

	// Ordinary event cannot be probabilistic
	private void checkOrdiProbConvergence(IConcreteEventInfo concreteEventInfo)
			throws CoreException {
		if (concreteCvg == IConvergenceElement.Convergence.ORDINARY
				&& concreteProb) {
			createProblemMarker(concreteEventInfo.getEvent(), PROB_ATTRIBUTE,
					ProbabilisticGraphProblem.ProbEventOrdinaryWarning,
					concreteEventInfo.getEventLabel());
			concreteProb = false;
		}
	}

	// Anticipated event cannot be probabilistic
	private void checkAntiProbConvergence(IConcreteEventInfo concreteEventInfo)
			throws CoreException {
		if (concreteCvg == IConvergenceElement.Convergence.ANTICIPATED
				&& concreteProb) {
			createProblemMarker(concreteEventInfo.getEvent(), PROB_ATTRIBUTE,
					ProbabilisticGraphProblem.ProbEventAnticipatedWarning,
					concreteEventInfo.getEventLabel());
			concreteProb = false;
		}
	}

	// abstract event is probabilistic, but concrete is not.
	private void checkAbstractProb1(IConcreteEventInfo concreteEventInfo)
			throws CoreException {
		if (concreteCvg != IConvergenceElement.Convergence.CONVERGENT
				&& abstractProb) {
			createProblemMarker(concreteEventInfo.getEvent(),
					EventBAttributes.CONVERGENCE_ATTRIBUTE,
					ProbabilisticGraphProblem.EventMustProb,
					concreteEventInfo.getEventLabel());
		}
	}

	// abstract event is probabilistic, but concrete is not.
	private void checkAbstractProb2(IConcreteEventInfo concreteEventInfo)
			throws CoreException {
		if (!concreteProb && abstractProb) {
			createProblemMarker(concreteEventInfo.getEvent(), PROB_ATTRIBUTE,
					ProbabilisticGraphProblem.EventMustProb,
					concreteEventInfo.getEventLabel());
		}
	}

	// abstract event is convergent but not probabilistic.
	private void checkAbsProbConvergence(IConcreteEventInfo concreteEventInfo)
			throws CoreException {
		if (concreteCvg == IConvergenceElement.Convergence.CONVERGENT
				&& concreteProb && abstractCvg && !abstractProb) {
			createProblemMarker(concreteEventInfo.getEvent(), PROB_ATTRIBUTE,
					ProbabilisticGraphProblem.ProbAbsNotConvWarning,
					concreteEventInfo.getEventLabel());
			concreteProb = false;
		}
	}

	@Override
	public void endModule(ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		variantInfo = null;
	}

}
