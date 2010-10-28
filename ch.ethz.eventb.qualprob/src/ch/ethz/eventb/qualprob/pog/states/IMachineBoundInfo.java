package ch.ethz.eventb.qualprob.pog.states;

/**
 * Protocol for accessing the bound of a machine.
 */

import org.eventb.core.ast.Expression;
import org.eventb.core.pog.POGCore;
import org.eventb.core.pog.state.IPOGState;
import org.eventb.core.tool.IStateType;

import ch.ethz.eventb.qualprob.QualProbPlugin;
import ch.ethz.eventb.qualprob.basis.ISCBound;


public interface IMachineBoundInfo extends IPOGState {

	final static IStateType<IMachineBoundInfo> STATE_TYPE = 
		POGCore.getToolStateType(QualProbPlugin.PLUGIN_ID + ".machineBoundInfo");
	
	/**
	 * Returns the parsed and type-checked bound expression, or <code>null</code> 
	 * if the machine does not have a bound.
	 * 
	 * @return the parsed and type-checked bound expression, or <code>null</code> 
	 * 		if the machine does not have a bound
	 */
	Expression getExpression();
	
	/**
	 * Returns a handle to the bound, or <code>null</code> if the machine does not have a bound.
	 * 
	 * @return a handle to the bound, or <code>null</code> if the machine does not have a bound
	 */
	ISCBound getBound();
	
	/**
	 * Returns whether the machine has a bound.
	 * 
	 * @return whether the machine has a bound
	 */
	boolean machineHasBound();

}
