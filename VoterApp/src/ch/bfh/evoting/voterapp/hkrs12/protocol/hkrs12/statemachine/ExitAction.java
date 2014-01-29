package ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.statemachine;

import ch.bfh.evoting.voterapp.hkrs12.AndroidApplication;
import ch.bfh.evoting.voterapp.hkrs12.protocol.HKRS12ProtocolInterface;

import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.Transition;
import com.continuent.tungsten.fsm.core.TransitionFailureException;
import com.continuent.tungsten.fsm.core.TransitionRollbackException;

public class ExitAction extends AbstractAction {

	public ExitAction() {
		super();
	}

	@Override
	public void doAction(Event arg0, Entity arg1, Transition arg2, int arg3)
			throws TransitionRollbackException, TransitionFailureException,
			InterruptedException {
		((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).reset();
	}

}
