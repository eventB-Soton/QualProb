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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IExpressionElement;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariant;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Type;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.qualprob.basis.IBound;
import ch.ethz.eventb.qualprob.basis.ISCBound;


public class MachineBoundModule extends SCProcessorModule {

	// The name prefix used to register bounds in the database.
	// As there is only one bound, we don't have to calculate indexes.
	private static final String BOUND_NAME_PREFIX = "BND";

	public static final IModuleType<MachineBoundModule> MODULE_TYPE = SCCore
			.getModuleType(QualProbPlugin.PLUGIN_ID + ".machineBoundModule");

	public static final IAttributeType.Boolean PROB_ATTRIBUTE = RodinCore
			.getBooleanAttrType(QualProbPlugin.PLUGIN_ID + ".probabilistic");

	private FormulaFactory factory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.core.tool.types.IModule#getModuleType()
	 */
	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

	@Override
	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {

		monitor.subTask("ProcessingBound");

		final IRodinFile machineFile = (IRodinFile) element;
		final IMachineRoot machineRoot = (IMachineRoot) machineFile.getRoot();
		final ProbConvDesc pbDesc = new ProbConvDesc(machineRoot);
		factory = machineRoot.getFormulaFactory();
		
		final IBound[] bounds = machineRoot
				.getChildrenOfType(IBound.ELEMENT_TYPE);
		final IVariant[] variants = machineRoot.getVariants();

		if (!pbDesc.isProbabilistic()) {
			// if the model is not probabilistic but there is a bound
			if (bounds.length > 0) {
				createProblemMarker(
						bounds[bounds.length - 1],
						EventBAttributes.EXPRESSION_ATTRIBUTE,
						ProbabilisticGraphProblem.NoProbabilisticEventButBoundWarning);
			}
		}

		// we check that there is just one bound
		if (pbDesc.isProbabilistic() && bounds.length > 1) {
			// return an error cause there is too much bounds
			createProblemMarker(bounds[bounds.length - 1],
					EventBAttributes.EXPRESSION_ATTRIBUTE,
					ProbabilisticGraphProblem.TooManyBoundsError);
			return;
		}
		// variants.length == 1
		if (pbDesc.isProbabilistic()) {
			ITypeEnvironment typeEnv = repository.getTypeEnvironment();
				
			// No variant --> No bound
			if (variants.length == 0)
				return;
			
			final IVariant variant = variants[0];
			if (!assertIsASetOrConstant(variant, typeEnv)) {
				return;
			}

			// it must exist a bound in the model
			if (!existABound(machineRoot, bounds)) {
				return;
			}

			// call to the filter modules, if something went wrong we return
			if (!filterModules(element, repository, monitor)) {
				return;
			}

			// We are sure that the expression typeChecks because it comes after Bound
			// filter module.
			final IBound bound = bounds[0];
			if (!assertIsASetOrConstant(bound, typeEnv)) {
				return;
			}
			if (!getType(bound, typeEnv).equals(getType(variant, typeEnv))) {
				createProblemMarker(bound,
						EventBAttributes.EXPRESSION_ATTRIBUTE,
						ProbabilisticGraphProblem.VariantBoundTypeError);
				return;
			}
			final ISCBound scBound = target.getInternalElement(
					ISCBound.ELEMENT_TYPE, BOUND_NAME_PREFIX);
			scBound.create(null, monitor);
			scBound.setExpression(getExpression(bound), null);
			scBound.setSource(bound, monitor);
		}
	}

	private boolean existABound(IMachineRoot root, IBound[] bounds)
			throws RodinDBException {
		if (bounds.length != 1) {
			createProblemMarker(root, EventBAttributes.EXPRESSION_ATTRIBUTE,
					ProbabilisticGraphProblem.BoundMustBeSpecified);
			return false;
		}
		return true;
	}

