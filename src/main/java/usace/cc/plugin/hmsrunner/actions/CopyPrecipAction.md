# CopyPrecipAction

# Description
Supports the process of loading multiple HEC-RAS HDF files with gridded precipitation exported from HEC-HMS through Vortex. Since the HMS model domain typically covers the geographic domain of many HEC-RAS models, exporting from one HMS simulation exported precip to many HEC-RAS input HDF files is a common need. Since the HEC-HMS output of the exported precipitation grid is local after the HMS compute, it is simple to push the result to all HEC-RAS hdf files at that time to minimize loading the exported precip for each ras model at a later time. Though if modifications to an HEC-RAS HDF file happen after HEC-HMS has computed, this action can be used to modify the new HDF files with the previously computed exported precipitation in HDF format from Vortex.

# Implementation Details

# Process Flow
This action assumes the input files exist within the container before computing.
The action opens the source input at the default path in defined in the action, at the default data-path, and pastes the table in the destination output datasource for all specified path keys at the default data-path.
# Configuration

   ## Environment
   CC_EVENT_IDENTIFIER (if substitution is used for exported hdf file and destination hdf file output locations)
   ## Attributes
   * any substitution variables.
   ### Action
   * copy_precip_table

   ### Global

   ## Inputs
   This action requires an exported hdf file containing precipitation and a set of destination hdf files needing precipitation input into them.
    ### Input Data Sources
      * exported-precip.p01.tmp.hdf
      * any destination hdf files.
      * 
   ## Outputs
   The primary output from this action is the destination hdf files with the precip copied into them.
     ### Output Data Sources
      * model-name-1.p01.hdf
