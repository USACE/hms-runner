# hms-runner

a lightweight hms runner in linux for wat-api

the following environmental variables will need to be configured to run this project:

```

GDAL_DATA

PROJ_LIB

CC_AWS_REGION

CC_AWS_ACCESS_KEY_ID

CC_AWS_SECRET_ACCESS_KEY

CC_AWS_DEFAULT_REGION

CC_AWS_S3_BUCKET

CC_ROOT

CC_MANIFEST_ID

CC_EVENT_NUMBER

FFRD_AWS_DEFAULT_REGION

FFRD_AWS_ACCESS_KEY_ID

FFRD_AWS_SECRET_ACCESS_KEY

FFRD_AWS_S3_BUCKET

USERNAME

TOKEN

```

where `USERNAME` and `TOKEN` are github credentials, and the token has read-access.

hms-runner uses the cc-java-sdk (CC) to look in the bucket specified by `CC_AWS_S3_BUCKET` at the path `$CC_ROOT/$CC_MANIFEST_ID` for a file called `payload` which contains information for the hms-runner to perform various "actions" (for example, the action specified in computeForecastAction.java). A Payload is a JSON formatted file defined by a an "attributes" dictionary containing properties of the HMS model being run, along with various slices such as a "stores" slice to define various S3 stores to be used, as well as a slice of DataSources that are inputs, a slice of DataSources that are outputs, and a slice of Actions that are the actions to be performed by the hms-runner. This is the structured way that CC can provide plugins information to compute within the framework.

The mapping of input files for the hms-runner is as follows:

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

Run the hms-runner with Docker:

```
docker build . -t <image_name>
docker run <image_name>
```

Optionally, if a `.env` file was created, it can be passed to the container with `--env-file .env ` coming BEFORE the image_name in the run command.
