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
 * Spatial Reallocation simplified algorithm
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
 *
 */
public class SpatialReallocationSimplifiedAlgorithm extends StandardLocalExternalAlgorithm{

	RScriptsManager scriptManager;
	String outputFile;

	public enum Intersections{		
		FAO_AREAS_x_EEZ_HIGHSEAS,
		GRID_5DEG_x_EEZ_HIGHSEAS		
	}
	
	@Override
	public void init() throws Exception {
		AnalysisLogger.getLogger().debug("Initialization");
	}
	
	@Override
	public String getDescription() {
		return "The Spatial Reallocaton algorithm allows to estimate statistics for other areas " +
				"from those where they were reported. The algorithm is based on spatial disaggregation technics " +
				"and provides at now an area-weighted reallocation. This simplified algorithm is specifically targeting users " +
				"from the FAO Fisheries and Aquaculture department, aims to facilitate its execution by doing abstraction of " +
				"the intersections to provide.";
	}
	
	@Override
	protected void setInputParameters() {
		addStringInput("InputData", "Input statistics file, in SDMX-ML GenericData format", null);
		addStringInput("RefAreaField","Field name of the area for which statistics are reported", null);
		addStringInput("StatField","Field name of the statistics to be reallocated", null);
		addEnumerateInput(Intersections.values(), "InputIntersection", "Intersection to use for the reallocation", Intersections.FAO_AREAS_x_EEZ_HIGHSEAS.name());
		inputs.add(new PrimitiveType(Boolean.class.getName(), null, PrimitiveTypes.BOOLEAN, "IncludeCalculations", "Whether the intermediate calculations have to be included in the output", "false"));
	}
	
	@Override
	protected void process() throws Exception {
		
		status = 0;
		
		//instantiate the R script manager
		scriptManager = new RScriptsManager();
		String scriptName = "SpatialReallocationSimplifiedAlgorithm.R";
		String defaultInputFileInTheScript = "statistics.xml";
		String defaultOutputFileInTheScript = "spread_statistics.csv";
		
		//inputs
		String inputData = config.getParam("InputData");
		
		//configuring
		AnalysisLogger.getLogger().debug("Spatial Reallocation -> Config path "+config.getConfigPath()+" Persistence path: "+config.getPersistencePath());
		
		//input parameters: represent the context of the script. Values will be assigned in the R environment.
		LinkedHashMap<String,String> inputParameters = new LinkedHashMap<String, String>();
		inputParameters.put("refAreaField", "\""+config.getParam("RefAreaField")+"\"");
		inputParameters.put("statField", "\""+config.getParam("StatField")+"\"");
		inputParameters.put("inputIntersection", "\""+config.getParam("InputIntersection")+"\"");
		inputParameters.put("includeCalculations", config.getParam("IncludeCalculations").toUpperCase());
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
