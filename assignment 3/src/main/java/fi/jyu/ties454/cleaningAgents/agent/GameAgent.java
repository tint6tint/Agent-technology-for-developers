package fi.jyu.ties454.cleaningAgents.agent;

import java.util.Optional;
import java.util.UUID;

import fi.jyu.ties454.cleaningAgents.infra.Device;
import fi.jyu.ties454.cleaningAgents.infra.Manager;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GameAgent extends Agent {

	private static final long serialVersionUID = 1L;

	public static class NoSuchDeviceException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public <E extends Device> NoSuchDeviceException(Class<E> deviceClass) {
			super("There is no device of class " + deviceClass.getName() + " available");
		}
	}

	/**
	 * Sends a request to get the device specified by deviceClass.
	 *
	 * @param deviceClass
	 * @return
	 */
	public <E extends Device> Optional<E> getDevice(Class<E> deviceClass) {
		try {
			ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
			m.addReceiver(Manager.AID);
			m.setProtocol(Manager.DEVICE_ACQUISITION_PROTOCOL);
			m.setContent(deviceClass.getName());
			m.setReplyWith(UUID.randomUUID().toString());
			this.send(m);
			ACLMessage response = this.blockingReceive(MessageTemplate.MatchInReplyTo(m.getReplyWith()));
			if (response.getPerformative() == ACLMessage.AGREE) {
				if (this.currentDevice == null) {
					throw new Error("Manager informaed that device was delivered, but nothing received.");
				}
				E theCurrentDevice = deviceClass.cast(this.currentDevice);
				return Optional.of(theCurrentDevice);
			} else if (response.getPerformative() == ACLMessage.REFUSE) {
				return Optional.empty();
			} else if (response.getPerformative() == ACLMessage.FAILURE) {
				throw new NoSuchDeviceException(deviceClass);
			} else {
				throw new Error(Manager.AID.getLocalName() + " send message with unknown performative");
			}
		} finally {
			this.currentDevice = null;
		}
	}

	private Device currentDevice = null;

	public final void update(Device d) {
		this.currentDevice = d;
	}

}
