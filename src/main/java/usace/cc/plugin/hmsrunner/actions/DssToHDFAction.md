# DssToHdfAction

# Description
Supports the process of converting HEC-DSS timeseries output into HDF tables. Since the HEC-HMS output of the timeseries DSS is local after the HMS compute, it is simple to push the result to all HEC-RAS hdf files at that time to minimize loading the exported precip for each ras model at a later time. Though if modifications to an HDF file happen after HEC-HMS has computed, this action can be used to modify the new HDF files with the previously computed DSS files also.

# Implementation Details

# Process Flow
This action assumes the input files exist within the container before computing.
The action opens the source input DSS at the default path in defined in the action, copies all records from the data-paths, and pastes the copied time series in the destination output datasource for all matching specified data-path keys in the destination HDF file.
# Configuration

   ## Environment
   CC_EVENT_IDENTIFIER (if substitution is used for exported hdf file and destination hdf file output locations)
   ## Attributes
   * any substitution variables.
   ### Action
   * dss_to_hdf

   ### Global

   ## Inputs
   This action requires an input DSS file containing time series data and a destination hdf file with data-paths describing where to put each time series being migrated.
    ### Input Data Sources
      * some dss file
      * the destination hdf file
   ## Outputs
   The primary output from this action is the destination hdf files with local-flow input into bc-line boundary conditions in HEC-RAS hdf files.
     ### Output Data Sources
      * model-name-1.p01.hdf
# Configuration Examples
```json
{
   "manifest_name": "example-dss_to_hdf",
   "plugin_definition": "FFRD-HMS-RUNNER-TRINITY",
   "inputs": {
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
        "data_sources": [
            {
                "name": "lavon.p01.hdf",
                "paths": {
                    "default": "{ATTR::scenario}/{ATTR::base-hydraulics-directory}/lavon/lavon.p01.hdf"
                },
                "store_name": "FFRD"
            },
            {
                "name": "{ATTR::hydrology-simulation}.dss",
                "paths": {
                    "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-hydrology-directory}/{ATTR::hydrology-simulation}.dss"
                },
                "store_name": "FFRD"
            }
        ],

    },
   "outputs": [
        {
            "name": "lavon.p01.hdf",
            "paths": {
                "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-hydraulics-directory}/lavon/lavon.p01.tmp.hdf"
            },
            "store_name": "FFRD"
        }
    ],
   "stores": [
        {
            "name": "FFRD",
            "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            "store_type": "S3",
            "profile": "FFRD",
            "params": {
                "root": "model-library/ffrd-trinity"
            }
        }
    ],
   "actions":[
         {  
            "description": "updating baseflows from hms output",
            "name": "dss_to_hdf",
            "type": "dss_to_hdf",
            "attributes": {
                "base-hydraulics-directory": "hydraulics",
                "base-hydrology-directory": "hydrology",
                "base-reservoir-operations-directory": "reservoir-operations",
                "base-system-response-directory": "system-response",
                "hydrology-simulation": "SST",
                "model-name": "trinity",
                "modelPrefix": "lavon",
                "outputdir": "simulations",
                "outputroot": "simulations/event-data",
                "plan": "01",
                "scenario": "conformance",
                "watershed": "ffrd_trinity"
            },
            "inputs": [
                {
                    "name": "source",
                    "paths": {
                        "default": "/model/{ATTR::model-name}/{ATTR::hydrology-simulation}.dss"
                    },
                    "data_paths": {
                        "east-fork_s090": "//east-fork_s090/FLOW-BASE//1Hour/RUN:SST/",
                        "east-fork_s100": "//east-fork_s100/FLOW-BASE//1Hour/RUN:SST/",
                        "east-fork_s110": "//east-fork_s110/FLOW-BASE//1Hour/RUN:SST/",
                        "east-fork_s120": "//east-fork_s120/FLOW-BASE//1Hour/RUN:SST/",
                        "indian-ck_s010": "//indian-ck_s010/FLOW-BASE//1Hour/RUN:SST/",
                        "indian-ck_s020": "//indian-ck_s020/FLOW-BASE//1Hour/RUN:SST/",
                        "indian-ck_s030": "//indian-ck_s030/FLOW-BASE//1Hour/RUN:SST/",
                        "indian-ck_s040": "//indian-ck_s040/FLOW-BASE//1Hour/RUN:SST/",
                        "sister-grove_s010": "//sister-grove_s010/FLOW-BASE//1Hour/RUN:SST/",
                        "sister-grove_s020": "//sister-grove_s020/FLOW-BASE//1Hour/RUN:SST/",
                        "wilson-ck_s010": "//wilson-ck_s010/FLOW-BASE//1Hour/RUN:SST/"
                    },
                    "store_name": "FFRD"
                }
            ],
            "outputs": [
                {
                    "name": "destination",
                    "paths": {
                        "default": "/model/{ATTR::model-name}/lavon.p{ATTR::plan}.hdf"
                    },
                    "data_paths": {
                        "east-fork_s090": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_east-fork_s090_base",
                        "east-fork_s100": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_east-fork_s100_base",
                        "east-fork_s110": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_east-fork_s110_base",
                        "east-fork_s120": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_east-fork_s120_base",
                        "indian-ck_s010": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_indian-ck_s010_base",
                        "indian-ck_s020": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_indian-ck_s020_base",
                        "indian-ck_s030": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_indian-ck_s030_base",
                        "indian-ck_s040": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_indian-ck_s040_base",
                        "sister-grove_s010": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_sister-grove_s010_base",
                        "sister-grove_s020": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_sister-grove_s020_base",
                        "wilson-ck_s010": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/2D: lavon BCLine: bc_wilson-ck_s010_base"
                    },
                    "store_name": "FFRD"
                }
            ],
            "stores": [
                {
                    "name": "FFRD",
                    "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                    "store_type": "S3",
                    "profile": "FFRD",
                    "params": {
                        "root": "model-library/ffrd-trinity"
                    }
                }
            ]
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