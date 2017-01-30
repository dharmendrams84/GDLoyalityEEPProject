package com.gdyn.orpos.exportfile;

import org.apache.log4j.Logger;

import oracle.retail.stores.exportfile.EntityMappingObjectFactory;
import oracle.retail.stores.exportfile.EntityMappingObjectFactoryIfc;
import oracle.retail.stores.exportfile.mapper.MappingResultIfc;

import com.gdyn.orpos.exportfile.rtlog.GDYNRTLogMappingResult;

public class GDYNEntityMappingObjectFactory extends EntityMappingObjectFactory
		implements EntityMappingObjectFactoryIfc {

	protected static final Logger logger = Logger
			.getLogger(GDYNEntityMappingObjectFactory.class);

	/**
	 * This method gets an instance of MappingResultIfc
	 */
	public MappingResultIfc getMappingResultInstance() {
		return new GDYNRTLogMappingResult();
	}
}
