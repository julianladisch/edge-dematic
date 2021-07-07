# edge-dematic

Copyright (C) 2021 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction
The purpose of this edge API is to bridge the gap between Dematic remote storage provider and FOLIO.

There are two modules involved in Dematic workflow: mod-remote-storage, which interacts with other Folio modules and edge-dematic, which acts as a gate between Dematic and Folio:

`FOLIO <–> mod-remote-storage <–> edge-dematic <–> Dematic`

Edge-dematic supports two separate ways of communication 
* HTTP endpoints for Dematic EMS 
* TCP/IP sockets for Dematic StagingDirector. 

Dematic EMS interacts with Folio via HTTP endpoints (see [API Details](#api-details)). All flows – accession, retrieve and return – are initiated by Dematic. For each flow Dematic EMS polls appropriate edge-dematic endpoint. Each request must contain apikey as a query parameter: `/asrService/lookupNewASRItems/aaa-bbb-ccc?apikey=someApiKey`

Dematic StagingDirector (hereinafter SD) requires two TCP/IP sockets (channels):
* primary channel for sending requests to SD
* status channel for responses from SD 

Both connections initiated and maintained on Folio side by `edge-dematic` module. 

Accession and retrieval flows initiated by Folio – at the configurable interval edge-dematic checks two queues: accession queue and retrieval queue (filled by `mod-remote-storage` when circulation events occur). If new records are present, module sends requests to SD via primary channel. SD then sends responses or, in case of item return, a return message via status channel.

## Additional information

### API Details
API provides the following URLs for working with remote storage configurations:

| Method | URL| Description | 
|---|---|---|
| GET | /asrService/asr/lookupNewAsrItems/{remoteStorageConfigurationId}  | The polling API for accessions |
| GET | /asrService/asr/updateASRItemStatusBeingRetrieved/{remoteStorageConfigurationId} | The polling API for retrievals |
| GET | /asrService/asr//updateASRItemStatusBeingRetrieved/{remoteStorageConfigurationId} | The API for retrieve |
| POST | /asrService/asr/updateASRItemStatusAvailable/{remoteStorageConfigurationId} | The API for return |

### Deployment information
#### Dematic StagingDirector setup
1. Dematic StagingDirector connection should be established from the Dematic edge Folio module. Therefore Dematic edge module 
needs to know the name of all the tenants, which has StagingDirector connection. For the ephemeral configuration these names locate in the
`ephemeral.properties` (key `tenants`). In order to provide it before the deployment the list of tenant names (e.g. ids) should be put to AWS parameters store (as String). The tenant names list separated by 
coma (e.g. diku, someothertenantname) should be stored in AWS param store in the variable with 
key: `stagingDirector_tenants` by default or could be provided its own key through `staging_director_tenants` parameter of starting module. 
2. For each tenant using StagingDirector the corresponding user should be added 
to the AWS parameter store with key in the following format `{{username}}_{{tenant}}_{{username}}` (where salt and username are the same - `{{username}}`) with value of corresponding `{{password}}` (as Secured String). 
This user should work as ordinary edge institutional user with the only one difference 
- his username and salt name are - `{{username}}`. 
 By default the value of `{{username}}` is `stagingDirector`. It could be changed through `staging_director_client` parameter of starting module.
3. User `{{username}}` with password `{{password}}` and remote-storage.all permissions should be created on FOLIO. After that apikey can
be generated for making calls to Edge Dematic API.

##### Create Dematic StagingDirector configuration
1. Log in to Folio, go to "Settings" -> "Remote storage" -> "Configurations", click "New" button.
2. Enter General information settings:
* Select "Dematic StagingDirector" in Provider name box
* Enter Remote storage name
* Enter IP address and port in URL (for primary channel) and Status URL (for status channel). Address and port separated by colon (no whitespaces or other symbols), for example `192.168.1.1:1234`
3. Set Data synchronization schedule. This setting defines timeframe to scan accession and retrieval queues and data exchange with provider.
4. Click "Save & close" button

*Note: Dematic StagingDirector configuration settings applied only upon module startup, so in case of their changes, edge-dematic service must be restarted.*   

#### Dematic EMS setup
The deployment information above is related only to Dematic StagingDirector edge user. For Dematic EMS another edge user (with corresponding API_KEY) should be created following the standard process for edge users creation.

##### Create Dematic EMS configuration
1. Log in to Folio, go to "Settings" -> "Remote storage" -> "Configurations", click "New" button.
2. Enter General information settings:
* Select "Dematic EMS" in Provider name box
* Enter Remote storage name
3. Click "Save & close" button

*Note: Since Dematic EMS flows initiated on provider side, all other settings can be omitted.*

### Required Permissions
The following permissions should be granted to institutional users (as well as StagingDirectortenants) in order to use this edge API:
- `remote-storage.all`

### Configuration
Please refer to the [Configuration](https://github.com/folio-org/edge-common/blob/master/README.md#configuration) section in the [edge-common](https://github.com/folio-org/edge-common/blob/master/README.md) documentation to see all available system properties and their default values.

### Issue tracker
See project [EDGDEMATIC](https://issues.folio.org/browse/EDGDEMATIC)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation
Feature documentation [Remote Storage Integration](https://wiki.folio.org/display/DD/Remote+storages+integration).
Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
