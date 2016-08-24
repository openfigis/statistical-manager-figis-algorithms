# Spatial Data Reallocation algorithm for FIGIS
inputFile <- "statistics.xml"
outputFile <- "spread_statistics.csv"

#package needs
#------------
require(rsdmx)
require(RFigisGeo)

#business logic
#-------------
cat(paste0(as.character(Sys.time()),"\n"))

#read stats
cat("Reading SDMX stat data ...\n")
sdmx <- readSDMX(inputFile, isURL = FALSE) # here isURL = FALSE because SM seems to download inputFile from URL
statistics <- as.data.frame(sdmx)

#read intersections
cat("Reading intersection...\n")
intersections <- readWFS(inputIntersection)
if(class(intersections) == "SpatialPolygonsDataFrame") intersections <- intersections@data

#aggregation
aggregate <- NULL
if(nchar(aggregateField) > 1) aggregate <- aggregateField

#reallocation
cat("Reallocating statistical data...\n")
result <- reallocate(
			x = statistics,
			y = intersections,
			area.x = refAreaField,
			area.y = intersectionAreaField,
			by.x = NULL,
			by.y = NULL,
			data = statField,
			warea = surfaceField,
			wprob = NULL,
			aggregates = aggregate
		)

write.table(result, outputFile, row.names = FALSE, col.names = TRUE, sep=",", dec=".")
cat(paste0(as.character(Sys.time()),"\n"))
