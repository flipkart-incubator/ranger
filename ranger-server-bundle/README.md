# Ranger Server Bundle

Ranger server bundle is a common dropwizard bundle atop which we could implement any http based ranger backend. 
- Provides the core interface atop which any ranger http backend could be built.
- Provides type-safe generic interface for integration with the above. 
- A really easy plug and play system to get started. 
- Abstracts out ranger's finder hub's complexity. 
- Written atop the wonderful. [Dropwizard](http://dropwizard.io/)

#### Using a server bundle to initialize a ZK backend. 

```
    val rangerServerBundle = new RangerServerBundle<ShardInfo, Criteria<ShardInfo>, ZkNodeDataDeserializer<ShardInfo>,
                AppConfiguration>() {

            @Override
            protected List<RangerHubClient<ShardInfo, Criteria<ShardInfo>>> withHubs(AppConfiguration configuration) {
                return Lists.newArrayList(
                        RangerServerUtils.buildRangerHub(curatorFramework, rangerConfiguration, environment.getObjectMapper())
                );
            }

            @Override
            protected boolean withInitialRotationStatus(AppConfiguration configuration) {
                return appConfiguration.isInitialRotationStatus();
            }
        };
        rangerServerBundle.run(appConfiguration, environment);
        
     rangerServerBundle.start()   
```

#### Using a server bundle to initialize a HTTP backend.

```
       val rangerServerBundle = new RangerServerBundle<ShardInfo, Criteria<ShardInfo>, HTTPResponseDataDeserializer<ShardInfo>,
                AppConfiguration>() {

            @Override
            protected List<RangerHubClient<ShardInfo, Criteria<ShardInfo>>> withHubs(AppConfiguration configuration) {
                val clientConfigs = configuration.getRangerConfiguration().getHttpClientConfigs();
                return clientConfigs.stream().map(clientConfig ->
                        RangerHttpServerUtils.buildRangerHub(
                                rangerConfiguration.getNamespace(),
                                rangerConfiguration.getNodeRefreshTimeMs(),
                                clientConfig,
                                environment.getObjectMapper()
                )).collect(Collectors.toList());
            }

            @Override
            protected boolean withInitialRotationStatus(AppConfiguration configuration) {
                return appConfiguration.isInitialRotationStatus();
            }
        };
        rangerServerBundle.run(appConfiguration, environment);
    rangerServerBundle.start();
```

Stop the bundle once you are done with it. 

```
rangerServerBundle.stop();
```