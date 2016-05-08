# Ranger [![Travis build status](https://travis-ci.org/flipkart-incubator/ranger.svg?branch=master)](https://travis-ci.org/flipkart-incubator/ranger) [![Join the chat at https://gitter.im/flipkart-incubator/ranger](https://badges.gitter.im/flipkart-incubator/ranger.svg)](https://gitter.im/flipkart-incubator/ranger?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Ranger is a high level service discovery framework built on Zookeeper. The framework brings the following to the table:
  - Support of sharding of the service provider nodes
  - Support for monitoring of service provider nodes
  - Provides type-safe generic interface for integration with support for custom serializers and deserializers
  - Provides simple ways to plug in custom shard and node selection
  - Fault tolerant client side discovery with a combination of watchers and polling on watched nodes

## Why?

As request rates increase, load balancers, even the very expensive ones, become bottlenecks. We needed to move beyond and be able to talk to services without having to channel all traffic through a load-balancer. There is obviously curator discovery; but as much as we love curator, we needed more features on top of it. As such we built this library to handle app level sharding and healtchecks. Btw, it still uses curator for low level ZK interactions.

## Usage
Ranger provides two types of discovery out of the box:
  - Simple unsharded service discovery with service provider node healthchecks
  - Sharded service discovery with service provider node healthchecks
We'll take these up, one by one.

###Build instructions
  - Clone the source:

        git clone github.com/flipkart-incubator/ranger

  - Build

        mvn install

### Maven Dependency
Use the following repository:
```
<repository>
    <id>clojars</id>
    <name>Clojars repository</name>
    <url>https://clojars.org/repo</url>
</repository>
```
Use the following maven dependency:
```
<dependency>
    <groupId>com.flipkart.ranger</groupId>
    <artifactId>ranger</artifactId>
    <version>0.3.0</version>
</dependency>
```
## How it works
There are service providers and service clients. We will look at the interactions from both sides.
### Service Provider
Service providers register to the Ranger system by building and starting a ServiceProvider instance. During registering itself the provider must provide the following:
- _ShardInfo type_ - This is a type parameter to the ServiceProvider class. This can be any class that can be serialized and deserialized by the Serializer and Deserializer provided (see below). The hashCode() for this class is used to match and find the matching shard for a query. So be sure to implement this properly. A special version of this class _UnshardedClusterInfo_ is provided for unsharded discovery.
- _Zookeeper details_ - Can be any one of the following:
  - _Connection String_ - Zookeeper connections string to connect to ZK cluster.
  - CuratorFramework object - A prebuilt CuratorFramework object.
- _Namespace_ - A namespace for the service. For example the team name this service belongs to.
- _Service Name_ - Name of the service to be used by client for discovery.
- _Host_ - Hostname for the service.
- _Port_ - Port on which this service is running.
- _Serializer_ - A serializer implementation that will be used to serialize and store the shard information on ZooKeeper
- _Healthcheck_ - The healthcheck function is called every second and the status is updated on Zookeeper. 
- _Monitors_ : The health state of your service is also decided by a list of monitors. Register a list of monitors. These monitors will be monitored at regular intervals and an aggregated status is updated on Zookeeper
  - _Isolated Monitors_ - Each of these monitors will be running continuously on separate isolated threads. Each thread holds an independent state of the isolated monitor. The state of all Monitors will be aggregated an updated on Zookeeper at regular intervals. 

A node will be marked unhealthy iff:
  - The service is stopped.
  - If any isolated monitor's state is _HealthcheckStatus.unhealthy_
  - If _Healthcheck.check()_ has not been updated for over a minute. This signifies that the process is probably zombified.

