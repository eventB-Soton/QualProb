package ch.ethz.eventb.qualprob.pog.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPORoot;
import org.eventb.core.ISCEvent;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGFilterModule;
import org.eventb.core.pog.state.IAbstractEventGuardList;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.RodinDBException;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.utils.EventBUtils;

public class FisPORejectingFilterModule extends POGFilterModule {

	private static final IModuleType<FisPORejectingFilterModule> MODULE_TYPE = POGCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".fisPORejectingModule");

	private IMachineRoot mchRoot;
	
	private boolean absConv;
	
	@Override
	public void initModule(IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		IPORoot target = repository.getTarget();
		mchRoot = target.getMachineRoot();
		
		absConv = (getAbstractConvergence(repository) == Convergence.CONVERGENT);
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

	@Override
	public boolean accept(String poName, IProgressMonitor monitor)
			throws CoreException {
		if (poName.endsWith("/FIS")) {
			// Extract the event name.
			String[] split = poName.split("/");
			Assert.isLegal(split.length == 3, "There must be 3 components");
			String evtName = split[0];
			// Get the event.
			IEvent evt = EventBUtils.getEvent(mchRoot, evtName);
			// accept the PO if the event is not convergent.
			if (evt.getConvergence() != Convergence.CONVERGENT)
				return true;
			
			// accept the PO if the event does NOT have probabilistic attribute.
			if (!evt.hasAttribute(QualProbPlugin.PROB_ATTRIBUTE))
				return true;
			
			// accept the PO if it is NOT probabilistic.
			if (!evt.getAttributeValue(QualProbPlugin.PROB_ATTRIBUTE))
				return true;
			
			// accept the PO if the abstract event is convergence.
			if (absConv)
				return true;
			
			return false;
		}
		return true;
	}

	@Override
	public void endModule(IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		// Do nothing
	}

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
