package org.fao.fi.imarine.experiments;

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
 * SDMXDataConverter Test class
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
 *
 */
public class SDMXDataConverterTest {

	ComputationalAgent transducer = null;
	AlgorithmConfiguration config = null;
	
	@Before
	public void setup() throws Exception{
		config =  new AlgorithmConfiguration();
		config.setConfigPath("./cfg/");
		config.setParam("InputData", "http://data.fao.org/sdmx/repository/data/CAPTURE/..HER/FAO/?startPeriod=1990&endPeriod=2010");
		config.setParam("RemoveNaObs", "true");
		config.setAgent("FIGIS_SDMX_DATA_CONVERTER");
		
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
			Assert.assertEquals("FAO_MAJOR_AREA", unquote(dataHeader[0]));
			Assert.assertEquals("UN_COUNTRY", unquote(dataHeader[1]));
			Assert.assertEquals("SPECIES", unquote(dataHeader[2]));
			Assert.assertEquals("obsTime", unquote(dataHeader[3]));
			Assert.assertEquals("obsValue", unquote(dataHeader[4]));
			
			int rowNb = -1;
			while (dataRow != null) {
				dataRow = CSVFile.readLine();
				rowNb++;
			}
			Assert.assertEquals(920, rowNb);

			CSVFile.close();
		}catch(Exception e){
			throw new Exception("Failed to read CSV file");
		}
	}
	
	
	public static String unquote(String str) {
		int length = str == null ? -1 : str.length();
		if (str == null || length == 0)
			return str;
		if (length > 1 && str.charAt(0) == '\"'
				&& str.charAt(length - 1) == '\"') {
			str = str.substring(1, length - 1);
		}
		return str;
	}
	
}
