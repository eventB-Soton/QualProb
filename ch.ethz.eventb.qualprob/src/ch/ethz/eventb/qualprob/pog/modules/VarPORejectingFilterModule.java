package ch.ethz.eventb.qualprob.pog.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPORoot;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.POGFilterModule;
import org.eventb.core.pog.state.IPOGStateRepository;
import org.eventb.core.tool.IModuleType;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.utils.EventBUtils;

public class VarPORejectingFilterModule extends POGFilterModule {

	private static final IModuleType<VarPORejectingFilterModule> MODULE_TYPE = POGCore
			.getModuleType(QualProbPlugin.PLUGIN_ID
					+ ".varPORejectingModule");
	
	private IMachineRoot mchRoot;
	
	@Override
	public void initModule(IPOGStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		IPORoot target = repository.getTarget();
		mchRoot = target.getMachineRoot();
	}

	@Override
	public boolean accept(String poName, IProgressMonitor monitor)
			throws CoreException {
		
		if (poName.endsWith("/VAR")) {
			// Extract the event name.
			String evtName = poName.replaceAll("/VAR", "");
			// Get the event.
			IEvent evt = EventBUtils.getEvent(mchRoot, evtName);
			// accept the PO if the event is not convergent.
			if (evt.getConvergence() != Convergence.CONVERGENT)
				return true;
			
			// filter the PO if the event is probabilistic convergent.
			if (evt.hasAttribute(QualProbPlugin.PROB_ATTRIBUTE)
					&& evt.getAttributeValue(QualProbPlugin.PROB_ATTRIBUTE)) {
				return false;
			}
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
