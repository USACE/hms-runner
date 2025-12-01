# ComputeSimulationAction

# Description
Supports the computation of a simulation in HEC-HMS.

# Implementation Details

# Process Flow
This action assumes the project files exist within the container before computing.
# Configuration

   ## Environment

   ## Attributes

	### Action
   * project_file
   * simulation

    ### Global

   ## Inputs
   This action requires an HEC-HMS model with all of its files to be local to the container prior to computing, that could be accomplished through a volume mount or through specification of inputs at the global level, all default paths for each data source will be copied from remote to local. All global inputs are copied locally prior to action execution. The input datasources are typically broken into two major components, the hms base directory, and the data directory (per standard hms file storage conventions).
    ### Input Data Sources
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
      * data/all relevant files
   ## Outputs
   The primary output from hms is typically a dss file, if other output files are necessary, specify them as global output files and they will be copied to remote at the conclusion of the actions (all default path keys will be copied to remote from local by default).
     ### Output Data Sources
      * simulation-dss
      * other user specified files (log files, other exported outputs)
# Configuration Examples

# Outputs

   - Format

    - fields

    - field definitions

# Error Handling

# Usage Notes

# Future Enhancements

# Patterns and best practices