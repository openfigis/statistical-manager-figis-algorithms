#this is an R script for testing the Statistical Manager
#the R script reads a SDMX-ML dataset and writes it to a csv
inputFile <- "statistics.xml"
outputFile <- "statistics.csv"

#package needs
require(rsdmx)

#business logic
print(Sys.time())
print(paste("InputFile = ", inputFile, sep=""))
print(paste("OutputFile = ", outputFile, sep=""))
sdmx <- readSDMX(inputFile, isURL = FALSE) # here isURL = FALSE because SM seems to download inputFile from URL
statistics <- as.data.frame(sdmx)
if(RemoveNaObs){
	#default statField value
	statField <- "obsValue"
	
	#in case of SDMXCompactData
	if("OBS_VALUE" %in% colnames(statistics)){
		statField <- "OBS_VALUE"
	}
	
	statistics <- statistics[complete.cases(statistics[,statField]),]
	print(nrow(statistics))
}
write.table(statistics, outputFile, row.names = FALSE, col.names = TRUE, sep=",", dec=".")
print(Sys.time())