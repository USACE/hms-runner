# DssToHdfPoolElevationAction

# Description
Supports the process of converting HEC-DSS timeseries output into HDF tables that define initial conditions through HEC-RAS ICPoints. Since the HEC-HMS output of the timeseries DSS is local after the HMS compute, it is simple to push the result to all HEC-RAS hdf files at that time to minimize loading dss for each ras model at a later time. Though if modifications to an HDF file happen after HEC-HMS has computed, this action can be used to modify the new HDF files with the previously computed DSS files also.
Specifically this action takes the first timestep of an input DSS record and uses it to update the elevation values for ICPoints to set the inital pool elevation in HEC-RAS. This supports coordination of starting conditions for pool elevations for HEC-HMS and HEC-RAS, and a similar action sets HEC-ResSim pool elevations to match HEC-HMS to coordinate all three models having the same starting conditions for pool elevations.

# Implementation Details

# Process Flow
This action assumes the input files exist within the container before computing.
The action opens the source input DSS at the default path in defined in the action, copies the first timestep from the identified time series path, and pastes the first timestep value in the destination output datasource for all matching specified data-path keys in the destination HDF file.
# Configuration

   ## Environment
   CC_EVENT_IDENTIFIER (if substitution is used for exported hdf file and destination hdf file output locations)
   ## Attributes
   * any substitution variables.
   ### Action
   * dss_to_hdf_pool_elevations

   ### Global

   ## Inputs
   This action requires an input DSS file containing time series data and a destination hdf file with data-paths describing where to put each time series being migrated.
    ### Input Data Sources
      * an action level input datasource named 'source'
        paths: 
        * a default path to a dss file
        data-paths:
        * a key named by the icpoint name in HEC-RAS (e.g. ic_nid_tx0007) with a value of the elevation time series in the dss file that is the source for the starting elevation.
   ## Outputs
   The primary output from this action is the destination hdf files with local-flow input into bc-line boundary conditions in HEC-RAS hdf files.
     ### Output Data Sources
    * an action level output datasource named 'destination'
        paths: 
        * a default path to an hdf file
        data-paths:
        * a key named by the icpoint name in HEC-RAS (e.g. ic_nid_tx0007) with a value of the ic point name.
        * a key named "TwoDFlowAreaName" with a value of the 2DFlowArea name (e.g. lavon).
        * a key named CellsCSV with a value of a comma separated string of the cell id's in the 2DFlowArea name to update with this elevation.
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
            "name": "dss_to_hdf_pool_elevations",
            "description": "updating pool elevations at icpoints",
            "type": "dss_to_hdf_pool_elevations",
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
                        "ic_nid_tx00007_lavon": "//Lavon-Pool/Elev//1Hour/fema_ffrd-0/"
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
                        "CellsCSV": "683,690,704,705,707,708,711,712,716,717,718,28313",
                        "TwoDFlowAreaName": "lavon",
                        "ic_nid_tx00007_lavon": "ic_nid_tx00007_lavon"
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