package at.ac.tuwien.infosys.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import at.ac.tuwien.infosys.model.DeviceUpdate;

public interface DeviceUpdateRepository extends JpaRepository<DeviceUpdate, String> {

}
