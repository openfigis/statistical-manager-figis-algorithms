# Spatial Data Reallocation algorithm for FIGIS
inputFile <- "spread_input.csv"
outputFile <- "spread_output.csv"

#package needs
require(RFigisGeo)

#business logic
#-------------
cat(paste0(as.character(Sys.time()),"\n"))

#read stats
cat("Reading statistical data ...\n")
statistics <- read.table(inputFile, sep = ",", h = TRUE, quote = "\"")

#read intersections
cat("Reading intersection...\n")
intersectionURL <- paste("http://www.fao.org/figis/geoserver/GeoRelationship/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=GeoRelationship:", inputIntersection, sep = "")
intersections <- readWFS(intersectionURL)
targetAreaField <- unlist(strsplit(inputIntersection,"_x_"))[1]
aggregateField <- NULL
if(!includeCalculations){
	aggregateField <- unlist(strsplit(inputIntersection,"_x_"))[2]
}

#transform data (reallocate requires characters)
isNumericAreaRef <- is.numeric(statistics[,refAreaField])
if(isNumericAreaRef){
	statistics[,refAreaField] <- as.character(statistics[,refAreaField])
}

#reallocation
cat("Reallocating statistical data...\n")
result <- reallocate(
		x = statistics,
		y = intersections,
		area.x = refAreaField,
		area.y = targetAreaField,
		by.x = NULL,
		by.y = NULL,
		data = statField,
		warea = "INT_AREA",
		wprob = NULL,
		aggregates = aggregateField
)

if(isNumericAreaRef & includeCalculations){
	result[,refAreaField] <- as.character(result[,refAreaField])
}

write.table(result, outputFile, row.names = FALSE, col.names = TRUE, sep=",", dec=".")
cat(paste0(as.character(Sys.time()),"\n"))