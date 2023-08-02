# hms-runner

A lightweight hms-runner for cloud wat (or cloud compute: `cc`)

This library uses the [cc-java-sdk](https://github.com/USACE/cc-java-sdk) to read from a `payload` file stored in the bucket specified by `CC_AWS_S3_BUCKET` at the path `$CC_ROOT/$CC_MANIFEST_ID/payload`. In general, the `payload` provides a structured and consistent serialization of required data that CC can provide to plugins. In this case, the `payload` contains information for hms-runner to perform various `actions`.

This library provides tools for running hms in a containerized environment with the following `Actions`:

1. [downloadInputsAction](\src/main/java/usace/cc/plugin/hmsrunner/downloadInputsAction.java): Verifies connection to S3 and downloads all files listed in the `payload` to the local directory (i.e. in the container) `/model/{model_name}` where `model_name` is specified in the payload as an attribute.
2. [computeForecastAction](\src/main/java/usace/cc/plugin/hmsrunner/computeForecast.java): Opens a project and runs a forecast for that project.
3. [computeSimulationAction](\src/main/java/usace/cc/plugin/hmsrunner/computeSimulationAction.java): Opens a project and runs a simulation for that project.
4. [CopyPrecipAction](\src/main/java/usace/cc/plugin/hmsrunner/CopyPrecipAction.java): Connects to a h5 instance to access hdf5 storage and copy files from a `source` to a `destination` as specified by the payload action.
5. [dsstoCsvAction](\src/main/java/usace/cc/plugin/hmsrunner/dsstoCsvAction.java): Reads a .dss file to write the `HecTimeSeries` to a destination csv file based on the datasource path
6. [dsstoCHdfAction](\src/main/java/usace/cc/plugin/hmsrunner/dsstoHdfAction.java): Reads a .dss file to write the `HecTimeSeries` to a destination csv file based on the datasource path
7. [ExportExcessPrecipAction](\src/main/java/usace/cc/plugin/hmsrunner/ExportExcessPrecipAction.java): Uses the hms model's `exportSpatialResults` function to export spatial variable data to a specified `destination`
8. [pushOutputsAction](\src/main/java/usace/cc/plugin/hmsrunner/pushOutputsAction.java): Takes the output files specified in the `payload` and pushes them to the S3 bucket.

## Getting Started

### Requirements

- [Docker](https://docs.docker.com/get-docker/)
- AWS S3

### Running the System

Run the hms-runner with Docker:

```
docker build --build-arg USERNAME=<USERNAME> --build-arg TOKEN=<TOKEN> -t <image_name> .
docker run --env-file .env-example <image_name>
```

## Payload Example

This [payload](example-payload.json) contains the following mapping of input and output files for the hms-runner:

```
kanawha
│
└───hms
│   │   Jan_1996.control
│   │   KanawhaCWMS___1996.basin
│   │   KanawhaCWMS___1996.sqlite
│   │   KanawhaHMS.dss
│   │   KanawhaHMS.gage
│   │   KanawhaHMS.hms
│   │   KanawhaHMS.pdata
│   │   KanawhaHMS.run
│   │
│   └───data
│       │   Alderson_to_Hilldale_1.dss
|       |   Alderson_to_Hildale.dss
|       └───Streamflow
|           │   Resevoirs.dss
|           │   Streamflow.dss
└───runs
│   └───51
|       └───hms-mutator
|       |   │   Jan_1996.met
|       |   │   KanawhaHMS.grid
|       |   └───data
│       |       │   Storm.dss
|       └───hms-runner
|           │   Jan_1996___Calibration.dss
```
