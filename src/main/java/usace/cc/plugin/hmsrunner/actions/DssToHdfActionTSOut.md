# DssToHdfAction

# Description
Supports the process of converting HEC-DSS timeseries output into HDF tables for time series output for 2d structures in HEC-RAS. Since the HEC-HMS output of the timeseries DSS is local after the HMS compute, it is simple to push the result to all HEC-RAS hdf files at that time to minimize loading the DSS for each ras model at a later time. Though if modifications to an HDF file happen after HEC-HMS has computed, this action can be used to modify the new HDF files with the previously computed DSS files also.

# Implementation Details

# Process Flow
This action assumes the input files exist within the container before computing.
The action opens the source input DSS at the default path in defined in the action, copies all records from the data-paths, and pastes the copied time series in the destination output datasource for all matching specified data-path keys in the destination HDF file. This specific action differs from dss_to_hdf in that it adjusts the timestep of the data by a half timestep to convert per-average to inst-val. Typically, this is done for output from HEC-ResSim into HEC-RAS, but can be used for HEC-HMS output into HEC-RAS too depending on circumstances of the dams.
# Configuration

   ## Environment
   CC_EVENT_IDENTIFIER (if substitution is used for exported hdf file and destination hdf file output locations)
   ## Attributes
   * any substitution variables.
   ### Action
   * dss_to_hdf_tsout

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
                "name": "simulation.dss",
                "paths": {
                    "default": "{ATTR::scenario}/{ATTR::outputroot}/{ENV::CC_EVENT_IDENTIFIER}/{ATTR::base-reservoir-operations-directory}/simulation.dss"
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
            "name": "dss_to_hdf_tsout",
            "type": "dss_to_hdf_tsout",
            "description": "updating regulated outflows from ressim output",
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
                        "default": "/model/{ATTR::model-name}/simulation.dss"
                    },
                    "data_paths": {
                        "nid_tx00007": "//Lavon Outflow/Flow//1Hour/fema_ffrd-0/"
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
                        "nid_tx00007": "Event Conditions/Unsteady/Boundary Conditions/Flow Hydrographs/SA Conn: nid_tx00007 (Outlet TS: nid_tx00007)"
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