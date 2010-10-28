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

import static org.eventb.core.ast.Formula.BTRUE;

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
import org.eventb.core.ast.RelationalPredicate;
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
 * Generating proof obligations :
 * <ul>
 * <li><b>(BWD)</b> is bound well-defined</li>
 * </ul>
 */
public class FwdMachineBoundModule extends POGProcessorModule {

	public static final IModuleType<FwdMachineBoundModule> MODULE_TYPE = POGCore
			.getModuleType(QualProbPlugin.PLUGIN_ID + ".fwdMachineBoundModule");
	private static final List<IPOGPredicate> emptypredicates = Collections.emptyList();
	private static final IPOGHint[] NO_HINTS = new IPOGHint[0];

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void process(IRodinElement element, IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {

		final IPORoot target = repository.getTarget();
		final IMachineBoundInfo boundInfo = (IMachineBoundInfo) repository
				.getState(IMachineBoundInfo.STATE_TYPE);

		if (!boundInfo.machineHasBound()) {
			return;
		}
		
		final ITypeEnvironment typeEnv = repository.getTypeEnvironment();
		final FormulaFactory ff = typeEnv.getFormulaFactory();
		final Predicate wdPredicate = boundInfo.getExpression().getWDPredicate(
				ff);
		final ISCBound scBound = boundInfo.getBound();

		final IPOGSource[] sources = new IPOGSource[] { makeSource(
				IPOSource.DEFAULT_ROLE, scBound.getSource()) };

		final IMachineHypothesisManager machineHypothesisManager = (IMachineHypothesisManager) repository
				.getState(IMachineHypothesisManager.STATE_TYPE);

		if (!goalIsTrivial(wdPredicate, ff)) {
			createPO(target, "BWD",
					POGProcessorModule.makeNature("Well-definedness of bound"),
					machineHypothesisManager.getFullHypothesis(),
					emptypredicates, makePredicate(wdPredicate, scBound.getSource()), sources,
					NO_HINTS, machineHypothesisManager.machineIsAccurate(), monitor);
		}
	}

	@Override
	public void initModule(IRodinElement element,
			IPOGStateRepository repository, IProgressMonitor monitor)
			throws CoreException {

		final IRodinFile machineFile = (IRodinFile) element;
		final ISCMachineRoot root = (ISCMachineRoot) machineFile.getRoot();
		final ISCBound[] bounds = root.getChildrenOfType(ISCBound.ELEMENT_TYPE);

		if (bounds.length != 1) {
			repository.setState(new MachineBoundInfo(null, null));
			return;
		}

		final ISCBound scBound = bounds[0];
		final ITypeEnvironment typeEnv = repository.getTypeEnvironment();
		final Expression expr = scBound.getExpression(
				typeEnv.getFormulaFactory(), typeEnv);
		repository.setState(new MachineBoundInfo(expr, scBound));
	}

	private boolean goalIsTrivial(Predicate goal, FormulaFactory ff) {
		return goal.equals(ff.makeLiteralPredicate(BTRUE, null))
				|| goalIsNotRestricting(goal, ff);
	}

	private boolean goalIsNotRestricting(Predicate goal, FormulaFactory ff) {
		if (goal instanceof RelationalPredicate) {
			final RelationalPredicate relGoal = (RelationalPredicate) goal;
			switch (relGoal.getTag()) {
			case Formula.IN:
			case Formula.SUBSETEQ:
				final Expression expression = relGoal.getRight();
				final Type type = expression.getType();
				final Type baseType = type.getBaseType();
				if (baseType == null)
					return false;
				final Expression typeExpression = baseType.toExpression(ff);
				if (expression.equals(typeExpression))
					return true;
				break;
			default:
				return false;
			}
		}
		return false;
	}
}