#### Registering a simple unsharded service
This is very simple. Use the following boilerplate code.
```
ServiceProvider<UnshardedClusterInfo> serviceProvider
            = ServiceProviderBuilders.unshardedServiceProviderBuilder()
                .withConnectionString("localhost:2181")           //Zookeeper host string
                .withNamespace("test")                            //Service namespace
                .withServiceName("test-service")                  //Service name
                .withSerializer(new Serializer<UnshardedClusterInfo>() { //Serializer for info
                    @Override
                    public byte[] serialize(ServiceNode<UnshardedClusterInfo> data) {
                        try {
                            return objectMapper.writeValueAsBytes(data);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .withHostname(host)                            //Service hostname
                .withPort(port)                                //Service port
                .withHealthcheck(new Healthcheck() {           //Healthcheck implementation.
                    @Override
                    public HealthcheckStatus check() {
                        return HealthcheckStatus.healthy;      // OOR stuff should be put here
                    }
                })
                .withIsolatedHealthMonitor(new RotationStatusMonitor(TimeEntity.everySecond(), "/var/rotation.html"))
                .buildServiceDiscovery();
        serviceProvider.start();                               //Start the instance
```
Stop the provider once you are done. (Generally this is when process ends)
```
serviceProvider.stop()
```
#### Registering a sharded service
Let's assume that the following is your shard info class:

```
   private static final class TestShardInfo {
        private int shardId;

        public TestShardInfo(int shardId) {
            this.shardId = shardId;
        }

        public TestShardInfo() {
        }

        public int getShardId() {
            return shardId;
        }

        public void setShardId(int shardId) {
            this.shardId = shardId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestShardInfo that = (TestShardInfo) o;

            if (shardId != that.shardId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return shardId;
        }
    }
```

To register a service provider node with this shard info, we can use the following code:

```
final ServiceProvider<TestShardInfo> serviceProvider
    = ServiceProviderBuilders.<TestShardInfo>shardedServiceProviderBuilder()
                .withConnectionString("localhost:2181")
                .withNamespace("test")
                .withServiceName("test-service")
                .withSerializer(new Serializer<TestShardInfo>() {
                    @Override
                    public byte[] serialize(ServiceNode<TestShardInfo> data) {
                        try {
                            return objectMapper.writeValueAsBytes(data);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .withHostname(host)
                .withPort(port)
                .withNodeData(new TestShardInfo(shardId)) //Set the shard info for this shard
                .withHealthcheck(new Healthcheck() {
                    @Override
                    public HealthcheckStatus check() {
                        return HealthcheckStatus.healthy;
                    }
                })
                .buildServiceDiscovery();
        serviceProvider.start();
```

Stop the provider once you are done. (Generally this is when process ends)

```
serviceProvider.stop()
```

### Monitors
In a distributed architecture, taking care of thousands of servers is a difficult task. Failures are bound to happen, and individual services could always face issues. It becomes very important that we automate handling such failures. Ranger allows you to do that, for your _ServiceProviders_.

As mentioned earlier, the health state of any _ServiceProvider_ is determined by a set of health monitors which are continuously running in the Service Provider.
All monitors (and at least 1) need to be registered while building the _ServiceProvider_.

You may register any kind of _Monitor_, which could be monitoring any serivce/system level metric. For example, you could have monitors:
- that monitor the service's heap, to ensure that it doesn't go beyond a threashold
- that check for any breach in max jetty threads 
- that monitors the systems disk space
- that does a continuous ping test
- that monitors the 5XX count from the service.

If any of the above are breached, the service will automatically be marked as unhealthy.

- _Isolated Monitors_ - Any extention of _IsolatedHealthMonitor_ may be used to register an isolated monitor. Each of these monitors will be running continuously on separate isolated threads. Each thread holds an independent state of the isolated monitor. The state of all Monitors will be aggregated an updated on Zookeeper at regular intervals.
  - _PingCheckMonitor_ - This monitor can be used to ping a url at regular intervals. It could be a self localhost ping too. You can also add minimum failure counts, to ensure that there are no fluctuations
    ```
        .withIsolatedHealthMonitor(new PingCheckMonitor(new TimeEntity(2, TimeUnit.SECONDS), httpRequest, 5000, 5, 3, "google.com", 80));  // put in the url here
    ```
  - _RotationStatusMonitor_ - This monitor can be used check the rotation status of your server, which decides if the host can serve traffic at the moment or not. Removing the file, will automatically prevent this host from getting discovered.
    ```
        ..withIsolatedHealthMonitor(new RotationStatusMonitor(TimeEntity.everySecond(), "/var/rotation.html"));  // path of file to be checked 
    ```