# Configuration Examples
```json
        "payload_attributes": {
            "base-hydraulics-directory": "hydraulics",
            "base-hydrology-directory": "hydrology",
            "base-reservoir-operations-directory": "reservoir-operations",
            "base-system-response-directory": "system-response",
            "hydrology-simulation": "SST",
            "model-name": "trinity",
            "outputdir": "simulations",
            "outputroot": "simulations/event-data",
            "plan": "01",
            "scenario": "conformance",
            "watershed": "ffrd_trinity"
        },
         "inputs": {
            "data_sources": [
               {
                  "name": "exported-precip_{ATTR::model-name}.p{ATTR::plan}.tmp.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-hydrology-directory}/exported-precip_{ATTR::model-name}.p{ATTR::plan}.tmp.hdf"
                  },
                  "data_paths": {
                     "default": "Event Conditions/Meteorology/Precipitation/Values"
                  },
                  "store_name": "FFRD"
               },
               {
                  "name": "blw-bear.p01.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::base-hydraulics-directory}/blw-bear/blw-bear.p01.hdf"
                  },
                  "store_name": "FFRD"
               },
               {
                  "name": "blw-elkhart.p01.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::base-hydraulics-directory}/blw-elkhart/blw-elkhart.p01.hdf"
                  },
                  "store_name": "FFRD"
               },
               {
                  "name": "clear-creek.p01.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::base-hydraulics-directory}/clear-creek/clear-creek.p01.hdf"
                  },
                  "store_name": "FFRD"
               }
            ],
         },
         "outputs": [
            {
                  "name": "blw-bear.p01.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-hydraulics-directory}/blw-bear/blw-bear.p01.tmp.hdf"
                  },
                  "store_name": "FFRD"
            },
            {
                  "name": "blw-elkhart.p01.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-hydraulics-directory}/blw-elkhart/blw-elkhart.p01.tmp.hdf"
                  },
                  "store_name": "FFRD"
            },
            {
                  "name": "clear-creek.p01.hdf",
                  "paths": {
                     "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-hydraulics-directory}/clear-creek/clear-creek.p01.tmp.hdf"
                  },
                  "store_name": "FFRD"
            }
         ],
         "actions":[{
            "name": "copy_precip_table",
            "type": "copy_precip_table",
            "description": "copy_precip_table",
            "attributes": {
                "base-hydraulics-directory": "hydraulics",
                "base-hydrology-directory": "hydrology",
                "base-reservoir-operations-directory": "reservoir-operations",
                "base-system-response-directory": "system-response",
                "hydrology-simulation": "SST",
                "model-name": "trinity",
                "outputdir": "simulations",
                "outputroot": "simulations/event-data",
                "plan": "01",
                "scenario": "conformance",
                "watershed": "ffrd_trinity"
            },
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
            "inputs": [
                {
                    "name": "source",
                    "paths": {
                        "default": "/model/{ATTR::model-name}/exported-precip_{ATTR::model-name}.p{ATTR::plan}.tmp.hdf"
                    },
                    "data_paths": {
                        "default": "Event Conditions/Meteorology/Precipitation/Values"
                    },
                    "store_name": "FFRD"
                }
            ],
            "outputs": [
                {
                    "name": "destination",
                    "paths": {
                        "bardwell-creek": "/model/{ATTR::model-name}/bardwell-creek.p01.hdf",
                        "bedias-creek": "/model/{ATTR::model-name}/bedias-creek.p01.hdf",
                        "blw-bear": "/model/{ATTR::model-name}/blw-bear.p01.hdf",
                        "blw-clear-fork": "/model/{ATTR::model-name}/blw-clear-fork.p01.hdf",
                        "blw-east-fork": "/model/{ATTR::model-name}/blw-east-fork.p01.hdf",
                        "blw-elkhart": "/model/{ATTR::model-name}/blw-elkhart.p01.hdf",
                        "blw-richland": "/model/{ATTR::model-name}/blw-richland.p01.hdf",
                        "blw-west-fork": "/model/{ATTR::model-name}/blw-west-fork.p01.hdf",
                        "bridgeport": "/model/{ATTR::model-name}/bridgeport.p01.hdf",
                        "cedar-creek": "/model/{ATTR::model-name}/cedar-creek.p01.hdf",
                        "chambers-creek": "/model/{ATTR::model-name}/chambers-creek.p01.hdf",
                        "clear-creek": "/model/{ATTR::model-name}/clear-creek.p01.hdf",
                        "clear-fork": "/model/{ATTR::model-name}/clear-fork.p01.hdf",
                        "denton": "/model/{ATTR::model-name}/denton.p01.hdf",
                        "eagle-mountain": "/model/{ATTR::model-name}/eagle-mountain.p01.hdf",
                        "east-fork": "/model/{ATTR::model-name}/east-fork.p01.hdf",
                        "kickapoo": "/model/{ATTR::model-name}/kickapoo.p01.hdf",
                        "lavon": "/model/{ATTR::model-name}/lavon.p01.hdf",
                        "lewisville": "/model/{ATTR::model-name}/lewisville.p01.hdf",
                        "livingston": "/model/{ATTR::model-name}/livingston.p01.hdf",
                        "mill-creek": "/model/{ATTR::model-name}/mill-creek.p01.hdf",
                        "mountain": "/model/{ATTR::model-name}/mountain.p01.hdf",
                        "ray-hubbard": "/model/{ATTR::model-name}/ray-hubbard.p01.hdf",
                        "ray-roberts": "/model/{ATTR::model-name}/ray-roberts.p01.hdf",
                        "rchlnd-chmbers": "/model/{ATTR::model-name}/rchlnd-chmbers.p01.hdf",
                        "white-rock": "/model/{ATTR::model-name}/white-rock.p01.hdf"
                    },
                    "data_paths": {
                        "default": "Event Conditions/Meteorology/Precipitation/Values"
                    },
                    "store_name": "FFRD"
                }
            ]
        }
    ],

```


# Outputs

   - Format

    - fields

    - field definitions

# Error Handling

# Usage Notes

# Future Enhancements

# Patterns and best practices