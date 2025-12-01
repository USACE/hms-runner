# ComputeSimulationAction

# Description
Supports the exporting of excess precip from HEC-HMS after a simulation has been computed.

# Implementation Details

# Process Flow
This action assumes the project files (including all requisite results from a simulation) exist within the container before computing. The easiest way to ensure this is the case is to use this action in the same manifest after a compute_simulation action to ensure that the compute_simulation has been successfully performed.
# Configuration

   ## Environment

   ## Attributes

   ### Action
   * project_file
   * simulation
   * destination_path (if the destination path ends with .dss a dss file will be generated, if it ends with .p01.tmp.hdf an hdf file in the format expected by HEC-RAS will be generated.)

   ### Global
   * any substitution variables.
   ## Inputs
   This action requires an HEC-HMS model with all of its files to be local to the container prior to computing, that could be accomplished through a volume mount or through specification of inputs at the global level, all default paths for each data source will be copied from remote to local. All global inputs are copied locally prior to action execution. The input datasources are typically broken into two major components, the hms base directory, and the data directory (per standard hms file storage conventions). As noted above, this action requires a successful run of the simulation prior to exporting, so using this action after the compute-simulation action is the most logical way.
    ### Input Data Sources
      * hms-project-file

   ## Outputs
   The primary output from this is the exported excess precipitation.
     ### Output Data Sources
      * user named exported precipitation.
# Configuration Examples

# Outputs

   - Format

    - fields

    - field definitions

# Error Handling

# Usage Notes

# Future Enhancements

# Patterns and best practices