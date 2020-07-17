# distage-test-project

## App Functionality

This application fetches the data on infected covid19 people

## Data Source

There are three types of data sources:
* World sourse fetches data on infected in the world by country
* Russia fetches data on infected in Russia by region
* Mock datasourse for testing purposes

Each datasource have it's own DI axis.

Each data frame contains information about location and infected people data

## Data storage

The Dummy storage stores data in map with location name as a key.

The main storage is Cassandra. The data are stored in a table called covid. For each type of the data source we use the separate keyspace. You can change Cassandra configuration in the `common-reference.conf`. Your should define config for the each data source axis separately, like in the example below:

```
cassandra {
  mock {
    host = "172.17.0.2"
    keySpace = "mock"
    port = 9042
  }
  world {
    host = "172.17.0.2"
    keySpace = "world"
    port = 9042
  }
}
```
The config includes the following parameters:
* `host` - the host address for the Cassandra connection
* `port` - the primary port number for Cassandra connection
* `keyspace` - the keyscpace. Can be the same for different datasources but not recommended

## Endpoint

Endpoint is the example of usage. It just retrieve information and save it in a storage

