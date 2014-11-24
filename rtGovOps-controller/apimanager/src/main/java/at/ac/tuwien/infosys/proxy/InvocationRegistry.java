package at.ac.tuwien.infosys.proxy;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.infosys.model.Capability;

@Component
@Scope(value = "singleton")
public class InvocationRegistry {

	private ConcurrentHashMap<String, Capability> invocations = new ConcurrentHashMap<>();

	public InvocationRegistry() {
	}

	public synchronized boolean checkCapability(String device,
			String capabilityId) {

		if (!invocations.containsKey(device))
			return false;

		if (this.invocations.get(device) == null)
			return false;

		return true;
	}

	public void addDeviceCapability(String device, Capability capability) {
		this.invocations.put(device, capability);
	}

	public Capability getCapability(String deviceId) {

		return this.invocations.get(deviceId);
	}
}
