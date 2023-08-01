# hms-runner

A lightweight hms-runner for cloud wat (or cloud compute: `cc`)


This library uses the [cc-java-sdk](https://github.com/USACE/cc-java-sdk) to read from a `payload` file stored in the bucket specified by `CC_AWS_S3_BUCKET` at the path `$CC_ROOT/$CC_MANIFEST_ID/payload`. The `payload` contains information for the hms-runner to perform various `actions`.

This library provides tools to for running hms in a containerized environment with the following `Actions`:

1. [downloadInputsAction](\src/main/java/usace/cc/plugin/hmsrunner/downloadInputsAction.java): Verifies connection to S3 and downloads all files listed in the `payload` to the local directory (i.e. in the container) `/models`.
2. 
3. 

The `payload` is a JSON formatted file defined by an `attributes` dictionary containing properties available to the various actions:

- stores: 
- sources:  
    - inputs:
    - outputs: 

The `payload` provides a structured and consistent serialization of required data that CC can provide to plugins.



## Getting Started

### Requirements

- [Docker](https://docs.docker.com/get-docker/)
- other (java)

### Running the System
This stack requires the following....

#### Running system the first time

Run the hms-runner with Docker:

```
docker build . -t <image_name>
docker run <image_name>
```

Optionally, if a `.env` file was created, it can be passed to the container with `--env-file .env ` coming BEFORE the image_name in the run command.

...


## Payload

The [payload](example-payload.json) ....mapping of input files for the hms-runner is as follows:

```
kanawha
│
└───hms
│   │   sample.met
│   │   sample.control
│   │   model.grid
│   │   ...
│   │
│   └───data
│       │   sample.dss
│       │   ...
|		└───Streamflow
|			│   Resevoirs.dss
|			│   ...
└───runs
│   └───51
|		└───hms-mutator
|			│   sample.control
|			│   sample.met
|			│   model.met
|			│   ...
|			└───data
│       		│   sample.dss
│       		│   ...
|				└───Streamflow
|					│   Resevoirs.dss
|					│   ...
|		└───hms-runner
|			│   output.dss
```