	private boolean assertIsASetOrConstant(IExpressionElement element, ITypeEnvironment typeEnv)
			throws RodinDBException {
		
		final Type type = getType(element, typeEnv);
		if (type == null) {
			return false;
		}
		if (!isASetOrConstant(type)) {
			if (element instanceof IBound) {
				createProblemMarker(element,
						EventBAttributes.EXPRESSION_ATTRIBUTE,
						ProbabilisticGraphProblem.BoundMustBeConstantError);
			}
			if (element instanceof IVariant) {
				createProblemMarker(element,
						EventBAttributes.EXPRESSION_ATTRIBUTE,
						ProbabilisticGraphProblem.VariantMustBeConstantError);
			}
			return false;
		}
		return true;
	}

	private Type getType(IExpressionElement element, ITypeEnvironment typeEnv) throws RodinDBException {
		final Expression boundExpression = getExpression(element);
		boundExpression.typeCheck(typeEnv);
		return boundExpression.getType();
	}

	private Expression getExpression(IExpressionElement element)
			throws RodinDBException {
		final String formula = element.getExpressionString();
		final IParseResult result = factory.parseExpression(formula, element);
		final Expression boundExpression = result.getParsedExpression();
		return boundExpression;
	}

	private boolean isASetOrConstant(Type type) {
		if (type.equals(factory.makeIntegerType())) {
			return true;
		}
		final Type baseType = type.getBaseType();
		if (baseType != null) {
			return type.equals(factory.makePowerSetType(baseType));
		}
		return false;
	}

	/**
	 * Class used to calculate the presence of a convergent event, and the
	 * presence of probabilistic event.
	 */
	private static class ProbConvDesc {

		private boolean convergent;
		private boolean probabilistic;

		/**
		 * The constructor computes the root to assign values of convergence and
		 * probabilistic convergence
		 * 
		 * @param root
		 *            the computed machine root
		 */
		public ProbConvDesc(IMachineRoot root) {
			convergent = false;
			probabilistic = false;
			try {
				convergent = isConvergentModel(root);
				probabilistic = isProbabilisticModel(root);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		/**
		 * @return <code>true</code> if there is a convergent event in the
		 *         model, <code>false</code> otherwise
		 */
		@SuppressWarnings("unused")
		public boolean isConvergent() {
			return convergent;
		}

		/**
		 * @return <code>true</code> if there is a probabilistic convergent
		 *         event in the model, <code>false</code> otherwise
		 */
		public boolean isProbabilistic() {
			return probabilistic;
		}

		/**
		 * Returns <code>true</code> if the model aims to be proved against
		 * probabilistic convergence. This means that at least there is a
		 * convergent probabilistic event in the model.
		 * 
		 * @param machineRoot
		 *            the machine that we are static checking
		 * @return <code>true</code> if at least one event of the machine is
		 *         probabilistic, <code>false</code> otherwise
		 * @throws RodinDBException
		 */
		private boolean isProbabilisticModel(IMachineRoot machineRoot)
				throws CoreException {
			for (IEvent event : machineRoot.getEvents()) {
				// prob is true if the attribute defined and is "true".
				boolean prob = event.hasAttribute(PROB_ATTRIBUTE);
				if (prob) {
					prob = event.getAttributeValue(PROB_ATTRIBUTE);
				}
				
				
				if (event.getConvergence() == Convergence.CONVERGENT
						&& prob) {
					// event is probabilistic if convergence attribute is
					// "Convergent" and prob is set.
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns <code>true</code> if the model is convergent. This means that
		 * at least there is a convergent event in the model.
		 * 
		 * @param machineRoot
		 *            the machine that we are static checking
		 * @return <code>true</code> if at least one event of the machine is
		 *         convergent, <code>false</code> otherwise
		 * @throws RodinDBException
		 */
		private boolean isConvergentModel(IMachineRoot machineRoot)
				throws CoreException {
			for (IEvent event : machineRoot.getEvents()) {
				if (event.getConvergence() == Convergence.CONVERGENT) {
					return true;
				}
			}
			return false;
		}

	}

}
