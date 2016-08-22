package org.fao.fi.dataanalysis.spread;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.gcube.contentmanagement.lexicalmatcher.utils.AnalysisLogger;
import org.gcube.dataanalysis.ecoengine.datatypes.PrimitiveType;
import org.gcube.dataanalysis.ecoengine.datatypes.StatisticalType;
import org.gcube.dataanalysis.ecoengine.datatypes.enumtypes.PrimitiveTypes;
import org.gcube.dataanalysis.ecoengine.interfaces.StandardLocalExternalAlgorithm;
import org.gcube.dataanalysis.executor.util.RScriptsManager;

/**
 * Spatial Reallocation algorithm
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
 *
 */
public class SpatialReallocationGenericAlgorithm extends StandardLocalExternalAlgorithm{

	RScriptsManager scriptManager;
	String outputFile;
	
	@Override
	public void init() throws Exception {
		AnalysisLogger.getLogger().debug("Initialization");
	}
	
	@Override
	public String getDescription() {
		return "The Spatial Reallocaton algorithm allows to estimate statistics for other areas " +
				"from those where they were reported. The algorithm is based on spatial disaggregation technics " +
				"and provides at now an area-weighted reallocation.";
	}
	
	@Override
	protected void setInputParameters() {
		addStringInput("InputData", "Input statistics file, in SDMX-ML GenericData format", null);
		addStringInput("InputIntersection", "Input intersection, in GML format", null);
		addStringInput("RefAreaField","Field name of the area for which statistics are reported", null);
		addStringInput("IntersectionAreaField","Equivalent Field name of the area in the intersection data", null);
		addStringInput("SurfaceField", "Field name of the surface in the intersection data", null);
		addStringInput("StatField","Field name of the statistics to be reallocated", null);
		addStringInput("AggregateField","Field name of the target area for which estimated statistics will be aggregated." +
										"If not specified, disaggregated data with pre-calculations will be returned", null);
	}
	
	@Override
	protected void process() throws Exception {
		
		status = 0;
		
		//instantiate the R script manager
		scriptManager = new RScriptsManager();
		String scriptName = "SpatialReallocationGenericAlgorithm.R";
		String defaultInputFileInTheScript = "statistics.xml";
		String defaultOutputFileInTheScript = "spread_statistics.csv";
		
		//inputs
		String inputData = config.getParam("InputData");
		
		//configuring
		AnalysisLogger.getLogger().debug("Spatial Reallocation -> Config path "+config.getConfigPath()+" Persistence path: "+config.getPersistencePath());
		
		//input parameters: represent the context of the script. Values will be assigned in the R environment.
		LinkedHashMap<String,String> inputParameters = new LinkedHashMap<String, String>();
		inputParameters.put("inputIntersection", "\""+config.getParam("InputIntersection")+"\"");
		inputParameters.put("refAreaField", "\""+config.getParam("RefAreaField")+"\"");
		inputParameters.put("intersectionAreaField", "\""+config.getParam("IntersectionAreaField")+"\"");
		inputParameters.put("surfaceField", "\""+config.getParam("SurfaceField")+"\"");
		inputParameters.put("statField", "\""+config.getParam("StatField")+"\"");
		inputParameters.put("aggregateField", "\""+config.getParam("AggregateField")+"\"");
		AnalysisLogger.getLogger().debug("Spatial Reallocation -> Input Parameters: "+inputParameters);
		
		HashMap<String,String> codeInjection = null;
		boolean scriptMustReturnAFile = true;
		
		AnalysisLogger.getLogger().debug("Spatial Reallocation -> Executing the script ");
		status = 10;
		scriptManager.executeRScript(
				config, scriptName,
				inputData, inputParameters,
				defaultInputFileInTheScript, defaultOutputFileInTheScript,
				codeInjection, scriptMustReturnAFile, true, false, config.getPersistencePath());
		
		// assign the file path to an output variable for the SM
		outputFile = scriptManager.currentOutputFileName;
		AnalysisLogger.getLogger().debug("Output File is "+outputFile);
		status = 100;
		
	}
	
	@Override
	public StatisticalType getOutput() {
		PrimitiveType output = new PrimitiveType(
				File.class.getName(), new File(outputFile), PrimitiveTypes.FILE, "OutputFile",
				"Output file in CSV format.");
		return output;
	}

	@Override
	public void shutdown() {	
		//in the case of forced shutdown, stop the R process
		if (scriptManager!=null)
			scriptManager.stop();
		System.gc();
		
		AnalysisLogger.getLogger().debug("Shutdown");
	}

}
