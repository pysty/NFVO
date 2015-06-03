package org.project.neutrino.nfvo.api;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import javax.validation.Valid;

import org.project.neutrino.nfvo.api.exceptions.NSDNotFoundException;
import org.project.neutrino.nfvo.api.exceptions.PNFDNotFoundException;
import org.project.neutrino.nfvo.api.exceptions.VNFDNotFoundException;
import org.project.neutrino.nfvo.api.exceptions.VNFDependencyNotFoundException;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDependency;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.PhysicalNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ns-records")
public class RestNetworkServiceRecord {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NetworkServiceRecordManagement networkServiceRecordManagement;

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 * 
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return NetworkServiceRecord: the Network Service Descriptor filled with
	 *         id and values from core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public NetworkServiceRecord create(
			@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) {
		try {
			return networkServiceRecordManagement
					.onboard(networkServiceDescriptor);
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (VimException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return null;// TODO return error

	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 * 
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		try {
			networkServiceRecordManagement.delete(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);

		}
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 * 
	 * @return List<NetworkServiceRecord>: the list of Network Service
	 *         Descriptor stored
	 */

	@RequestMapping(method = RequestMethod.GET)
	public List<NetworkServiceRecord> findAll() {
		return networkServiceRecordManagement.query();
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 * 
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor selected
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public NetworkServiceRecord findById(@PathVariable("id") String id) {
		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsr;
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 * 
	 * @param networkServiceRecord
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public NetworkServiceRecord update(
			@RequestBody @Valid NetworkServiceRecord networkServiceRecord,
			@PathVariable("id") String id) {
		return networkServiceRecordManagement.update(networkServiceRecord, id);
	}

	/**
	 * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 * 
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	@RequestMapping(value = "{id}/vnfrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<VirtualNetworkFunctionRecord> getVirtualNetworkFunctionRecord(
			@PathVariable("id") String id) {
		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsr.getVnfr();
	}

	@RequestMapping(value = "{id}/vnfrecords/{id_vnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
			@PathVariable("id") String id, @PathVariable("id_vnf") String id_vnf) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		return findVNF(nsd.getVnfr(), id_vnf);
	}

	@RequestMapping(value = "{id}/vnfrecords/{id_vnf}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteVirtualNetworkFunctionDescriptor(
			@PathVariable("id") String id, @PathVariable("id_vnf") String id_vnf) {

		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		VirtualNetworkFunctionRecord nRecord = findVNF(nsr.getVnfr(), id_vnf);
		nsr.getVnfr().remove(nRecord);
	}

	@RequestMapping(value = "{id}/vnfrecords/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public VirtualNetworkFunctionRecord postVNFR(
			@RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
			@PathVariable("id") String id) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		nsd.getVnfr().add(vnfRecord);
		networkServiceRecordManagement.update(nsd, id);
		return vnfRecord;
	}

	@RequestMapping(value = "{id}/vnfrecords/{id_vnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VirtualNetworkFunctionRecord updateVNF(
			@RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
			@PathVariable("id") String id, @PathVariable("id_vnf") String id_vnf) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		VirtualNetworkFunctionRecord nRecord = findVNF(nsd.getVnfr(), id_vnf);
		nRecord = vnfRecord;
		nsd.getVnfr().add(nRecord);
		networkServiceRecordManagement.update(nsd, id);
		return nRecord;
	}

	/**
	 * Returns the list of VNFDependency into a NSD with id
	 * 
	 * @param id
	 *            : The id of NSD
	 * @return List<VNFDependency>: The List of VNFDependency into NSD
	 */

	@RequestMapping(value = "{id}/vnfdependencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<VNFDependency> getVNFDependencies(@PathVariable("id") String id) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd.getVnf_dependency();
	}

	@RequestMapping(value = "{id}/vnfdependencies/{id_vnfr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VNFDependency getVNFDependency(@PathVariable("id") String id,
			@PathVariable("id_vnfr") String id_vnfr) {
		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		return findVNFD(nsr.getVnf_dependency(), id_vnfr);
	}

	@RequestMapping(value = "{id}/vnfdependencies/{id_pnf}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteVNFDependency(@PathVariable("id") String id,
			@PathVariable("id_vnfd") String id_vnfd) {

		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		VNFDependency vnfDependency = findVNFD(nsd.getVnf_dependency(), id_vnfd);
		nsd.getVnf_dependency().remove(vnfDependency);
	}

	@RequestMapping(value = "{id}/vnfdependencies/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public VNFDependency postVNFDependency(
			@RequestBody @Valid VNFDependency vnfDependency,
			@PathVariable("id") String id) {
		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		nsr.getVnf_dependency().add(vnfDependency);
		networkServiceRecordManagement.update(nsr, id);
		return vnfDependency;
	}

	@RequestMapping(value = "{id}/vnfdependencies/{id_vnfd}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VNFDependency updateVNFD(
			@RequestBody @Valid VNFDependency vnfDependency,
			@PathVariable("id") String id,
			@PathVariable("id_pnf") String id_vnfd) {
		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		VNFDependency vDependency = findVNFD(nsr.getVnf_dependency(), id_vnfd);
		vDependency = vnfDependency;
		nsr.getVnf_dependency().add(vDependency);
		networkServiceRecordManagement.update(nsr, id);
		return vnfDependency;
	}

	/**
	 * Returns the list of PhysicalNetworkFunctionRecord into a NSD with id
	 * 
	 * @param id
	 *            : The id of NSD
	 * @return List<PhysicalNetworkFunctionRecord>: The List of
	 *         PhysicalNetworkFunctionRecord into NSD
	 */
	@RequestMapping(value = "{id}/pnfrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<PhysicalNetworkFunctionRecord> getPhysicalNetworkFunctionRecord(
			@PathVariable("id") String id) {
		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsr.getPnfr();
	}

	/**
	 * Returns the PhysicalNetworkFunctionRecord
	 * 
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         selected
	 */

	@RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public PhysicalNetworkFunctionRecord getPhysicalNetworkFunctionRecord(
			@PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		return findPNFD(nsd.getPnfr(), id_pnf);
	}

	/**
	 * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
	 * 
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 */
	@RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePhysicalNetworkFunctionRecord(
			@PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {

		NetworkServiceRecord nsr = null;
		try {
			nsr = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		PhysicalNetworkFunctionRecord pDescriptor = findPNFD(nsr.getPnfr(),
				id_pnf);
		nsr.getVnfr().remove(pDescriptor);
	}

	/**
	 * Stores the PhysicalNetworkFunctionRecord
	 * 
	 * @param pDescriptor
	 *            : The PhysicalNetworkFunctionRecord to be stored
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         stored
	 */
	@RequestMapping(value = "{id}/pnfrecords/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public PhysicalNetworkFunctionRecord postPhysicalNetworkFunctionRecord(
			@RequestBody @Valid PhysicalNetworkFunctionRecord pDescriptor,
			@PathVariable("id") String id) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		nsd.getPnfr().add(pDescriptor);
		networkServiceRecordManagement.update(nsd, id);
		return pDescriptor;
	}

	/**
	 * Edits the PhysicalNetworkFunctionRecord
	 * 
	 * @param pRecord
	 *            : The PhysicalNetworkFunctionRecord to be edited
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         edited
	 */
	@RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public PhysicalNetworkFunctionRecord updatePNFD(
			@RequestBody @Valid PhysicalNetworkFunctionRecord pRecord,
			@PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {
		NetworkServiceRecord nsd = null;
		try {
			nsd = networkServiceRecordManagement.query(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}

		PhysicalNetworkFunctionRecord pnfDescriptor = findPNFD(nsd.getPnfr(),
				id_pnf);
		pnfDescriptor = pRecord;
		nsd.getPnfr().add(pnfDescriptor);
		networkServiceRecordManagement.update(nsd, id);
		return pnfDescriptor;
	}

	// TODO The Rest of the classes

	private PhysicalNetworkFunctionRecord findPNFD(
			List<PhysicalNetworkFunctionRecord> listPNFR, String id_pnf) {
		PhysicalNetworkFunctionRecord pNetworkFunctionDescriptor = null;
		for (PhysicalNetworkFunctionRecord pRecord : listPNFR) {
			if (pRecord.getId().equals(id_pnf)) {
				pNetworkFunctionDescriptor = pRecord;
			}
		}
		if (pNetworkFunctionDescriptor == null) {
			throw new PNFDNotFoundException(id_pnf);
		}
		return pNetworkFunctionDescriptor;
	}

	private VNFDependency findVNFD(List<VNFDependency> vnf_dependency,
			String id_vnfd) {
		VNFDependency vDependency = null;
		for (VNFDependency vnfDependency : vnf_dependency) {
			if (vnfDependency.getId().equals(id_vnfd)) {
				vDependency = vnfDependency;
			}
		}
		if (vDependency == null) {

			throw new VNFDependencyNotFoundException(id_vnfd);
		}
		return vDependency;
	}

	private VirtualNetworkFunctionRecord findVNF(
			List<VirtualNetworkFunctionRecord> listVNF, String id_vnf) {

		VirtualNetworkFunctionRecord nRecord = null;
		for (VirtualNetworkFunctionRecord vnfRecord : listVNF) {
			if (vnfRecord.getId().equals(id_vnf)) {
				nRecord = vnfRecord;
			}
		}
		if (nRecord == null) {
			throw new VNFDNotFoundException(id_vnf);
		}
		return nRecord;
	}
}
