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

import static org.eventb.core.ast.LanguageVersion.V2;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCFilterModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.qualprob.basis.IBound;


public class MachineBoundFreeIdentsModule extends SCFilterModule {

	public static final IModuleType<MachineBoundModule> MODULE_TYPE = SCCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".machineBoundFreeIdentsModule");

	private static final Set<IFormulaExtension> extensions = Collections
			.emptySet();
	private static final FormulaFactory factory = FormulaFactory
			.getInstance(extensions);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eventb.internal.core.tool.types.ISCFilterModule#accept(org.rodinp
	 * .core.IRodinElement, org.eventb.core.sc.state.ISCStateRepository,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean accept(IRodinElement element, ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		final IRodinFile file = (IRodinFile) element;
		final IMachineRoot root = (IMachineRoot) file.getRoot();

		final IBound[] bounds = root.getChildrenOfType(IBound.ELEMENT_TYPE);

		// we know that there is just one bound as this filter is called after
		// processor checking on bounds cardinality
		final IBound bound = bounds[0];

		final IParseResult parseResult = factory.parseExpression(
				bound.getExpressionString(), V2, bound);

		if (parseResult.hasProblem()) {
			createProblemMarker(bound, EventBAttributes.EXPRESSION_ATTRIBUTE,
					ProbabilisticGraphProblem.InvalidBoundExpressionError,
					bound);
			return false;
		}

		// No problems, so the expression is parsed
		final ITypeEnvironment typeEnv = repository.getTypeEnvironment();
		final Expression parsedExpression = parseResult.getParsedExpression();

		final FreeIdentifier[] freeIdentifiers = parsedExpression
				.getFreeIdentifiers();

		boolean ok = true;
		for (FreeIdentifier ident : freeIdentifiers) {
			if (!typeEnv.contains(ident.getName())) {
				createProblemMarker(bound,
						EventBAttributes.EXPRESSION_ATTRIBUTE,
						ProbabilisticGraphProblem.BoundFreeIdentifierError,
						ident);
				ok = false;
			}
		}
		return ok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.core.tool.types.IModule#getModuleType()
	 */
	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
