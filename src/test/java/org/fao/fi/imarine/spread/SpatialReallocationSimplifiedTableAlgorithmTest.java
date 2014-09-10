package org.fao.fi.imarine.spread;

import java.util.List;

import org.gcube.dataanalysis.ecoengine.configuration.AlgorithmConfiguration;
import org.gcube.dataanalysis.ecoengine.datatypes.InputTable;
import org.gcube.dataanalysis.ecoengine.datatypes.StatisticalType;
import org.gcube.dataanalysis.ecoengine.interfaces.ComputationalAgent;
import org.gcube.dataanalysis.ecoengine.processing.factories.TransducerersFactory;
import org.gcube.dataanalysis.ecoengine.test.regression.Regressor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SpatialDataReallocation Test class
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
 *
 */
public class SpatialReallocationSimplifiedTableAlgorithmTest {

	
	ComputationalAgent transducer = null;
	AlgorithmConfiguration config = null;
	
	@Before
	public void setup() throws Exception{
		config = new AlgorithmConfiguration();
		config.setConfigPath("./cfg/");
		config.setAgent("FIGIS_SPATIAL_REALLOCATION_SIMPLIFIED_TABLE");
		
		//test configs
		String dbUser = "user";
		String dbPwd = "pwd";
		String dbURL = "url";
		String intable = "generic_id407c7aa2_fcfd_46bd_abb8_c0aee57a01ea";
		String serviceUser = null;
		
		//database config
		config.setParam("DatabaseUserName", dbUser);
		config.setParam("DatabasePassword", dbPwd);
		config.setParam("DatabaseURL", dbURL);
		
		//service config
		if(serviceUser != null){
			config.setParam("ServiceUserName", serviceUser);
		}
		
		//algorithm config
		config.setParam("Dataset", intable);
		config.setParam("Georef", "fao_major_area");
		config.setParam("Statistic", "obsvalue");
		config.setParam("Intersection", "FAO_AREAS_x_EEZ_HIGHSEAS");
		config.setParam("IncludeCalculations", "true");
		config.setParam("TableLabel", "SPREAD output");
	
		List<ComputationalAgent> trans = TransducerersFactory.getTransducerers(config);
		transducer = trans.get(0);
		transducer.init();
		
	}
	
	@Test
	public void testProcess() throws Exception{	
		Regressor.process(transducer);
		StatisticalType st = transducer.getOutput();
		
		Assert.assertNotNull((InputTable) st); 
		Assert.assertNotNull((((InputTable) st).getTableName())); 

	}
	
}
