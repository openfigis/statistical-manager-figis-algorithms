package org.fao.fi.imarine.spread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import junit.framework.Assert;

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
public class SpatialReallocationGenericAlgorithmTest {

	ComputationalAgent transducer = null;
	AlgorithmConfiguration config = null;
	
	@Before
	public void setup() throws Exception{
		config =  new AlgorithmConfiguration();
		config.setConfigPath("./cfg/");
		config.setAgent("FIGIS_SPATIAL_REALLOCATION_GENERIC");
		
		config.setParam("InputData", "http://data.fao.org/sdmx/repository/data/CAPTURE/..HER/FAO/?startPeriod=1990&endPeriod=2010");
		config.setParam("InputIntersection", "http://www.fao.org/figis/geoserver/GeoRelationship/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=GeoRelationship:FAO_AREAS_x_EEZ_HIGHSEAS");
		config.setParam("RefAreaField", "FAO_MAJOR_AREA");
		config.setParam("IntersectionAreaField", "FAO_AREAS");
		config.setParam("StatField", "obsValue");
		config.setParam("SurfaceField", "INT_AREA");
		config.setParam("AggregateField", "EEZ_HIGHSEAS");
		
		List<ComputationalAgent> trans = TransducerersFactory.getTransducerers(config);
		transducer = trans.get(0);
		transducer.init();
	}
	
	@Test
	public void testProcess() throws Exception{	
		Regressor.process(transducer);
		StatisticalType st = transducer.getOutput();
		
		File csvOutput = (File) ((PrimitiveType) st).getContent();
		try {
			BufferedReader CSVFile = new BufferedReader(new FileReader(csvOutput));
			String dataRow = CSVFile.readLine();
			String[] dataHeader = dataRow.split(",");
			Assert.assertEquals("EEZ_HIGHSEAS", SpreadUtils.unquote(dataHeader[0]));
			Assert.assertEquals("UN_COUNTRY", SpreadUtils.unquote(dataHeader[1]));
			Assert.assertEquals("SPECIES", SpreadUtils.unquote(dataHeader[2]));
			Assert.assertEquals("obsTime", SpreadUtils.unquote(dataHeader[3]));
			Assert.assertEquals("spreadValue", SpreadUtils.unquote(dataHeader[4]));
			
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
