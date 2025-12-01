# ComputeSimulationAction

# Description
Supports the computation of all placements of a given storm in a cloud-compute run each representing a single simulation in HEC-HMS. This action supports running by event number or by storm name, giving flexibility to the user to support multiple workflows. This action was designed to minimze file transfers to the container, and to streamline the typical action structure of copy, compute, export precip, export peaks, post into a single action that loops over many simulations.
Running by storm reduces the horizontal scale opportunity since all storm placements are done within a single server in sequence, running by event increases horizontal scale but creates issues for aggregating peak values (until the event store is leveraged)

# Implementation Details

# Process Flow
This action downloads all required inputs performs required manipulations (changing the met file coordinates updating the control file, importing the appropriate basin file) internal to the action.
1. get required input attributes
2. download hms model files
3. download hms data files
4. download storms csv file
5. get CC_EVENT_IDENTIFIER and determine if run by storms or run by event
6. get storm associated with storm name (either storm or event)
7. filter storms csv file based on event nubmer or storm name
8. get basin file and control file associated with the next storm in the filtered storms list
9. compute the hms run
10. post dss output
11. extract peak flows for all variables identified in "exported variables" for all durations in "exported durations"
12. post peaks
13. truncate the time window to the 72 hour period of precipitation
14. export precip for the first 72 hours of precipitation
15. post excess precip
16. continue until filtered list is 0. 
17. post any faied events and log compute times.

# Configuration

   ## Environment
   * CC_EVENT_IDENTIFIER -> if this parses to an integer a single event is computed, if this parses to a storm name, that storm is computed for all placements.
   ## Attributes

	### Action
   * storms_catalog -> the path to the storm catalog (from the root of the store identified in the "storms" input datasource)
   * model-name
   * simulation
   * met-name
   * control-name
   * basin-name
   * exported-precip-name -> if extension is .dss a dss file will be generatd if the extension is .p01.tmp.hdf an HEC-RAS formatted hdf file will be produced
   * storm-name
   * exported-peak-paths -> an array of strings each representing a dss pathname from the output dss file from an HEC-HMS simulation that the user wants peak values exported for
   * exported-peak-durations -> an array of integers specifying the number of timesteps to export peaks for (duration frequency curves)
   * peak-data-source-name -> the name of the datasource that contains path keys that match the array of peak durations for where to store the csvs containing peaks.
   * log-data-source-name -> the datasource where log file storage is specified. 

    ### Global

   ## Inputs
   This action is very specific to the FFRD workflows. The majority of the HEC-HMS model with all of its files are downloaded at the start of the action and then basin files and control files are downloaded for each event computed. The met file is updated with each event based on the x and y coordinate of the storm. The inputs are required to be very specific in their names and input data.
    ### Input Data Sources
      * hms
         * control-file
         * met-file
         * simulation-dss
         * grid-file
         * paired-data-file
         * hms-project-file
         * run-file
         * sqlite-file
         * terrain-file
         * basin-file
      * data
         * paired-data-file
         * normalization-file
      * storms
         * default
      * storm-catalog
         * default
      * basinfiles
         * default
      * controlfiles
         * default

   ## Outputs
   The outputs from this action are a dss file containing time series for each event, an exported precip file for each event and, a set of peak csv files for each duration and a log file containing compute time durations of each simulation component.
     ### Output Data Sources
      * simulation-dss
         * default
      * excess-precip
         * default
      * peaks (must match exported-peak-durations)
         * 1
         * 24
         * 48
         * 72
      * logs
         * failed_events
         * time_log
