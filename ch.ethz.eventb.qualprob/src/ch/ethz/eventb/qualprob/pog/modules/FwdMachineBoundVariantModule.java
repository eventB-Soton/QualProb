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
package ch.ethz.eventb.qualprob.pog.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCVariant;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.IntegerType;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGProcessorModule;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.qualprob.basis.ISCBound;


/**
 * Generating proof obligation for bound variant relation :
 * <p>
 * <b>(BND)</b> is variant smaller than bound or subset of bound
 * </p>
 */
public class FwdMachineBoundVariantModule extends POGProcessorModule {

	public static final IModuleType<FwdMachineBoundVariantModule> MODULE_TYPE = POGCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".fwdMachineBoundVariantModule");

	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void process(IRodinElement element, IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {

		final ISCEvent scEvent = (ISCEvent) element;
		final ISCMachineRoot machineRoot = (ISCMachineRoot) scEvent.getRoot();

		final ITypeEnvironment typeEnv = repository.getTypeEnvironment();

		final ISCBound[] scBounds = machineRoot
				.getChildrenOfType(ISCBound.ELEMENT_TYPE);
		final ISCVariant[] scVariants = machineRoot.getSCVariants();
		if (scBounds.length != 1)
			return;

		if (scVariants.length != 1)
			return;

		final ISCBound scBound = scBounds[0];
		final ISCVariant scVariant = scVariants[0];

		final FormulaFactory ff = typeEnv.getFormulaFactory();
		final Expression boundExpression = scBound.getExpression(ff, typeEnv);
		final Expression variantExpression = scVariant.getExpression(ff,
				typeEnv);
		final IntegerType integerType = ff.makeIntegerType();
		final boolean isIntVariant = boundExpression.getType().equals(
				integerType);
		final boolean isIntBound = variantExpression.getType().equals(
				integerType);

		if (isIntVariant != isIntBound)
			return;

//		final IPORoot target = repository.getTarget();
//		final IPOGSource[] sources = new IPOGSource[] {
//				makeSource(IPOSource.DEFAULT_ROLE, scEvent.getSource()),
//				makeSource(IPOSource.DEFAULT_ROLE, scBound.getSource()),
//				makeSource(IPOSource.DEFAULT_ROLE, scVariant.getSource()) };
//		final IEventHypothesisManager eventHypothesisManager = (IEventHypothesisManager) repository
//				.getState(IEventHypothesisManager.STATE_TYPE);
//
//		final Predicate bvrPredicate = getBoundVariantPredicate(ff,
//				boundExpression, variantExpression, isIntBound);
//		
//		final List<IPOGPredicate> guards = getGuards(scEvent, typeEnv);
//		createPO(
//				target,// target
//				// name
//				scEvent.getLabel() + "/BND",
//				// the nature of the proof obligation
//				POGProcessorModule.makeNature("Bound-Variant Relation"),
//				// globalHypotheses (shared between proof obligations)
//				eventHypothesisManager.getFullHypothesis(),
//				// localHypotheses (not shared between proof obligations)
//				guards,
//				// the goal to be proved
//				makePredicate(bvrPredicate, scBound.getSource()),
//				// sources references to source elements from which the
//				// proof obligation was derived
//				sources,
//				// hints for a theorem prover
//				NO_HINTS,
//				// accurate the accuracy of the PO sequent
//				eventHypothesisManager.eventIsAccurate(),
//				// a progress mon
//				monitor);
	}

//	private List<IPOGPredicate> getGuards(ISCEvent scEvent,
//			ITypeEnvironment typeEnv) throws RodinDBException {
//		final ISCGuard[] guards = scEvent.getSCGuards();
//		final List<IPOGPredicate> result = new ArrayList<IPOGPredicate>();
//		for (ISCGuard scGuard : guards) {
//			final Predicate guardPredicate = scGuard.getPredicate(
//					typeEnv.getFormulaFactory(), typeEnv);
//			result.add(makePredicate(guardPredicate, scEvent.getSource()));
//		}
//		return result;
//	}
//
//	private Predicate getBoundVariantPredicate(FormulaFactory ff,
//			Expression boundExpression, Expression variantExpression,
//			boolean isIntBound) {
//		return ff.makeRelationalPredicate(
//				//
//				(isIntBound ? Formula.LE : Formula.SUBSETEQ),
//				variantExpression, boundExpression, null);
//	}

}
