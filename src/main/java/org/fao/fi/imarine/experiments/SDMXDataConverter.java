package org.fao.fi.imarine.experiments;

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
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
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
		return "This algorithm is for testing Statistical Manager and Rscripts." +
			   "The business logic just parses a SDMX GenericData file, and gives it as CSV";
	}

	@Override
	protected void setInputParameters() {
		addStringInput("InputData", "Input file in SDMX-ML GenericData format", null);
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
		AnalysisLogger.getLogger().debug("SDMX parser algorithm -> Input Parameters: "+inputParameters);
		
		HashMap<String,String> codeInjection = null;
		boolean scriptMustReturnAFile = true;
		boolean uploadScriptOnTheInfrastructureWorkspace = false; //the Statistical Manager service will manage the upload
		
		AnalysisLogger.getLogger().debug("SDMX parser algorithm  -> Executing the script ");
		status = 10;
		scriptManager.executeRScript(
				config, scriptName,
				inputData, inputParameters,
				defaultInputFileInTheScript, defaultOutputFileInTheScript,
				codeInjection, scriptMustReturnAFile, uploadScriptOnTheInfrastructureWorkspace);
		
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
