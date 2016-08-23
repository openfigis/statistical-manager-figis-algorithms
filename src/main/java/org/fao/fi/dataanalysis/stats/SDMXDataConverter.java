package org.fao.fi.dataanalysis.stats;

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
 * SDMXDataConverter
 * 
 * A basic algorithm used for testing the gCube Statistical Manager framework
 * and integration of R scripts. The R script only proceeds only to a data conversion
 * from SDMX-ML document (dataset) to CSV, by means of the rsdmx R data abstraction library.
 * 
 * @author Emmanuel Blondel
 *
 */
public class SDMXDataConverter extends StandardLocalExternalAlgorithm{

	RScriptsManager scriptManager;
	String outputFile;
	
	@Override
	public void init() throws Exception {
		AnalysisLogger.getLogger().debug("Initialization");
	}
	
	@Override
	public String getDescription() {
		return "This tool allows to convert easily a SDMX dataset into CSV, by calling"
				+ "the rsdmx package for R";
	}

	@Override
	protected void setInputParameters() {
		addStringInput("InputData", "Input file in SDMX-ML GenericData format", null);
		inputs.add(new PrimitiveType(Boolean.class.getName(), null, PrimitiveTypes.BOOLEAN, "RemoveNaObs", "If NA observations have to be removed", "false"));
	}
	
	@Override
	protected void process() throws Exception {
		
		status = 0;
		
		//instantiate the R script manager
		scriptManager = new RScriptsManager();
		String scriptName = "SDMXDataConverter.R";
		String defaultInputFileInTheScript = "statistics.xml";
		String defaultOutputFileInTheScript = "statistics.csv";
		
		//inputs
		String inputData = config.getParam("InputData");
		
		
		//configuring
		AnalysisLogger.getLogger().debug("SDMX parser algorithm -> Config path "+config.getConfigPath()+" Persistence path: "+config.getPersistencePath());
		
		LinkedHashMap<String,String> inputParameters = new LinkedHashMap<String, String>();
		inputParameters.put("RemoveNaObs", config.getParam("RemoveNaObs").toUpperCase());
		AnalysisLogger.getLogger().debug("SDMX parser algorithm -> Input Parameters: "+inputParameters);
		
		HashMap<String,String> codeInjection = null;
		boolean scriptMustReturnAFile = true;
		boolean uploadScriptOnTheInfrastructureWorkspace = false;
		boolean deleteTempFiles = true;
		
		AnalysisLogger.getLogger().debug("SDMX parser algorithm  -> Executing the script ");
		status = 10;
		scriptManager.executeRScript(
				config, scriptName, inputData, inputParameters,
				defaultInputFileInTheScript, defaultOutputFileInTheScript,
				codeInjection, scriptMustReturnAFile, uploadScriptOnTheInfrastructureWorkspace, deleteTempFiles, config.getConfigPath());

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
