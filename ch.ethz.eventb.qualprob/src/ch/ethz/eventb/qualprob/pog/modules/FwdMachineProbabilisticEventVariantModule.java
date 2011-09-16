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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSource;
import org.eventb.core.ISCAction;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.Type;
import org.eventb.core.pog.IPOGHint;
import org.eventb.core.pog.IPOGPredicate;
import org.eventb.core.pog.IPOGSource;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGProcessorModule;
import org.eventb.core.pog.state.IAbstractEventGuardList;
import org.eventb.core.pog.state.IConcreteEventActionTable;
import org.eventb.core.pog.state.IEventHypothesisManager;
import org.eventb.core.pog.state.IMachineHypothesisManager;
import org.eventb.core.pog.state.IMachineVariantInfo;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.qualprob.QualProbPlugin;


/**
 * Generation of the PRV PO
 */
public class FwdMachineProbabilisticEventVariantModule extends POGProcessorModule {

	private static final IModuleType<FwdMachineProbabilisticEventVariantModule> MODULE_TYPE = POGCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".fwdMachineEventVariantModule");

	public static final IAttributeType.Boolean PROB_ATTRIBUTE = RodinCore
			.getBooleanAttrType(QualProbPlugin.PLUGIN_ID + ".probabilistic");

	private Convergence concreteConvergence;
	private boolean concreteProb;
	private boolean accurate;
	private IConcreteEventActionTable concreteEventActionTable;
	private IEventHypothesisManager eventHypothesisManager;
	private IPOGHint[] NO_HINTS = new IPOGHint[0];
	private IMachineHypothesisManager machineHypothesisManager;

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void process(IRodinElement element, IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {

		final IMachineVariantInfo machineVariantInfo = (IMachineVariantInfo) repository
				.getState(IMachineVariantInfo.STATE_TYPE);

		final ISCEvent scEvent = (ISCEvent) element;
		final IEvent event = (IEvent) scEvent.getSource();
		final ISCMachineRoot scRoot = (ISCMachineRoot) scEvent.getRoot();
		final FormulaFactory ff = scRoot.getFormulaFactory();
 
		// no PO if no variant.
		final Expression varExpression = machineVariantInfo.getExpression();
		if (varExpression == null)
			return;
		
		// no PO if not convergent
		final Convergence eventConvergence = event.getConvergence();
		if (eventConvergence != Convergence.CONVERGENT) {
			return;
		}

		// no PO if the abstract event was convergent
		if (getAbstractConvergence(repository) == Convergence.CONVERGENT) {
			return;
		}
		
		// no PO if this is not probabilistic
		concreteProb = event.getAttributeValue(PROB_ATTRIBUTE);
		if (!concreteProb) {
			return;
		}

		final IPORoot target = repository.getTarget();

		concreteEventActionTable = (IConcreteEventActionTable) repository
				.getState(IConcreteEventActionTable.STATE_TYPE);

		final List<BecomesEqualTo> substitution = new LinkedList<BecomesEqualTo>();
		if (concreteEventActionTable.getDeltaPrime() != null) {
			substitution.add(concreteEventActionTable.getDeltaPrime());
		}

		Expression nextVarExpression = varExpression.applyAssignments(
				substitution, ff);
		substitution.clear();
		substitution.addAll(concreteEventActionTable.getPrimedDetAssignments());
		nextVarExpression = nextVarExpression
				.applyAssignments(substitution, ff);

		boolean isIntVariant = varExpression.getType().equals(
				ff.makeIntegerType());
		final Predicate varPredicate = getVarPredicate(ff, nextVarExpression,
				varExpression, isIntVariant);

		final List<Predicate> andPredicate = concreteEventActionTable
				.getNondetPredicates();

		final BecomesEqualTo beq = concreteEventActionTable.getDeltaPrime();

		if (beq == null) {
			return;
		}
		
		final FreeIdentifier[] freeIdents = beq.getFreeIdentifiers();
		
		final List<FreeIdentifier> containedFreeIdents = getPrimedContainedFreeIdents(
				freeIdents, varPredicate, andPredicate);

		final IRodinElement variantSource = machineVariantInfo.getVariant()
				.getSource();
		final IPOGSource[] sources = new IPOGSource[] {
				makeSource(IPOSource.DEFAULT_ROLE, variantSource),
				makeSource(IPOSource.DEFAULT_ROLE, event) };

		final ArrayList<IPOGPredicate> hyp = makeActionHypothesis(varPredicate);

		eventHypothesisManager = (IEventHypothesisManager) repository
				.getState(IEventHypothesisManager.STATE_TYPE);
		machineHypothesisManager = (IMachineHypothesisManager) repository
				.getState(IMachineHypothesisManager.STATE_TYPE);
		accurate = machineHypothesisManager.machineIsAccurate()
				&& eventHypothesisManager.eventIsAccurate();

		// only generate PO if it is probabilistic
		if (andPredicate != null) {
			final String sequentNamePRV = event.getLabel() + "/PRV";
			Predicate probPredicate = getProbVarPredicate(ff,
					containedFreeIdents, andPredicate, varPredicate,
					isIntVariant);
			probPredicate = probPredicate.flatten(ff);

			createPO(target, sequentNamePRV,
					POGProcessorModule
							.makeNature("Probabilistic variant of event"),
					eventHypothesisManager.getFullHypothesis(), hyp,
					makePredicate(probPredicate, variantSource), sources,
					NO_HINTS, accurate, monitor);
		}
	}

	private List<FreeIdentifier> getPrimedContainedFreeIdents(
			FreeIdentifier[] freeIdents, Predicate varPredicate,
			List<Predicate> andPredicates) {
		final List<FreeIdentifier> result = new ArrayList<FreeIdentifier>();
		final List<FreeIdentifier> managedFreeIdents = new ArrayList<FreeIdentifier>();
		for (Predicate andPred : andPredicates) {
			managedFreeIdents
					.addAll(Arrays.asList(andPred.getFreeIdentifiers()));
		}

		for (FreeIdentifier fid : freeIdents) {
			if (fid.isPrimed() && managedFreeIdents.contains(fid)) {
				result.add(fid);
			}
		}
		return result;
	}

	private Predicate getVarPredicate(FormulaFactory ff,
			Expression nextVarExpression, Expression varExpression,
			boolean isIntVariant) {
		int tag;
		if (concreteConvergence == Convergence.ANTICIPATED)
			if (isIntVariant)
				tag = Formula.LE;
			else
				tag = Formula.SUBSETEQ;
		else if (isIntVariant)
			tag = Formula.LT;
		else
			tag = Formula.SUBSET;

		Predicate varPredicate = ff.makeRelationalPredicate(tag,
				nextVarExpression, varExpression, null);
		return varPredicate;
	}

	private Predicate getProbVarPredicate(FormulaFactory ff,
			List<FreeIdentifier> freeIdents, List<Predicate> andPredicate,
			Predicate varPredicate, boolean isIntVariant) {

		Collection<Predicate> predicates = new LinkedList<Predicate>();
		predicates.addAll(andPredicate);
		predicates.add(varPredicate);

		final Predicate existPredicate = ff.makeAssociativePredicate(
				Formula.LAND, predicates, null);
		final Predicate boundExistPredicate = existPredicate.bindTheseIdents(freeIdents, ff);

		// Generate bound identifier declarations.
		final int size = freeIdents.size();
		BoundIdentDecl[] boundIdentDecls = new BoundIdentDecl[size];
		for (int i = 0; i < size; i++) {
			final String name = freeIdents.get(i).getName();
			final Type type = freeIdents.get(i).getType();
			boundIdentDecls[i] = ff.makeBoundIdentDecl(name, null, type);
		}

		final Predicate probvarPredicate = ff.makeQuantifiedPredicate(
				Formula.EXISTS, boundIdentDecls, boundExistPredicate, null);
		return probvarPredicate;

	}

	private Convergence getAbstractConvergence(IPOGStateRepository repository)
			throws CoreException, RodinDBException {
		final IAbstractEventGuardList abstractEventGuardList = (IAbstractEventGuardList) repository
				.getState(IAbstractEventGuardList.STATE_TYPE);
		final List<ISCEvent> abstractEvents = abstractEventGuardList
				.getAbstractEvents();
		if (abstractEvents.size() == 0) {
			return null;
		}
		final List<Convergence> convergences = new ArrayList<Convergence>(
				abstractEvents.size());

		for (ISCEvent abstractEvent : abstractEvents) {
			convergences.add(abstractEvent.getConvergence());
		}
		return Collections.min(convergences);
	}

	private Set<FreeIdentifier> addAllFreeIdents(Set<FreeIdentifier> identSet,
			FreeIdentifier[] identifiers) {
		for (FreeIdentifier identifier : identifiers) {
			identSet.add(identifier);
		}
		return identSet;
	}

	private Set<FreeIdentifier> newFreeIdentsFromPredicate(Predicate predicate) {
		FreeIdentifier[] identifiers = predicate.getFreeIdentifiers();
		HashSet<FreeIdentifier> identSet = new HashSet<FreeIdentifier>();
		return addAllFreeIdents(identSet, identifiers);
	}

	private ArrayList<IPOGPredicate> newLocalHypothesis() {
		int size = concreteEventActionTable.getNondetActions().size();
		return new ArrayList<IPOGPredicate>(size);
	}

	private void makeActionHypothesis(ArrayList<IPOGPredicate> hyp,
			Set<FreeIdentifier> freeIdents) throws RodinDBException {
		// create local hypothesis for nondeterministic assignments

		List<Predicate> nondetPredicates = concreteEventActionTable
				.getNondetPredicates();
		List<ISCAction> nondetActions = concreteEventActionTable
				.getNondetActions();

		for (int i = 0; i < nondetPredicates.size(); i++) {
			Predicate baPredicate = nondetPredicates.get(i);
			for (FreeIdentifier ident : baPredicate.getFreeIdentifiers()) {
				if (ident.isPrimed() && freeIdents.contains(ident)) {
					hyp.add(makePredicate(baPredicate, nondetActions.get(i)
							.getSource()));
					break;
				}
			}

		}

	}

	private ArrayList<IPOGPredicate> makeActionHypothesis(Predicate predicate)
			throws RodinDBException {
		// create local hypothesis for nondeterministic assignments

		ArrayList<IPOGPredicate> hyp = newLocalHypothesis();
		Set<FreeIdentifier> freeIdents = newFreeIdentsFromPredicate(predicate);

		makeActionHypothesis(hyp, freeIdents);

		return hyp;
	}

}
