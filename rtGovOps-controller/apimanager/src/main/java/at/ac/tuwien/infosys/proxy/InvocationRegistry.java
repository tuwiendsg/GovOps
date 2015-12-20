package at.ac.tuwien.infosys.proxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.infosys.model.Capability;

@Component
@Scope(value = "singleton")
public class InvocationRegistry {

	private ConcurrentHashMap<String, Set<Capability>> invocations = new ConcurrentHashMap<>();

	public InvocationRegistry() {
	}

	public synchronized boolean checkCapability(String device,
			String capabilityId) {

		if (!invocations.containsKey(device))
			return false;

		if (this.invocations.get(device) == null)
			return false;

		if (!this.invocations.get(device).stream()
				.anyMatch(c -> c.getId().equals(capabilityId)))
			return false;

		return true;
	}

	public void addDeviceCapability(String device, Capability capability) {
		if (this.invocations.contains(device)) {
			this.invocations.get(device).add(capability);
		} else {
			Set<Capability> capabilities = new HashSet<>();
			capabilities.add(capability);
			this.invocations.put(device, capabilities);
		}
	}

	public Capability getCapability(String deviceId, String capaId) {

		return this.invocations.get(deviceId).stream()
				.filter(c -> c.getId().equals(capaId)).findFirst().get();
	}

	public void removeDevice(String deviceId) {

		if (deviceId != null) {
			this.invocations.remove(deviceId);
		}
	}
}
