package org.fao.fi.imarine.spread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gcube.contentmanagement.lexicalmatcher.utils.AnalysisLogger;
import org.gcube.dataanalysis.ecoengine.datatypes.ColumnType;
import org.gcube.dataanalysis.ecoengine.datatypes.DatabaseType;
import org.gcube.dataanalysis.ecoengine.datatypes.InputTable;
import org.gcube.dataanalysis.ecoengine.datatypes.OutputTable;
import org.gcube.dataanalysis.ecoengine.datatypes.PrimitiveType;
import org.gcube.dataanalysis.ecoengine.datatypes.StatisticalType;
import org.gcube.dataanalysis.ecoengine.datatypes.enumtypes.PrimitiveTypes;
import org.gcube.dataanalysis.ecoengine.datatypes.enumtypes.TableTemplates;
import org.gcube.dataanalysis.ecoengine.interfaces.StandardLocalExternalAlgorithm;
import org.gcube.dataanalysis.ecoengine.utils.DatabaseFactory;
import org.gcube.dataanalysis.ecoengine.utils.DatabaseUtils;
import org.gcube.dataanalysis.executor.util.RScriptsManager;
import org.hibernate.SessionFactory;

/**
 * Spatial Reallocation simplified algorithm
 * tailored for the iMarine Tabular Data Manager
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org>
 *
 */
public class SpatialReallocationSimplifiedTableAlgorithm extends StandardLocalExternalAlgorithm {
	
	SessionFactory dbconnection;
	
	RScriptsManager scriptManager;
	String outputFile;
	