# Configuration Examples
```json
{
    "manifest_name":     "hms-runner-trinity",
    "plugin_definition": "FFRD-HMS-RUNNER-TRINITY",
    "stores": [
        {
            "name": "FFRD",
            "store_type": "S3",
            "profile": "FFRD",
            "params": {
                "root": "model-library/ffrd-trinity"
            }
        }
    ],
    "inputs":{
      "payload_attributes": {
		"scenario": "conformance",
		"outputroot": "simulations",
		"base-hydrology-directory": "hydrology",
		"model-name": "trinity",
		"simulation": "SST",
		"met-name": "SST",
		"control-name": "SST",
		"basin-name":"trinity_sst",
		"exported-precip-name": "exported-precip_trinity.dss"
      },
      "data_sources": []
    },
    "outputs":[],
    "actions": [
        {
            "type": "compute_simulation_all_placements_given_storm",
            "description": "compute all placements storms",
            "attributes": {
                "storms_catalog": "/conformance/storm-catalog/storms/",
                "model-name": "trinity",
                "simulation": "SST",
                "met-name": "SST",
                "control-name": "SST",
                "basin-name":"trinity_sst",
                "exported-precip-name": "exported-precip_trinity.dss",
                "storm-name": "{ENV::CC_EVENT_IDENTIFIER}",
                "exported-peak-paths": [
                    "//wilson-ck_s010/PRECIP-EXCESS//1Hour/RUN:SST/",
                    "//wilson-ck_j010/FLOW//1Hour/RUN:SST/",
                    "//wilson-ck_s010/PRECIP-INC//1Hour/RUN:SST/"
                ],
                "exported-peak-durations": [
                    1,
                    24,
                    48,
                    72
                ],
                "peak-datasource-name": "peaks",
                "log-datasource-name": "logs"
            },
            "stores": [{
            "name": "FFRD",
            "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            "store_type": "S3",
            "profile": "FFRD",
            "params": {
                "root": "model-library/ffrd-trinity"
            }
        }],
            "inputs":[{
                "name": "hms",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "control-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::control-name}.control",
                    "met-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::met-name}.met",
                    "simulation-dss":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::model-name}.dss",
                    "grid-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::model-name}.grid",
                    "paired-data-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::model-name}.pdata",
                    "hms-project-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::model-name}.hms",
                    "run-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::model-name}.run",
                    "sqlite-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::basin-name}.sqlite",
                    "terrain-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::model-name}.terrain",
                    "basin-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/{ATTR::basin-name}.basin"
                },
                "store_name": "FFRD"
            },{
                "name": "hms-data",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "paired-data-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/data/paired_data.dss",
                    "normalization-file":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/data/trinity_72hr_100yr_in.tif"
                },
                "store_name": "FFRD"
            },{
                "name": "storms",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "default":"{ATTR::scenario}/{ATTR::outputroot}/storm-catalog/storms.csv"
                },
                "store_name": "FFRD"
            },{
                "name": "storm-catalog",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "default":"{ATTR::scenario}/storm-catalog/storms/{ENV::CC_EVENT_IDENTIFIER}.dss"
                },
                "store_name": "FFRD"
            },{
                "name": "basinfiles",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "basin-prefix":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}"
                },
                "store_name": "FFRD"
            },{
                "name": "controlfiles",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "control-prefix":"{ATTR::scenario}/{ATTR::base-hydrology-directory}/{ATTR::model-name}/data/controlspecs"
                },
                "store_name": "FFRD"
            }],
            "outputs":[{
                "name": "simulation-dss",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "default":"{ATTR::scenario}/{ATTR::outputroot}/event-data/$<eventnumber>/{ATTR::base-hydrology-directory}/{ATTR::simulation}.dss"
                },
                "store_name": "FFRD"
            },{
                "name": "excess-precip",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "default":"{ATTR::scenario}/{ATTR::outputroot}/event-data/$<eventnumber>/{ATTR::base-hydrology-directory}/{ATTR::exported-precip-name}"
                },
                "store_name": "FFRD"
            },{
                "name": "peaks",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "1":"{ATTR::scenario}/{ATTR::outputroot}/summary-data/{ATTR::base-hydrology-directory}/1h_peaks_{ENV::CC_EVENT_IDENTIFIER}.csv",
                    "24":"{ATTR::scenario}/{ATTR::outputroot}/summary-data/{ATTR::base-hydrology-directory}/24h_peaks_{ENV::CC_EVENT_IDENTIFIER}.csv",
                    "48":"{ATTR::scenario}/{ATTR::outputroot}/summary-data/{ATTR::base-hydrology-directory}/48h_peaks_{ENV::CC_EVENT_IDENTIFIER}.csv",
                    "72":"{ATTR::scenario}/{ATTR::outputroot}/summary-data/{ATTR::base-hydrology-directory}/72h_peaks_{ENV::CC_EVENT_IDENTIFIER}.csv"
                },
                "store_name": "FFRD"
            },{
                "name": "logs",
                "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "paths": {
                    "failed_events":"{ATTR::scenario}/{ATTR::outputroot}/logs/{ATTR::base-hydrology-directory}/failed_events_{ENV::CC_EVENT_IDENTIFIER}.csv",
                    "time_log":"{ATTR::scenario}/{ATTR::outputroot}/logs/{ATTR::base-hydrology-directory}/timings_{ENV::CC_EVENT_IDENTIFIER}.csv"
                },
                "store_name": "FFRD"
            }]
        }
    ]
}
```
# Outputs

   - Format

    - fields

    - field definitions

# Error Handling

# Usage Notes

# Future Enhancements

# Patterns and best practices