package org.fao.fi.dataanalysis.spread;

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

	static final String SERVICE_SCOPE = "/gcube/devsec/devVRE";
	static final String SERVICE_USERNAME = "emmanuel.blondel";
	
	ComputationalAgent transducer1 = null;
	ComputationalAgent transducer2 = null;
	AlgorithmConfiguration config1 = null;
	AlgorithmConfiguration config2 = null;
	
	@Before
	public void setup() throws Exception{
		
		//test data
		String CFG_PATH = "./cfg/";
		String ALGORITHM_ID = "FIGIS_SPATIAL_REALLOCATION_SIMPLIFIED_TABLE";
		String GEO_REF = "cl_fao_major_area_new_code";
		String STAT = "obsvalue";
		String INTERSECTION = "FAO_AREAS_x_EEZ_HIGHSEAS";
		
		String dbURL = "jdbc:postgresql://host/db";
		String dbUser = "user";
		String dbPwd = "pwd";
		
		String intable = "generic_ida5c07152_ea15_48e5_b0cf_4cef82c23a50";
		
		//config 1 (without aggregation)
		config1 = new AlgorithmConfiguration();
		config1.setConfigPath(CFG_PATH);
		config1.setPersistencePath(CFG_PATH);
		config1.setAgent(ALGORITHM_ID);
		config1.setParam("DatabaseUserName", dbUser);
		config1.setParam("DatabasePassword", dbPwd);
		config1.setParam("DatabaseURL", dbURL);
		config1.setParam("Dataset", intable);
		config1.setParam("Georef", GEO_REF);
		config1.setParam("Statistic", STAT);
		config1.setParam("Intersection", INTERSECTION);
		config1.setParam("IncludeCalculations", "true");
		config1.setParam("TableLabel", "SPREAD output");
		
		//set the scope and the user
		config1.setGcubeScope(SERVICE_SCOPE);
		config1.setParam("ServiceUserName", SERVICE_USERNAME);
	
		List<ComputationalAgent> trans1 = TransducerersFactory.getTransducerers(config1);
		transducer1 = trans1.get(0);
		transducer1.init();
		
		//config 2 (with aggregation)
		config2 = new AlgorithmConfiguration();
		config2.setConfigPath(CFG_PATH);
		config2.setPersistencePath(CFG_PATH);
		config2.setAgent(ALGORITHM_ID);
		config2.setParam("DatabaseUserName", dbUser);
		config2.setParam("DatabasePassword", dbPwd);
		config2.setParam("DatabaseURL", dbURL);
		config2.setParam("Dataset", intable);
		config2.setParam("Georef", GEO_REF);
		config2.setParam("Statistic", STAT);
		config2.setParam("Intersection", INTERSECTION);
		config2.setParam("IncludeCalculations", "false");
		config2.setParam("TableLabel", "SPREAD output");
		
		//set the scope and the user
		config2.setGcubeScope(SERVICE_SCOPE);
		config2.setParam("ServiceUserName", SERVICE_USERNAME);
	
		List<ComputationalAgent> trans2 = TransducerersFactory.getTransducerers(config2);
		transducer2 = trans2.get(0);
		transducer2.init();
		
	}
	
	@Test
	public void testRawProcess() throws Exception{	
		Regressor.process(transducer1);
		StatisticalType st = transducer1.getOutput();
		
		Assert.assertNotNull((InputTable) st); 
		Assert.assertNotNull((((InputTable) st).getTableName())); 
	}
	
	@Test
	public void testAggregateProcess() throws Exception{	
		Regressor.process(transducer2);
		StatisticalType st = transducer2.getOutput();
		
		Assert.assertNotNull((InputTable) st); 
		Assert.assertNotNull((((InputTable) st).getTableName())); 
	}
	
}