	String destinationTableLabel;
	String destinationTable;

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
				"from those where they were reported. The algorithm is based on spatial disaggregation " +
				"technics and provides at now an area-weighted reallocation. This simplified algorithm " +
				"is specifically targeting users from the FAO Fisheries and Aquaculture department, aims " +
				"to facilitate its execution by doing abstraction of the intersections to provide.";
	}
	
	@Override
	protected void setInputParameters() {		
		List<TableTemplates> template= new ArrayList<TableTemplates>();
		template.add(TableTemplates.GENERIC);
		
		//input table
		InputTable table = new InputTable(template, "Dataset","An input dataset having at least a " +
													"numerical column and a reference column corresponding " +
													"to a geographic dimension");
		inputs.add(table);
		
		//reference column
		ColumnType refColumn = new ColumnType("Dataset", "Georef", "Field name of the area for which statistics are reported", null, false);
		inputs.add(refColumn);
		
		//stat column
		ColumnType statColumn = new ColumnType("Dataset", "Statistic", "Field name of the statistics to be reallocated", null, false);
		inputs.add(statColumn);
		
		//intersection
		PrimitiveType intersection = new PrimitiveType(Enum.class.getName(), Intersections.values(), PrimitiveTypes.ENUMERATED,
													   "Intersection", "Intersection to use for the reallocation", null, false);
		inputs.add(intersection);

		//include calculations
		PrimitiveType includeColumn = new PrimitiveType(Boolean.class.getName(), null, PrimitiveTypes.BOOLEAN, "IncludeCalculations",
														"Whether the intermediate calculations have to be included in the output", "false", false);
		inputs.add(includeColumn);
		
		//output
		PrimitiveType outputTableLabel = new PrimitiveType(String.class.getName(), null, PrimitiveTypes.STRING, "TableLabel", "Name of the table which will contain the SPREAD output", "SPREAD result", false);
		inputs.add(outputTableLabel);
		
		DatabaseType.addDefaultDBPars(inputs);
	}
	
	@Override
	protected void process() throws Exception {

		//configuration
		String inputTable = config.getParam("Dataset");
		System.out.println("Origin Table: "+inputTable);
		
		destinationTableLabel = config.getParam("TableLabel");
		System.out.println("Destination Table Label: "+destinationTableLabel);
		
		destinationTable = "spread_"+UUID.randomUUID().toString().replaceAll("-", "_");
		AnalysisLogger.getLogger().debug(destinationTable);
		
		//db connection
		config.setParam("DatabaseDriver", "org.postgresql.Driver");
		dbconnection = DatabaseUtils.initDBSession(config);
		
		//get data from db
		AnalysisLogger.getLogger().debug("Copying table to local csv file...");
		
		String inputFile = inputTable + ".csv";
		AnalysisLogger.getLogger().debug(inputFile);
		SpreadUtils.createLocalFileFromRemoteTable(
			inputFile, inputTable, ",", true,
			config.getDatabaseUserName(),
			config.getDatabasePassword(),
			config.getDatabaseURL());
		
		status = 25;
		
		//instantiate the R script manager
		scriptManager = new RScriptsManager();
		String scriptName = "SpatialReallocationSimplifiedTableAlgorithm.R";
		String defaultInputFileInTheScript = "spread_input.csv";
		String defaultOutputFileInTheScript = "spread_output.csv";
		int extension = defaultOutputFileInTheScript.lastIndexOf(".");
		AnalysisLogger.getLogger().debug(extension);
		AnalysisLogger.getLogger().debug(defaultOutputFileInTheScript.substring(0, extension));
				
		//input parameters: represent the context of the script (values will be assigned in the R environment)
		LinkedHashMap<String,String> inputParameters = new LinkedHashMap<String, String>();
		inputParameters.put("refAreaField", "\""+config.getParam("Georef")+"\"");
		inputParameters.put("statField", "\""+config.getParam("Statistic")+"\"");
		inputParameters.put("inputIntersection", "\""+config.getParam("Intersection")+"\"");
		inputParameters.put("includeCalculations", config.getParam("IncludeCalculations").toUpperCase());
		AnalysisLogger.getLogger().debug("SPREAD -> Input Parameters: "+inputParameters);
				
		HashMap<String,String> codeInjection = null;
		boolean scriptMustReturnAFile = true;
		boolean uploadScriptOnTheInfrastructureWorkspace = false; //the Statistical Manager service will manage the upload
				
		AnalysisLogger.getLogger().debug("Spatial Reallocation -> Executing the script ");
		status = 10;
		scriptManager.executeRScript(
				config, scriptName,
				inputFile, inputParameters,
				defaultInputFileInTheScript, defaultOutputFileInTheScript,
				codeInjection, scriptMustReturnAFile, uploadScriptOnTheInfrastructureWorkspace);
		
		// assign the file path to an output variable for the SM
		outputFile = scriptManager.currentOutputFileName;
		AnalysisLogger.getLogger().debug("Output File is "+outputFile);
		status = 75;
		
		// write R algorithm output to SM db
		AnalysisLogger.getLogger().debug("Writing SPREAD output...");
		
		//prepare output table schema & create table
		String tableSchema = null;
		
		//inherit fieldType from columns that are kept
		Map<String,String> inputFields = SpreadUtils.getSchemaDescription(inputTable, dbconnection);
		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String[] headers = br.readLine().split(",");
		br.close();
		for(String header : headers){
			String fieldSchemaElement = SpreadUtils.unquote(header.toLowerCase());
			if(inputFields.containsKey(fieldSchemaElement)){
				fieldSchemaElement += " "+inputFields.get(fieldSchemaElement);
			}else{
				if(fieldSchemaElement.matches("int_area") ||
				   fieldSchemaElement.matches("w") ||
				   fieldSchemaElement.matches("wsum") ||
				   fieldSchemaElement.matches("spread")
				   ){
					fieldSchemaElement += " double precision";
				}else{
					fieldSchemaElement += " character varying";
				}
			}
			
			//add to table schema
			if(tableSchema == null){
				tableSchema = fieldSchemaElement;
			}else{
				tableSchema += ", "+fieldSchemaElement;
			}
		}
		tableSchema = "("+tableSchema+")";
		String createTableStatement = String.format("CREATE TABLE %s"+tableSchema, destinationTable);
		AnalysisLogger.getLogger().debug(createTableStatement);
		DatabaseFactory.executeSQLUpdate(String.format(createTableStatement, destinationTable), dbconnection);
		
		//feed table
		DatabaseUtils.createRemoteTableFromFile(
				outputFile, destinationTable, ",", true,
				config.getDatabaseUserName(), config.getDatabasePassword(), config.getDatabaseURL());
		status = 100;	
		
	}

	@Override
	public StatisticalType getOutput() {
		List<TableTemplates> template = new ArrayList<TableTemplates>();
		template.add(TableTemplates.GENERIC);
		OutputTable output = new OutputTable(template, destinationTableLabel, destinationTable, "Output");
		return output;
	}
	
	@Override
	public void shutdown() {
		if (dbconnection != null)
			dbconnection.close();
		
		if (scriptManager!=null)
			scriptManager.stop();
		
		System.gc();
		AnalysisLogger.getLogger().debug("Shutdown");
	}
	
}
