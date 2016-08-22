package org.fao.fi.dataanalysis.spread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import junit.framework.Assert;

import org.fao.fi.dataanalysis.spread.SpreadUtils;
import org.gcube.dataanalysis.ecoengine.configuration.AlgorithmConfiguration;
import org.gcube.dataanalysis.ecoengine.datatypes.PrimitiveType;
import org.gcube.dataanalysis.ecoengine.datatypes.StatisticalType;
import org.gcube.dataanalysis.ecoengine.interfaces.ComputationalAgent;
import org.gcube.dataanalysis.ecoengine.processing.factories.TransducerersFactory;
import org.gcube.dataanalysis.ecoengine.test.regression.Regressor;
import org.junit.Before;
import org.junit.Test;

/**
 * SpatialDataReallocation Test class
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
 *
 */
public class SpatialReallocationSimplifiedAlgorithmTest {

	ComputationalAgent transducer1 = null;
	ComputationalAgent transducer2 = null;
	AlgorithmConfiguration config1 = null;
	AlgorithmConfiguration config2 = null;
	
	@Before
	public void setup() throws Exception{
		
		//test data
		String CFG_PATH = "./cfg/";
		String ALGORITHM_ID = "FIGIS_SPATIAL_REALLOCATION_SIMPLIFIED";
		String INPUT_DATA = "http://data.fao.org/sdmx/repository/data/CAPTURE/..HER/FAO/?startPeriod=1990&endPeriod=2010";
		String REF_AREA_FIELD = "FAO_MAJOR_AREA";
		String STAT_FIELD = "obsValue";
		String INPUT_INTERSECTION = "FAO_AREAS_x_EEZ_HIGHSEAS";
		
		//config 1 (with aggregation)
		config1 = new AlgorithmConfiguration();
		config1.setConfigPath(CFG_PATH);
		config1.setPersistencePath(CFG_PATH);
		config1.setAgent(ALGORITHM_ID);
		config1.setParam("InputData", INPUT_DATA);
		config1.setParam("RefAreaField", REF_AREA_FIELD);
		config1.setParam("StatField", STAT_FIELD);
		config1.setParam("InputIntersection", INPUT_INTERSECTION);
		config1.setParam("IncludeCalculations", "true");
		
		List<ComputationalAgent> trans1 = TransducerersFactory.getTransducerers(config1);
		transducer1 = trans1.get(0);
		transducer1.init();
		
		//config 1 (with aggregation)
		config2 = new AlgorithmConfiguration();
		config2.setConfigPath(CFG_PATH);
		config2.setPersistencePath(CFG_PATH);
		config2.setAgent(ALGORITHM_ID);
		config2.setParam("InputData", INPUT_DATA);
		config2.setParam("RefAreaField", REF_AREA_FIELD);
		config2.setParam("StatField", STAT_FIELD);
		config2.setParam("InputIntersection", INPUT_INTERSECTION);
		config2.setParam("IncludeCalculations", "false");
		
		List<ComputationalAgent> trans2 = TransducerersFactory.getTransducerers(config2);
		transducer2 = trans2.get(0);
		transducer2.init();
	}

	@Test
	public void testRawProcess() throws Exception{	
		Regressor.process(transducer1);
		StatisticalType st = transducer1.getOutput();
		
		File csvOutput = (File) ((PrimitiveType) st).getContent();
		try {
			BufferedReader CSVFile = new BufferedReader(new FileReader(csvOutput));
			String dataRow = CSVFile.readLine();
			
			int rowNb = -1;
			while (dataRow != null) {
				dataRow = CSVFile.readLine();
				rowNb++;
			}
			System.out.println(rowNb);
			//Assert.assertEquals(16236, rowNb);

			CSVFile.close();
		}catch(Exception e){
			throw new Exception("Failed to read CSV file");
		}
	}
	
	@Test
	public void testAggregateProcess() throws Exception{	
		Regressor.process(transducer2);
		StatisticalType st = transducer2.getOutput();
		
		File csvOutput = (File) ((PrimitiveType) st).getContent();
		try {
			BufferedReader CSVFile = new BufferedReader(new FileReader(csvOutput));
			String dataRow = CSVFile.readLine();
			String[] dataHeader = dataRow.split(",");
			Assert.assertEquals("EEZ_HIGHSEAS", SpreadUtils.unquote(dataHeader[0]));
			Assert.assertEquals("UN_COUNTRY", SpreadUtils.unquote(dataHeader[1]));
			Assert.assertEquals("SPECIES", SpreadUtils.unquote(dataHeader[2]));
			Assert.assertEquals("obsTime", SpreadUtils.unquote(dataHeader[3]));
			Assert.assertEquals("UNIT", SpreadUtils.unquote(dataHeader[4]));
			Assert.assertEquals("spreadValue", SpreadUtils.unquote(dataHeader[5]));
			
			int rowNb = -1;
			while (dataRow != null) {
				dataRow = CSVFile.readLine();
				rowNb++;
			}
			Assert.assertEquals(16236, rowNb);

			CSVFile.close();
		}catch(Exception e){
			throw new Exception("Failed to read CSV file");
		}	
	}
	
}