At regular intervals, all of the above monitors will be aggregated into a single Health state of the service, which


### Service discovery
For service discovery, a _ServiceFinder_ object needs to be built and used.
- _ShardInfo type_ - This is a type parameter to the ServiceFinder class. This can be any class that can be serialized and deserialized by the Serializer and Deserializer provided (see below). The hashCode() for this class is used to match and find the matching shard for a query. So be sure to implement this properly. A special version of this class _UnshardedClusterInfo_ is provided for unsharded discovery.
- _Zookeeper details_ - Can be any one of the following:
  - _Connection String_ - Zookeeper connections string to connect to ZK cluster.
  - CuratorFramework object - A prebuilt CuratorFramework object.
- _Namespace_ - A namespace for the service. For example the team name this service belongs to.
- _Service Name_ - Name of the service to be used by client for discovery.
- _Host_ - Hostname for the service.
- _Port_ - Port on which this service is running.
- _Deserializer_ - A deserializer implementation that will be used to deserialize and select shard from zookeeper.

Depending on whether you are looking to access a sharded service or an unsharded service, the code will differ a little.

#### Finding an instance of an unsharded Service Provider
First build and start the finder.

```
UnshardedClusterFinder serviceFinder
            = ServiceFinderBuilders.unshardedFinderBuilder()
                .withConnectionString("localhost:2181")
                .withNamespace("test")
                .withServiceName("test-service")
                .withDeserializer(new Deserializer<UnshardedClusterInfo>() {
                    @Override
                    public ServiceNode<UnshardedClusterInfo> deserialize(byte[] data) {
                        try {
                            return objectMapper.readValue(data,
                                    new TypeReference<ServiceNode<UnshardedClusterInfo>>() {
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .build();
serviceFinder.start();
```

To find an instance:

```
ServiceNode node = serviceFinder.get(null); //null because you don't need to pass any shard info
//User node.hetHost() and node.getPort()
```

Stop the finder once you are done. (Generally this is when process ends)
```
serviceFinder.stop()
```

#### Finding an instance of a sharded Service Provider
This is similar to the above but for the type parameter you are using everywhere.

```
SimpleShardedServiceFinder<TestShardInfo> serviceFinder
    = ServiceFinderBuilders.<TestShardInfo>shardedFinderBuilder()
        .withConnectionString(testingCluster.getConnectString())
        .withNamespace("test")
        .withServiceName("test-service")
        .withDeserializer(new Deserializer<TestShardInfo>() {
            @Override
            public ServiceNode<TestShardInfo> deserialize(byte[] data) {
                try {
                    return objectMapper.readValue(data,
                            new TypeReference<ServiceNode<TestShardInfo>>() {
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        })
        .build();
serviceFinder.start();
```

Now you can find the service:

```
ServiceNode<TestShardInfo> node = serviceFinder.get(new TestShardInfo(1));
//Use host, port etc from the node
```

Stop the finder once you are done. (Generally this is when process ends)

```
serviceFinder.stop()
```

Version
----

0.3.0

Tech
-----------

Ranger uses Apache Curator:

* [Curator](http://curator.apache.org/) - Abstraction library for ZooKeeper operations

# Contribution, Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](https://github.com/flipkart-incubator/ranger/issues).

If you would like to contribute code you can do so through GitHub by forking the repository and sending a pull request.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

## Contribution License

By contributing your code, you agree to license your contribution under the terms of the APLv2:  http://www.apache.org/licenses/LICENSE-2.0

All files are released with the Apache 2.0 license.

If you are adding a new file it should have a header like this:

    /**
     * Copyright 2015 Flipkart Internet Pvt. Ltd.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

LICENSE
-------

Copyright 2015 Flipkart Internet Pvt. Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

