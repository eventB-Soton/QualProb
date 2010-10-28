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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPORoot;
import org.eventb.core.IPOSource;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.ProductType;
import org.eventb.core.ast.Type;
import org.eventb.core.pog.IPOGHint;
import org.eventb.core.pog.IPOGPredicate;
import org.eventb.core.pog.IPOGSource;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGProcessorModule;
import org.eventb.core.pog.state.IMachineHypothesisManager;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.qualprob.basis.ISCBound;
import ch.ethz.eventb.qualprob.pog.states.IMachineBoundInfo;
import ch.ethz.eventb.qualprob.pog.states.MachineBoundInfo;


/**
 * Implementation of BNF PO generation
 */
public class FwdMachineBoundFinitenessModule extends POGProcessorModule {

	private static final IModuleType<FwdMachineBoundFinitenessModule> MODULE_TYPE = POGCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".fwdMachineBoundFinitenessModule");
	private static final List<IPOGPredicate> emptypredicates = Collections.emptyList();
	private static final IPOGHint[] NO_HINTS = new IPOGHint[0];

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void process(IRodinElement element, IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {

		final IMachineBoundInfo machineBoundInfo = (IMachineBoundInfo) repository
				.getState(IMachineBoundInfo.STATE_TYPE);

		final ISCBound scBound = machineBoundInfo.getBound();
		if (scBound == null) {
			return;
		}
		
		final Expression expr = machineBoundInfo.getExpression();
		final FormulaFactory ff = repository.getFormulaFactory();

		final IPOGSource[] sources = new IPOGSource[] { makeSource(
				IPOSource.DEFAULT_ROLE, scBound.getSource()) };
		final IPORoot target = repository.getTarget();

		final IMachineHypothesisManager machineHypothesisManager = (IMachineHypothesisManager) repository
				.getState(IMachineHypothesisManager.STATE_TYPE);

		// if the finitness of bound is not trivial
		// we generate the PO
		if (mustProveFinite(expr, ff)) {
			final Predicate finPredicate = ff.makeSimplePredicate(
					Formula.KFINITE, expr, null);
			createPO(target, "BFN",
					POGProcessorModule.makeNature("Finiteness of bound"),
					machineHypothesisManager.getFullHypothesis(),
					emptypredicates, makePredicate(finPredicate, scBound.getSource()), sources,
					NO_HINTS, machineHypothesisManager.machineIsAccurate(), monitor);
		}
	}

	@Override
	public void initModule(IRodinElement element,
			IPOGStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		repository.setState(createMachineBoundInfo(element, repository));
	}

	private IMachineBoundInfo createMachineBoundInfo(IRodinElement element,
			IPOGStateRepository repository) throws CoreException {
		final IRodinFile machineFile = (IRodinFile) element;
		final ISCMachineRoot root = (ISCMachineRoot) machineFile.getRoot();
		final ISCBound[] bounds = root.getChildrenOfType(ISCBound.ELEMENT_TYPE);
		if (bounds.length != 1) {
			return new MachineBoundInfo();
		}
		final ISCBound scBound = bounds[0];
		final ITypeEnvironment typeEnv = repository.getTypeEnvironment();
		final Expression expr = scBound.getExpression(
				typeEnv.getFormulaFactory(), typeEnv);
		return new MachineBoundInfo(expr, scBound);
	}

	/**
	 * Returns <code>true</code> if the PO for finiteness has to be generated
	 * for the given bound expression, <code>false</code> otherwise.
	 * 
	 * @param expr
	 *            the bound expression under consideration
	 * @param ff
	 *            the formula factory to use
	 * @return <code>true</code> if the PO for finiteness has to be generated
	 *         for the given expression, <code>false</code> otherwise
	 */
	private boolean mustProveFinite(Expression expr, FormulaFactory ff) {
		final Type type = expr.getType();
		if (type.equals(ff.makeIntegerType()))
			return false;
		if (derivedFromBoolean(type, ff))
			return false;
		return true;
	}

	/**
	 * Returns <code>true</code> if the given type is composed from boolean
	 * types, <code>false</code> otherwise.
	 * 
	 * @param type
	 *            the type to analyze
	 * @param ff
	 *            the formula factory to use
	 * @return <code>true</code> if the given type is composed from boolean
	 *         types, <code>false</code> otherwise
	 */
	private boolean derivedFromBoolean(Type type, FormulaFactory ff) {
		if (type.equals(ff.makeBooleanType()))
			return true;
		final Type baseType = type.getBaseType();
		if (baseType != null)
			return derivedFromBoolean(baseType, ff);
		if (type instanceof ProductType) {
			final ProductType productType = (ProductType) type;
			return derivedFromBoolean(productType.getLeft(), ff)
					&& derivedFromBoolean(productType.getRight(), ff);
		}
		return false;
	}

}
